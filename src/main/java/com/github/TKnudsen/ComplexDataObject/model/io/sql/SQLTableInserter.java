package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.TKnudsen.ComplexDataObject.model.tools.Threads;

/**
 * <p>
 * Inserts rows into SQL tables for MySQL and PostgreSQL connections, either
 * one row at a time or in batches via JDBC batching for efficiency. Builds
 * INSERT/INSERT IGNORE/REPLACE statements (emulating MySQL semantics with
 * PostgreSQL's ON CONFLICT where needed), binds row values to prepared
 * statements by Java type, and retries automatically when a data truncation
 * error indicates a column needs to be widened.
 * </p>
 */
public class SQLTableInserter {

	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * Inserts multiple rows into a SQL table.
	 * 
	 * Automatically retries once when a DataTruncation occurs and column capacity
	 * can be extended. Includes recursion-depth safeguard.
	 *
	 * @param conn                         JDBC connection
	 * @param schema                       database schema
	 * @param tableName                    table name (without schema)
	 * @param insertType                   INSERT, INSERT IGNORE, or REPLACE
	 * @param listOfMapWithKeyValuePairs   list of row maps (column to value)
	 * @param extendColumnCapacityIfNeeded whether to attempt column widening on
	 *                                     truncation
	 * @param useFloatInsteadOfDouble      use FLOAT instead of DOUBLE for numeric
	 *                                     mapping
	 * @param showTimingLog                print timing info
	 * @throws SQLException
	 */
	public static void insertRows(Connection conn, String schema, String tableName, String insertType,
			List<LinkedHashMap<String, Object>> listOfMapWithKeyValuePairs, boolean extendColumnCapacityIfNeeded,
			boolean useFloatInsteadOfDouble, boolean showTimingLog) throws SQLException {
		insertRows(conn, schema, tableName, insertType, listOfMapWithKeyValuePairs, extendColumnCapacityIfNeeded,
				useFloatInsteadOfDouble, showTimingLog, 0);
	}

	/**
	 * Internal recursive method with depth limit to avoid infinite retry loops.
	 */
	private static void insertRows(Connection conn, String schema, String tableName, String insertType,
			List<LinkedHashMap<String, Object>> listOfMapWithKeyValuePairs, boolean extendColumnCapacityIfNeeded,
			boolean useFloatInsteadOfDouble, boolean showTimingLog, int depth) throws SQLException {

		final int MAX_RETRY_DEPTH = 3;

		try {
			insertRows(conn, schema, tableName, insertType, listOfMapWithKeyValuePairs, useFloatInsteadOfDouble,
					showTimingLog);
		} catch (DataTruncation e) {
			if (!extendColumnCapacityIfNeeded)
				throw new IllegalArgumentException("SQLTableInserter: caught data truncation: " + e.getMessage(), e);

			if (depth >= MAX_RETRY_DEPTH)
				throw new SQLException("SQLTableInserter: exceeded retry limit (" + MAX_RETRY_DEPTH
						+ ") while handling repeated DataTruncation errors on table " + tableName, e);

			String column = SQLUtils.mitigateDataTruncationError(conn, e, listOfMapWithKeyValuePairs, schema,
					tableName);

			System.err.println("SQLTableInserter: Data truncation detected for column \"" + column
					+ "\". Column extended, retrying insertRows (attempt " + (depth + 1) + "/" + MAX_RETRY_DEPTH
					+ ")...");

			insertRows(conn, schema, tableName, insertType, listOfMapWithKeyValuePairs, extendColumnCapacityIfNeeded,
					useFloatInsteadOfDouble, showTimingLog, depth + 1);
		}
	}

