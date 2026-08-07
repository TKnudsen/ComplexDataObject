package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

/**
 * <p>
 * Selects data from SQL tables for MySQL and PostgreSQL connections and
 * converts the resulting rows into {@link ComplexDataObject} instances.
 * Offers selection of whole tables or filtered subsets via WHERE clauses,
 * retrieval of table/column metadata, and row counting (either an exact
 * COUNT(*) or a fast PostgreSQL reltuples-based estimate).
 * </p>
 */
public class SQLTableSelector {

	/**
	 * if floats shall replace double. ugly solution to avoid propagating the
	 * parameter through hundreds of methods calling SQLUtils.interpreteResultSet.
	 */
	public static boolean doubleAsFloat = true;

	public enum Order {
		DESC, ASC
	}

	/**
	 * 
	 * @param conn
	 * @param schema         where the table lives in
	 * @param tableName
	 * @param orderAttribute can be null, then order will be ignored as well
	 * @param order
	 * @return List of ComplexDataObjects, parameterized with size such that the
	 *         internal (Hash)Map does not need to be modified much.
	 * @throws SQLException
	 */
	public static List<ComplexDataObject> selectAllFromTable(Connection conn, String schema, String tableName,
			String orderAttribute, Order order) throws SQLException {

		return selectAllFromTable(conn, schema, tableName, orderAttribute, order, null);
	}

	/**
	 * 
	 * @param conn
	 * @param schema                    where the table lives in
	 * @param tableName
	 * @param orderAttribute            can be null, then order will be ignored as
	 *                                  well
	 * @param order
	 * @param attributeCharacterization attributes with type information that are
	 *                                  requested
	 * @return List of ComplexDataObjects, parameterized with size such that the
	 *         internal (Hash)Map does not need to be modified much.
	 * @throws SQLException
	 * 
	 */
	public static List<ComplexDataObject> selectAllFromTable(Connection conn, String schema, String tableName,
			String orderAttribute, Order order, Map<String, Class<?>> attributeCharacterization) throws SQLException {

		long l = System.currentTimeMillis();

		PreparedStatement preparedStatement = selectAllFromTablePreparedStatement(conn, schema, tableName,
				orderAttribute, order);
		ResultSet resultSet = null;
		try {
			resultSet = preparedStatement.executeQuery();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		List<ComplexDataObject> result = new ArrayList<ComplexDataObject>();
		// check: both methods create ComplexDataObjects with Map sizes
		if (attributeCharacterization != null)
			result.addAll(SQLUtils.interpreteResultSet(resultSet, attributeCharacterization));
		else
			result.addAll(SQLUtils.interpreteResultSet(resultSet, doubleAsFloat));

		if (resultSet != null)
			resultSet.close();
		if (preparedStatement != null)
			preparedStatement.close();

		// Single atomic write -- printing the "selecting from table ..." prefix
		// separately (before the query ran) let a concurrent caller's own
		// println land in between the two calls, splicing unrelated log lines
		// together once queries started running in parallel across threads.
		System.out.println("SQLTableSelector.selectAllFromTable: selected all rows from table " + tableName
				+ " in " + (System.currentTimeMillis() - l) + " ms");

		return result;
	}

	/**
	 *
	 * @param conn
	 * @param schema         where the table lives in
	 * @param tableName
	 * @param orderAttribute can be null, then order will be ignored as well
	 * @param order
	 * @return
	 * @throws SQLException
	 *
	 */
	public static PreparedStatement selectAllFromTablePreparedStatement(Connection conn, String schema,
			String tableName, String orderAttribute, Order order) throws SQLException {

		boolean postgreSQL = PostgreSQL.isPostgreSQLConnection(conn);

		// here the schema is already part of the connection
		String schemaAndTable = postgreSQL ? PostgreSQL.schemaAndTableName(schema, tableName) : tableName;

		PreparedStatement preparedStatement = null;
		String sql = (orderAttribute == null) ? "SELECT * FROM `" + schemaAndTable + "`"
				: "SELECT * FROM `" + schemaAndTable + "` ORDER BY `" + orderAttribute + "` " + order.name();

		if (postgreSQL)
			sql = PostgreSQL.replaceMySQLQuotes(sql);

		preparedStatement = conn.prepareStatement(sql);

		return preparedStatement;
	}

	/**
	 * 
	 * @param conn
	 * @param schema         where the table lives in
	 * @param tableName
	 * @param where          the WHERE condition (without WHERE). can be null.
	 *                       example a) column name greater or equals'2012-12-25
	 *                       00:00:00'. b) searchColumn equals 'searchQuery'. Make
	 *                       sure to use ' where needed
	 * @param orderAttribute can be null, then order will be ignored
	 * @param order
	 * @return List of ComplexDataObjects, parameterized with size such that the
	 *         internal (Hash)Map does not need to be modified much.
	 * @throws SQLException
	 */
	public static List<ComplexDataObject> selectFromTableWhere(Connection conn, String schema, String tableName,
			String where, String orderAttribute, Order order) throws SQLException {

		return selectFromTableWhere(conn, schema, tableName, where, orderAttribute, order, null);
	}

	/**
	 * 
	 * @param conn
	 * @param schema                    name of the schema where the table lives in
	 * @param tableName
	 * @param where                     the WHERE condition (without WHERE). can be
	 *                                  null. example a) column name greater or
	 *                                  equals'2012-12-25 00:00:00'. b) searchColumn
	 *                                  equals 'searchQuery'. Make sure to use '
	 *                                  where needed
	 * @param orderAttribute            can be null, then order will be ignored
	 * @param order
	 * @param attributeCharacterization the target schema that is selected from the
	 *                                  database
	 * @return List of ComplexDataObjects, parameterized with size such that the
	 *         internal (Hash)Map does not need to be modified much.
	 * @throws SQLException
	 */
	public static List<ComplexDataObject> selectFromTableWhere(Connection conn, String schema, String tableName,
			String where, String orderAttribute, Order order, Map<String, Class<?>> attributeCharacterization)
			throws SQLException {
		return selectFromTableWhere(conn, schema, tableName, null, where, orderAttribute, order,
				attributeCharacterization);
	}

	/**
	 * 
	 * @param conn
	 * @param tableName
	 * @param columns                   set of columns to be queried
	 * @param where                     the WHERE condition (without WHERE). has
	 *                                  problems with null. example a) column name
	 *                                  greater or equals'2012-12-25 00:00:00'. b)
	 *                                  searchColumn equals 'searchQuery'. Make sure
	 *                                  to use ' where needed
	 * @param orderAttribute            can be null, then order will be ignored
	 * @param order
	 * @param attributeCharacterization the target schema that is selected from the
	 *                                  database
	 * @return List of ComplexDataObjects, parameterized with size such that the
	 *         internal (Hash)Map does not need to be modified much.
	 * @throws SQLException
	 */
	public static List<ComplexDataObject> selectFromTableWhere(Connection conn, String schema, String tableName,
			List<String> columns, String where, String orderAttribute, Order order,
			Map<String, Class<?>> attributeCharacterization) throws SQLException {

		Objects.requireNonNull(schema);
		Objects.requireNonNull(tableName);

		boolean postgreSQL = PostgreSQL.isPostgreSQLConnection(conn);

		long l = System.currentTimeMillis();

		String fromString = columns == null ? "*" : "";
		if (columns != null && !columns.isEmpty()) {
			for (String column : columns)
				if (postgreSQL)
					fromString += (PostgreSQL.quotationsForAttribute(column) + ", ");
				else
					fromString += (column + ", ");
			fromString = fromString.substring(0, fromString.length() - 2);
		}

		String schemaAndTable = postgreSQL ? PostgreSQL.schemaAndTableName(schema, tableName) : tableName;
		// here the schema is already part of the connection

		List<ComplexDataObject> result = new ArrayList<ComplexDataObject>();

		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			String sql = (orderAttribute == null) ? "SELECT * FROM `" + schemaAndTable + "` WHERE PLACEHOLDER"
					: "SELECT " + fromString + " FROM `" + schemaAndTable + "` WHERE PLACEHOLDER ORDER BY `"
							+ orderAttribute + "` " + order.name();

			if (postgreSQL)
				sql = PostgreSQL.replaceMySQLQuotes(sql);

			// the search string needs to be postgreSQL conform, values may have the other
			// escape string in use (')
			String ss = PostgreSQL.replaceMySQLQuotes(where);
			sql = sql.replace("PLACEHOLDER", ss);

			preparedStatement = conn.prepareStatement(sql);

			// === Indexing ===
			SQLTableIndex.ensureIndexForWhereClause(conn, schema, tableName, where, orderAttribute);

			resultSet = preparedStatement.executeQuery();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(),
					e.getMessage() + " Schema: " + schema + ", Table: " + tableName);
			e.printStackTrace();
			if (e.getMessage().contains("does not exist")) {
			} else
				throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		if (attributeCharacterization != null)
			// check: both methods create ComplexDataObjects with Map sizes
			result.addAll(SQLUtils.interpreteResultSet(resultSet, attributeCharacterization));
		else
			result.addAll(SQLUtils.interpreteResultSet(resultSet, doubleAsFloat));

		if (resultSet != null)
			resultSet.close();
		if (preparedStatement != null)
			preparedStatement.close();

		// Single atomic write -- see selectAllFromTable's identical comment.
		System.out.println("SQLTableSelector.selectFromTableWhere: selected all rows from table " + tableName
				+ " in " + (System.currentTimeMillis() - l) + " ms");

		return result;
	}