	/**
	 * 
	 * Inserts multiple rows efficiently using JDBC batching.
	 * 
	 * Each batch uses a single prepared statement. Auto-commit is disabled for
	 * performance and consistency.
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param insertType
	 * @param listOfMapWithKeyValuePairs
	 * @param useFloatInsteadOfDouble
	 * @param showTimingLog
	 * @throws SQLException
	 * @throws IllegalArgumentException in case the list of rows planned for the
	 *                                  batch insert do not contain the same
	 *                                  attributes (key-value pairs), which is
	 *                                  required for the more effective batch insert
	 *                                  approach.
	 */
	private static void insertRows(Connection conn, String schema, String tableName, String insertType,
			List<LinkedHashMap<String, Object>> listOfMapWithKeyValuePairs, boolean useFloatInsteadOfDouble,
			boolean showTimingLog) throws SQLException, IllegalArgumentException {

		Objects.requireNonNull(conn);
		Objects.requireNonNull(listOfMapWithKeyValuePairs);

		if (listOfMapWithKeyValuePairs.isEmpty())
			return;

		int size = listOfMapWithKeyValuePairs.size();

		// Built up and emitted as a single atomic write at the end (see below)
		// instead of several separate System.out.print calls spanning the whole
		// insert (including a network round trip) -- the latter let a concurrent
		// caller's own println land in the middle of this one, splicing
		// unrelated log lines together once callers started running in parallel
		// across threads.
		StringBuilder log = showTimingLog
				? new StringBuilder("SQLTableInserter.insertRows: inserting " + size + " rows with "
						+ listOfMapWithKeyValuePairs.get(0).size() + " attributes into " + tableName + "...")
				: null;
		long t0 = System.currentTimeMillis();

		// would let throw an exception if data integrity is not ensured
		assertUniformAttributesOrThrow(listOfMapWithKeyValuePairs, tableName);

		boolean originalAutoCommit = conn.getAutoCommit();
		conn.setAutoCommit(false);

		// derive column list once from the first row
		LinkedHashMap<String, Object> sample = listOfMapWithKeyValuePairs.get(0);

		boolean postgreSQL = PostgreSQL.isPostgreSQLConnection(conn);
		String sql = getInsertSQL(conn, schema, tableName, insertType, sample, true, postgreSQL);

		try (PreparedStatement ps = conn.prepareStatement(sql)) {

			int batchSize = 0;
			final int BATCH_LIMIT = SQLUtils.estimateBatchLimitByBytes(listOfMapWithKeyValuePairs, postgreSQL,
					useFloatInsteadOfDouble, 6000000);

			for (LinkedHashMap<String, Object> row : listOfMapWithKeyValuePairs) {
				rowToPreparedStatement(ps, row, useFloatInsteadOfDouble, postgreSQL);
				ps.addBatch();
				batchSize++;
				if (log != null && size < BATCH_LIMIT)
					log.append('.');

				if (batchSize % BATCH_LIMIT == 0) {
					ps.executeBatch();
					batchSize = 0;
					if (log != null)
						log.append('[').append(BATCH_LIMIT).append(']');
				}
			}
			if (log != null && size > BATCH_LIMIT)
				log.append(':');

			if (batchSize > 0)
				ps.executeBatch();

			conn.commit();

			// refresh
			SQLTableStatistics.clearRowCountCache(schema);

		} catch (SQLException e) {
			conn.rollback();
			e.printStackTrace();

			throw e;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}

		if (log != null)
			System.out.println(log + " done in " + (System.currentTimeMillis() - t0) + " ms");
	}

	public static List<String> assertUniformAttributesOrThrow(List<LinkedHashMap<String, Object>> rows,
			String tableName) throws IllegalArgumentException {

		if (rows == null || rows.isEmpty())
			return java.util.Collections.emptyList();

		LinkedHashMap<String, Object> first = rows.get(0);
		if (first == null)
			throw new IllegalArgumentException("First row is null for table " + tableName);

		// Reference keys and count
		final int refSize = first.size();
		final String[] refKeys = first.keySet().toArray(new String[refSize]);

		outer: for (int i = 1, n = rows.size(); i < n; i++) {
			LinkedHashMap<String, Object> row = rows.get(i);
			if (row == null)
				throw new IllegalArgumentException("Row " + i + " is null in table " + tableName);

			if (row.size() != refSize) {
				throw new IllegalArgumentException("Row " + i + " has different attribute count (" + row.size()
						+ ", expected " + refSize + ") for table " + tableName);
			}

			// Compare key order directly
			int j = 0;
			for (String key : row.keySet()) {
				if (!refKeys[j].equals(key)) {
					throw new IllegalArgumentException(
							"Row " + i + " has different attribute order or missing key at position " + j
									+ " for table " + tableName + ". Expected: " + refKeys[j] + ", Found: " + key);
				}
				j++;
			}
		}

		// Convert once for downstream code
		return java.util.Arrays.asList(refKeys);
	}