	/**
	 *
	 * @param conn
	 * @param schema         where the table lives in
	 * @param tableName
	 * @param columns
	 * @param where          the WHERE condition (without WHERE). can be null.
	 *                       example a) column name greater or equals'2012-12-25
	 *                       00:00:00'. b) searchColumn equals 'searchQuery'. Make
	 *                       sure to use ' where needed
	 *
	 * @param orderAttribute
	 * @param order
	 * @return inner list to iterate over column indices per row element
	 * @throws SQLException
	 */
	public static Collection<List<Object>> selectColumnsFromTable(Connection conn, String schema, String tableName,
			List<String> columns, String where, String orderAttribute, Order order) throws SQLException {

		Objects.requireNonNull(tableName);

		boolean postgreSQL = PostgreSQL.isPostgreSQLConnection(conn);

		// here the schema is already part of the connection

		String schemaAndTable = postgreSQL ? PostgreSQL.schemaAndTableName(schema, tableName) : tableName;

		// prepare query
		String query = "SELECT ";
		if (columns != null && !columns.isEmpty()) {
			for (String column : columns)
				if (postgreSQL)
					query += (PostgreSQL.quotationsForAttribute(column) + ",");
				else
					query += "`" + column + "`,";
			query = query.substring(0, query.length() - 1);
		} else
			query += "*";
		query += " FROM `" + schemaAndTable + "`";

		if (where != null)
			query += (" WHERE " + where);

		if (orderAttribute != null)
			query += (" ORDER BY `" + orderAttribute + "` " + order.name());

		if (postgreSQL)
			query = PostgreSQL.replaceMySQLQuotes(query);

		// === Indexing ===
		SQLTableIndex.ensureIndexForWhereClause(conn, schema, tableName, where, orderAttribute);

		// === Execute and interpret ===
		try (Statement stmt = conn.createStatement(); ResultSet resultSet = stmt.executeQuery(query.toString())) {

			if (resultSet == null)
				throw new SQLException("Query returned no ResultSet for table " + tableName);

			ResultSetInterpreter.ResultSetDescriptor desc = ResultSetInterpreter.createExtractors(resultSet, false);

			Collection<List<Object>> values = new ArrayList<>();

			while (resultSet.next()) {
				LinkedHashMap<String, Object> map = ResultSetInterpreter.interpreteResultSetRow(resultSet,
						desc.getColumnNames(), desc.getExtractors());
				values.add(new ArrayList<>(map.values()));
			}

			return values;
		}
	}

	/**
	 * selects all tables in a database for a given schema
	 * 
	 * @param conn
	 * @param schema
	 * @return
	 */
	public static Collection<String> tableNames(Connection conn, String schema) throws SQLException {

		Collection<String> tables = new ArrayList<>();

		PreparedStatement preparedStatement = null;
		try {
			String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = '" + schema + "'";

			if (PostgreSQL.isPostgreSQLConnection(conn))
				sql = PostgreSQL.replaceMySQLQuotes(sql);

			preparedStatement = conn.prepareStatement(sql);

			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				tables.add(resultSet.getString("TABLE_NAME"));
			}
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (preparedStatement != null)
				try {
					preparedStatement.close();
				} catch (SQLException e) {
				}
		}

		return tables;
	}

	public static List<String> columnNames(Connection conn, String schema, String tableName) throws SQLException {

		List<String> columns = new ArrayList<>();

		DatabaseMetaData metadata = conn.getMetaData();

		ResultSet resultSet = metadata.getColumns(conn.getCatalog(), schema, tableName, null);
		while (resultSet.next()) {
			String name = resultSet.getString("COLUMN_NAME");
			columns.add(name);
		}

		if (columns.isEmpty()) {
			resultSet = metadata.getColumns(conn.getCatalog(), schema, tableName.toLowerCase(), null);
			while (resultSet.next()) {
				String name = resultSet.getString("COLUMN_NAME");
				columns.add(name);
			}
		}
		return columns;
	}

	/**
	 * Returns the row count for the given table using PostgreSQL's
	 * {@code reltuples} statistic as a fast estimate. Equivalent to calling
	 * {@link #countRows(Connection, String, String, boolean, boolean)} with
	 * {@code printTiming = false} and {@code exactRequired = false}.
	 *
	 * <p>
	 * <b>Note:</b> the returned value may differ from the true row count if
	 * {@code ANALYZE} has not been run recently. Use
	 * {@link #countRows(Connection, String, String, boolean, boolean)} with
	 * {@code exactRequired = true} whenever an exact count is needed.
	 *
	 * @param conn      active JDBC connection
	 * @param schema    schema name
	 * @param tableName table name
	 * @return estimated row count as {@code long}; never negative
	 * @throws SQLException if the table does not exist or a DB error occurs
	 */
	public static long countRows(Connection conn, String schema, String tableName) throws SQLException {
		return countRows(conn, schema, tableName, false, false);
	}

	/**
	 * Returns the row count for the given table, either as a fast {@code reltuples}
	 * estimate or as an exact {@code COUNT(*)} result, depending on
	 * {@code exactRequired}.
	 *
	 * <h3>Estimate path ({@code exactRequired = false})</h3>
	 * <p>
	 * Reads {@code pg_class.reltuples}, which is maintained by {@code ANALYZE} and
	 * autovacuum. This is a single catalog lookup with no table scan. If
	 * {@code reltuples = -1} (table never analyzed), {@code ANALYZE} is triggered
	 * automatically and the value is re-read. If it is still {@code -1} after that,
	 * the method falls back to an exact {@code COUNT(*)}.
	 *
	 * <h3>Exact path ({@code exactRequired = true})</h3>
	 * <p>
	 * Skips {@code reltuples} entirely and executes {@code COUNT(*)} directly. This
	 * always reflects the current committed state of the table but requires a full
	 * sequential scan and is therefore slower on large tables.
	 *
	 * <p>
	 * Both paths return {@code long} to correctly represent tables with more than
	 * {@link Integer#MAX_VALUE} rows without overflow or truncation.
	 *
	 * @param conn          active JDBC connection
	 * @param schema        schema name
	 * @param tableName     table name
	 * @param printTiming   if {@code true}, prints elapsed time to stdout
	 * @param exactRequired if {@code true}, bypasses the {@code reltuples} estimate
	 *                      and always executes an exact {@code COUNT(*)}
	 * @return row count as {@code long} -- exact when {@code exactRequired = true},
	 *         estimated (or exact as a fallback) otherwise; never negative
	 * @throws SQLException if the table does not exist or a DB error occurs
	 */
	public static long countRows(Connection conn, String schema, String tableName, boolean printTiming,
			boolean exactRequired) throws SQLException {

		if (printTiming)
			System.out.print("SQLTableSelector: countRows on " + schema + "." + tableName + " ...");
		long t = System.currentTimeMillis();

		if (!SQLUtils.tableExists(conn, schema, tableName))
			throw new SQLException("Table does not exist: \"" + schema + "\".\"" + tableName + "\"");

		if (exactRequired) {
			long exact = exactCountRows(conn, schema, tableName);
			if (printTiming)
				System.out.println("done (exact) in " + (System.currentTimeMillis() - t) + " ms");
			return exact;
		}

		long rows = -1L;

		String sql = "SELECT GREATEST(reltuples::bigint, -1) FROM pg_class c "
				+ "JOIN pg_namespace n ON n.oid = c.relnamespace " + "WHERE n.nspname = '" + schema
				+ "' AND c.relname = '" + tableName + "'";

		Statement stmt = conn.createStatement();
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery(sql);
			if (rs.next())
				rows = rs.getLong(1);
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					/* ignore */ }
			stmt.close();
		}

		if (rows == -1L) {
			System.out.println(
					"SQLTableStatistics: no reltuples for " + schema + "." + tableName + "  -  running ANALYZE.");
			Statement analyzeStmt = conn.createStatement();
			try {
				analyzeStmt.execute("ANALYZE \"" + schema + "\".\"" + tableName + "\"");
			} catch (SQLException e) {
				System.err.println("SQLTableStatistics: ANALYZE failed for " + schema + "." + tableName + "  -  "
						+ e.getMessage());
			} finally {
				analyzeStmt.close();
			}

			stmt = conn.createStatement();
			rs = null;
			try {
				rs = stmt.executeQuery(sql);
				if (rs.next())
					rows = rs.getLong(1);
			} finally {
				if (rs != null)
					try {
						rs.close();
					} catch (SQLException e) {
						/* ignore */ }
				stmt.close();
			}

			if (rows == -1L) {
				System.err.println("SQLTableStatistics: reltuples still unavailable after ANALYZE for " + schema + "."
						+ tableName + "  -  falling back to COUNT(*).");
				rows = exactCountRows(conn, schema, tableName);
			}
		}

		if (printTiming)
			System.out.println("SQLTableSelector: countRows done in " + (System.currentTimeMillis() - t) + " ms");

		return rows;
	}

	/**
	 * Executes an exact {@code COUNT(*)} against the given table and returns the
	 * result as {@code long}. No catalog estimates are consulted; the count always
	 * reflects the current committed state of the table.
	 *
	 * <p>
	 * Uses {@link ResultSet#getLong(int)} to correctly handle PostgreSQL's
	 * {@code bigint} return type of {@code COUNT(*)}, avoiding silent truncation
	 * that would occur with {@link ResultSet#getInt(int)} for tables exceeding
	 * {@link Integer#MAX_VALUE} rows.
	 *
	 * <p>
	 * This is the single authoritative implementation of exact row counting used by
	 * both {@link #countRows(Connection, String, String, boolean, boolean)} (as a
	 * last resort on the estimate path) and
	 * {@link SQLTableStatistics#rowCount(Connection, String, String, boolean)}
	 * (when {@code exactRequired = true}).
	 *
	 * @param conn      active JDBC connection
	 * @param schema    schema name
	 * @param tableName table name
	 * @return exact row count as {@code long}; {@code 0L} if the table is empty
	 * @throws SQLException on DB error
	 */
	private static long exactCountRows(Connection conn, String schema, String tableName) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM \"" + schema + "\".\"" + tableName + "\"");
		ResultSet rs = null;
		try {
			rs = ps.executeQuery();
			return rs.next() ? rs.getLong(1) : 0L;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					/* ignore */ }
			ps.close();
		}
	}
}