	/**
	 * hint: check if table exists
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param insertType              INSERT, INSERT IGNORE, REPLACE
	 * @param keyValuePairs           contains column and value information for this
	 *                                row
	 * @param useFloatInsteadOfDouble
	 */
	public static void insertRow(Connection conn, String schema, String tableName, String insertType,
			LinkedHashMap<String, Object> keyValuePairs, boolean useFloatInsteadOfDouble) throws SQLException {

		PreparedStatement pstmt = insertRowPreparedStatement(conn, schema, tableName, insertType, keyValuePairs,
				useFloatInsteadOfDouble);

		if (pstmt == null)
			return;

		try {
			pstmt.execute();
		} catch (Exception e) {
			e.printStackTrace();

			if (e.getClass().getSimpleName().equals("PSQLException") && insertType.equals("REPLACE")) {
				String errorMessage = e.getMessage();
				if (errorMessage.contains("duplicate key value violates unique constraint")) {
					System.err.println(errorMessage);
					System.err.println("SQLTableInserter.insertRow,  table " + tableName
							+ ": Postgresql has insert on duplicate problem and will delete the existing row and try it again...");

					List<String> pks = getPrimaryKeysFromErrorMessage(errorMessage, false);
					List<String> values = new ArrayList<>();
					for (String pk : pks)
						values.add(keyValuePairs.get(pk).toString());

					// delete original row, wait a bit,
					// a lot of exceptions like these have happened in the past, needs
					Threads.sleep(5);
					SQLTableDeleter.deleteTableRow(conn, schema, tableName, pks, values, true);

					// once again try to insert new row
					pstmt.execute();

					// refresh
					SQLTableStatistics.clearRowCountCache(schema);
				} else if (errorMessage.contains("ERROR: value too long for type character varying")) {
					System.err.println("SQLTableInserter.insertRow,  table " + tableName
							+ ": Postgresql has problem with the length of a column - manual curation is required! Error message: "
							+ errorMessage);
					throw e;
				} else
					throw e;
			} else
				throw e;
		} finally {
			pstmt.close();
		}
	}

	/**
	 * input syntax:
	 * 
	 * ERROR: duplicate key value violates unique constraint "tablename_pkey"
	 * Detail: Key ("pk1", "pk2")=(v1, v2) already exists.
	 * 
	 * @param error
	 * @param valuesInsteadOfKeys
	 * @return
	 */
	private static List<String> getPrimaryKeysFromErrorMessage(String error, boolean valuesInsteadOfKeys) {
		if (error == null)
			return null;

		String e = String.valueOf(error);
		e = e.substring(e.indexOf("Detail: Key ") + 12, e.length());
		e = e.substring(0, e.indexOf(" already exists")).trim();

		int index = 0;
		if (valuesInsteadOfKeys)
			index = e.indexOf("(", 1);

		String tokens = e.substring(index + 1, e.indexOf(")", index)).trim();

		List<String> ret = new ArrayList<>();
		while (tokens.contains(",")) {
			String t = tokens.substring(0, tokens.indexOf(",")).trim();
			t = t.replace("\"", ""); // error message comes with " escape quotes
			ret.add(t);
			tokens = tokens.substring(tokens.indexOf(",") + 1, tokens.length()).trim();
		}

		String t = tokens;
		t = t.replace("\"", ""); // error message comes with " escape quotes
		ret.add(t);

		return ret;
	}

	/**
	 * hint: check if table exists
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param insertType    INSERT, INSERT IGNORE, REPLACE
	 * @param keyValuePairs contains column and value information for this row
	 */
	public static PreparedStatement insertRowPreparedStatement(Connection conn, String schema, String tableName,
			String insertType, LinkedHashMap<String, Object> keyValuePairs, boolean useFloatInsteadOfDouble)
			throws SQLException {

		boolean postgreSQL = PostgreSQL.isPostgreSQLConnection(conn);

		PreparedStatement pstmt = null;

		String sql = getInsertSQL(conn, schema, tableName, insertType, keyValuePairs, true, postgreSQL);

		pstmt = conn.prepareStatement(sql);

		rowToPreparedStatement(pstmt, keyValuePairs, useFloatInsteadOfDouble, postgreSQL);

		return pstmt;
	}

	/**
	 * Binds a map of Java objects to a PreparedStatement in column order.
	 * 
	 * Functionally identical to the previous combination of
	 * SQLUtils.classToSQLType() + rowToPreparedStatement(), but avoids redundant
	 * type-string construction.
	 *
	 * Behavior and output remain unchanged.
	 */
	private static void rowToPreparedStatement(PreparedStatement pstmt, LinkedHashMap<String, Object> keyValuePairs,
			boolean useFloatInsteadOfDouble, boolean postgreSQL) throws SQLException {
		int index = 1;

		for (Object value : keyValuePairs.values()) {

			// --- Null handling (unchanged)
			if (value == null) {
				pstmt.setNull(index++, Types.NULL);
				continue;
			}
			if (value instanceof Double && Double.isNaN((double) value)) {
				pstmt.setNull(index++, Types.NULL);
				continue;
			}

			// Handle Double.NaN or Float.NaN by inserting NULL
			if ((value instanceof Double && ((Double) value).isNaN())
					|| (value instanceof Float && ((Float) value).isNaN())) {
				pstmt.setNull(index++, Types.NULL);
				continue;
			}

			// === type-safe binding ===
			if (value instanceof Boolean) {
				pstmt.setBoolean(index++, (Boolean) value);
			} else if (value instanceof Byte) {
				pstmt.setByte(index++, (Byte) value);
			} else if (value instanceof Short) {
				pstmt.setShort(index++, (Short) value);
			} else if (value instanceof Integer) {
				pstmt.setInt(index++, (Integer) value);
			} else if (value instanceof Long) {
				pstmt.setLong(index++, (Long) value);
			} else if (value instanceof Float) {
				pstmt.setFloat(index++, (Float) value);
			} else if (value instanceof Double) {
				if (useFloatInsteadOfDouble)
					pstmt.setFloat(index++, ((Double) value).floatValue());
				else
					pstmt.setDouble(index++, (Double) value);
			} else if (value instanceof BigDecimal) {
				pstmt.setBigDecimal(index++, (BigDecimal) value);
			} else if (value instanceof java.sql.Date) {
				pstmt.setDate(index++, (java.sql.Date) value);
			} else if (value instanceof java.util.Date) {
				pstmt.setDate(index++, new java.sql.Date(((Date) value).getTime()));
			} else if (value instanceof java.sql.Timestamp) {
				pstmt.setTimestamp(index++, (java.sql.Timestamp) value);
			} else if (value instanceof byte[]) {
				pstmt.setBytes(index++, (byte[]) value);
			} else if (value instanceof String) {
				pstmt.setString(index++, (String) value);
			} else {
				System.err.println("SQLTableInserter: unknown SQL type for class " + value.getClass().getName());
				pstmt.setString(index++, value.toString());
			}
		}
	}

	private static final Map<String, String> SQL_TEMPLATE_CACHE = new ConcurrentHashMap<>();

	private static String getInsertSQL(Connection conn, String schema, String tableName, String insertType,
			LinkedHashMap<String, Object> sample, boolean preparedStatement, boolean postgreSQL) {
		String key = (schema + "." + tableName + "#" + insertType + "#" + sample.keySet().hashCode() + "#"
				+ postgreSQL);
		return SQL_TEMPLATE_CACHE.computeIfAbsent(key, k -> {
			try {
				return createInsertRowString(conn, schema, tableName, insertType, sample, preparedStatement,
						postgreSQL);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		});
	}

	/**
	 * Builds an SQL INSERT / REPLACE statement string.
	 *
	 * For MySQL: REPLACE INTO ... For PostgreSQL: INSERT ... ON CONFLICT (...) DO
	 * UPDATE ...
	 *
	 * @param conn              JDBC connection
	 * @param tableName         table name (without schema)
	 * @param insertType        "INSERT", "INSERT IGNORE", or "REPLACE"
	 * @param keyValuePairs     column/value map for a single row
	 * @param preparedStatement true to use ? place holders
	 * @return the SQL string ready for PreparedStatement preparation
	 * @throws SQLException
	 */
	private static String createInsertRowString(Connection conn, String schema, String tableName, String insertType,
			LinkedHashMap<String, Object> keyValuePairs, boolean preparedStatement, boolean postgreSQL)
			throws SQLException {

		// ---------- MySQL behavior ----------
		if (!postgreSQL) {
			StringBuilder sql = new StringBuilder();

			sql.append(insertType).append(" INTO `").append(tableName).append("`");

			// column list
			String cols = keyValuePairs.keySet().stream().map(c -> "`" + c + "`")
					.collect(Collectors.joining(",", "(", ")"));
			sql.append(cols);

			// values
			String vals = keyValuePairs.keySet().stream()
					.map(c -> preparedStatement ? "?" : formatValueString(keyValuePairs.get(c)))
					.collect(Collectors.joining(",", " VALUES (", ")"));
			sql.append(vals);
			return sql.toString();
		}

		// ---------- PostgreSQL behavior ----------
		List<String> primaryKeysForTable = SQLTableStatistics.primaryKeysForTable(conn, schema, tableName);

		String fullTable = (schema == null || schema.isEmpty()) ? "\"" + tableName + "\""
				: "\"" + schema + "\".\"" + tableName + "\"";

		// Column list
		String columnList = keyValuePairs.keySet().stream().map(c -> "\"" + c + "\"")
				.collect(Collectors.joining(", ", "(", ")"));

		// Place holders or literal values
		String valuesList = keyValuePairs.keySet().stream()
				.map(c -> preparedStatement ? "?" : formatValueString(keyValuePairs.get(c)))
				.collect(Collectors.joining(", ", " VALUES (", ")"));

		// Base SQL
		StringBuilder sql = new StringBuilder("INSERT INTO ");
		sql.append(fullTable).append(" ").append(columnList).append(valuesList);

		// Decide behavior based on insertType
		if ("INSERT".equalsIgnoreCase(insertType)) {
			// Strict insert -- same as MySQL INSERT (throw error on conflict)
			// to do nothing extra
			return sql.toString();

		} else if ("INSERT IGNORE".equalsIgnoreCase(insertType)) {
			// Ignore duplicates -- ON CONFLICT DO NOTHING
			if (primaryKeysForTable != null && !primaryKeysForTable.isEmpty()) {
				String conflictCols = primaryKeysForTable.stream().map(pk -> "\"" + pk + "\"")
						.collect(Collectors.joining(", ", "(", ")"));
				sql.append(" ON CONFLICT ").append(conflictCols).append(" DO NOTHING");
			} else {
				System.err.println("SQLTableInserter: no primary key detected, falling back to simple INSERT");
			}

			return sql.toString();

		} else if ("REPLACE".equalsIgnoreCase(insertType)) {
			// Emulate MySQL REPLACE with ON CONFLICT DO UPDATE
			if (primaryKeysForTable != null && !primaryKeysForTable.isEmpty()) {
				String conflictCols = primaryKeysForTable.stream().map(pk -> "\"" + pk + "\"")
						.collect(Collectors.joining(", ", "(", ")"));

				String updateCols = keyValuePairs.keySet().stream().filter(col -> !primaryKeysForTable.contains(col))
						.map(col -> "\"" + col + "\" = EXCLUDED.\"" + col + "\"").collect(Collectors.joining(", "));

				if (updateCols.isEmpty()) {
					sql.append(" ON CONFLICT ").append(conflictCols).append(" DO NOTHING");
				} else {
					sql.append(" ON CONFLICT ").append(conflictCols).append(" DO UPDATE SET ").append(updateCols);
				}
			} else {
				System.err.println("SQLTableInserter: no primary key detected, falling back to simple INSERT");
			}
			return sql.toString();
		}

		// Unknown insertType to fallback
		return sql.toString();
	}

	/**
	 * hint: check if table exists
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param insertType INSERT, INSERT IGNORE, REPLACE
	 * @param attributes
	 * @param values
	 */
	public static void insertColumnWise(Connection conn, String schema, String tableName, String insertType,
			List<String> attributes, List<List<Object>> values) throws SQLException {

		Statement stmt = null;
		stmt = conn.createStatement();
		String sql = insertColumnWiseString(tableName, insertType, attributes, values,
				PostgreSQL.isPostgreSQLConnection(conn));

		stmt.executeUpdate(sql);
		stmt.close();

		// refresh
		SQLTableStatistics.clearRowCountCache(schema);
	}

	/**
	 * 
	 * @param tableName
	 * @param insertType INSERT, INSERT IGNORE, REPLACE
	 * @param attributes
	 * @param values     outer list is the rows, inner list the attribute values
	 * @param postgreSQL
	 * @return
	 */
	public static String insertColumnWiseString(String tableName, String insertType, List<String> attributes,
			List<List<Object>> values, boolean postgreSQL) {
		String sql = insertType + " INTO `" + tableName + "`";

		// add the attributes
		String columns = "(";
		for (String attribute : attributes)
			columns += ("`" + attribute + "`,");
		columns = columns.substring(0, columns.length() - 1);
		columns += ")";
		sql += columns;

		String rows = "VALUES";

		// add the list of value rows
		for (List<Object> row : values) {
			rows += "(";
			for (Object value : row)
				rows += (formatValueString(value) + ",");
			rows = rows.substring(0, rows.length() - 1);
			rows += "),";
		}

		rows = rows.substring(0, rows.length() - 1);
		sql += rows;

		if (postgreSQL)
			sql = PostgreSQL.replaceMySQLQuotes(sql);

		return sql;
	}

	private static String formatValueString(Object object) {
		if (object == null)
			return "NULL";
		else if (object instanceof Double && Double.isNaN((double) object)) {
			return "NULL";
		} else if (object instanceof Date) {
			Object o = dateFormat.format((Date) object);
			return "'" + o + "'";
		} else if (object instanceof Boolean)
			// return "'" + (((Boolean) object) == true) != null ? "1" : "0" + "'";
			return ((Boolean) object) ? "'1'" : "'0'";
		else if (object instanceof String)
			return "'" + ((String) object).replace("'", "''") + "'";
		else
			return "'" + object + "'";
	}

}
