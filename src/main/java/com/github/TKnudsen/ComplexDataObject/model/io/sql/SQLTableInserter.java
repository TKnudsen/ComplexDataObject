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
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.model.tools.Threads;

public class SQLTableInserter {

	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param insertType
	 * @param listOfMapWithKeyValuePairs
	 * @param extendColumnCapacityIfNeeded
	 * @param showTimingLog
	 * @throws SQLException
	 */
	public static void insertRows(Connection conn, String schema, String tableName, String insertType,
			List<LinkedHashMap<String, Object>> listOfMapWithKeyValuePairs, boolean extendColumnCapacityIfNeeded,
			boolean useFloatInsteadOfDouble, boolean showTimingLog) throws SQLException {

		try {
			insertRows(conn, schema, tableName, insertType, listOfMapWithKeyValuePairs, useFloatInsteadOfDouble,
					showTimingLog);
		} catch (DataTruncation e) {
			if (!extendColumnCapacityIfNeeded)
				throw new IllegalArgumentException(
						"SQLTableInserter: caught a data truncation exception with message " + e.getMessage());

			String column = SQLUtils.mitigateDataTruncationError(conn, e, listOfMapWithKeyValuePairs, schema,
					tableName);
			// TODO risk of an infinite loop!
			System.err.println("SQLTableInserter: data truncation error detected and column " + column
					+ " extended respectively, re-trying to insertRows");
			insertRows(conn, schema, tableName, insertType, listOfMapWithKeyValuePairs, extendColumnCapacityIfNeeded,
					showTimingLog);
		}
	}

	/**
	 * hint: check if table exists first.
	 * 
	 * hint: check if missing columns need to be added first using
	 * SQLTableCreator.addMissingColumns(...)
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param insertType                 INSERT, INSERT IGNORE, REPLACE
	 * @param listOfMapWithKeyValuePairs list means rows, map contains the key value
	 *                                   pairs for every row. Attention: modifies
	 *                                   the given object if attributes are not in
	 *                                   table
	 */
	private static void insertRows(Connection conn, String schema, String tableName, String insertType,
			List<LinkedHashMap<String, Object>> listOfMapWithKeyValuePairs, boolean useFloatInsteadOfDouble,
			boolean showTimingLog) throws SQLException {

		Objects.requireNonNull(conn);
		Objects.requireNonNull(listOfMapWithKeyValuePairs);

		if (showTimingLog)
			System.out.print("SQLTableInserter.insertRows: inserting multiple rows in table " + tableName + "...");
		long l = System.currentTimeMillis();

		List<String> columns = SQLTableSelector.columnNames(conn, schema, tableName);

		// remove attributes that cannot be inserted in columns
		for (LinkedHashMap<String, Object> row : listOfMapWithKeyValuePairs)
			for (String attribute : row.keySet())
				if (!columns.contains(attribute))
					row.remove(attribute);

		int i = 0;
		for (LinkedHashMap<String, Object> keyValuePairs : listOfMapWithKeyValuePairs) {
			if (showTimingLog)
				if (i++ % 100 == 0)
					if (i > 1)
						System.out.print("[" + (i - 1) + "]");

			insertRow(conn, schema, tableName, insertType, keyValuePairs, useFloatInsteadOfDouble);
		}
		if (showTimingLog)
			System.out.print("[" + (i) + "] ");

		if (showTimingLog)
			System.out.println("done in " + (System.currentTimeMillis() - l) + " ms");
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

		boolean postgreSQL = PostgreSQL.isPostgreSQLConnection(conn);
		String insertTypeInternal = insertType;
		if (insertType.equals("REPLACE") && postgreSQL)
			insertTypeInternal = "INSERT";

		PreparedStatement pstmt = insertRowPreparedStatement(conn, schema, tableName, insertTypeInternal, keyValuePairs,
				useFloatInsteadOfDouble);

		if (pstmt == null)
			return;

		try {
			pstmt.execute();
		} catch (Exception e) { // postgreSQL does not support REPLACE INTO. workaround...
			if (e.getClass().getSimpleName().equals("PSQLException") && insertType.equals("REPLACE")) {
				String errorMessage = e.getMessage();
				if (errorMessage.contains("duplicate key value violates unique constraint")) {
					List<String> pks = getPrimaryKeysFromErrorMessage(errorMessage, false);
					List<String> values = getPrimaryKeysFromErrorMessage(errorMessage, true);

					// delete original row
					// wait a bit, a lot of exceptions like these have happened in the past, needs
					// refactoring on the long run:
					// "An I/O error occurred while sending to the backend"
					// java.net.SocketException: An established connection was aborted by the
					// software in your host machine
					Threads.sleep(5);
					SQLTableDeleter.deleteTableRow(conn, PostgreSQL.schemaAndTableName(schema, tableName), pks, values);

					// once again try to insert new row
					pstmt.execute();
				}
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
		PreparedStatement pstmt = null;

		boolean postgreSQL = PostgreSQL.isPostgreSQLConnection(conn);

		if (!postgreSQL) {
			String sql = insertRowString(conn, tableName, insertType, keyValuePairs, true);

			pstmt = conn.prepareStatement(sql);

			rowToPreparedStatement(pstmt, keyValuePairs, useFloatInsteadOfDouble, postgreSQL);

			return pstmt;
		} else {
			String sqlString = insertRowString(conn, PostgreSQL.schemaAndTableName(schema, tableName), insertType,
					keyValuePairs, true);

			pstmt = conn.prepareStatement(sqlString);

			rowToPreparedStatement(pstmt, keyValuePairs, useFloatInsteadOfDouble, postgreSQL);

			return pstmt;
		}
	}

	private static void addKeyValuePairToPreparedStatement(PreparedStatement pstmt, int index, Object value)
			throws SQLException {
		switch (value.getClass().getSimpleName().toLowerCase()) {
		case "string":
			pstmt.setString(index, (String) value);
			break;
		case "integer":
			pstmt.setInt(index, (Integer) value);
			break;
		case "int":
			pstmt.setInt(index, (Integer) value);
			break;
		case "boolean":
			pstmt.setInt(index, (Integer) value);
			break;
		case "float":
			pstmt.setFloat(index, (Float) value);
			break;
		case "double":
			pstmt.setDouble(index, (Double) value);
			break;
		case "long":
			pstmt.setDouble(index, (Long) value);
			break;
		case "bigdecimal":
			pstmt.setBigDecimal(index, (BigDecimal) value);
			break;
		case "date":
			pstmt.setDate(index, new java.sql.Date(((Date) value).getTime()));
			break;

		default:
			break;
		}
	}

	/**
	 * 
	 * hint: check if table exists
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param insertType    INSERT, INSERT IGNORE, REPLACE
	 * @param keyValuePairs contains column and value information for this row
	 */
	public static String insertRowPreparedStatementString(Connection conn, String schema, String tableName,
			String insertType, LinkedHashMap<String, Object> keyValuePairs, boolean useFloatInsteadOfDouble)
			throws SQLException {

		PreparedStatement pstmt = null;

		String sql = insertRowString(conn, tableName, insertType, keyValuePairs, true);

		pstmt = conn.prepareStatement(sql);

		rowToPreparedStatement(pstmt, keyValuePairs, useFloatInsteadOfDouble, PostgreSQL.isPostgreSQLConnection(conn));

		String pSQL = pstmt.toString();
		// hack: remove the first part before the SQL query starts
		if (pSQL.contains("ClientPreparedStatement: "))
			pSQL = pSQL.substring(pSQL.indexOf("ClientPreparedStatement: ") + 25, pSQL.length());
		else
			throw new NullPointerException(
					"SQLTableInserter.insertRowPreparedStatementString: unable to create SQL String for " + pSQL);

		return pSQL;
	}

	/**
	 * 
	 * @param pstmt
	 * @param keyValuePairs
	 * @throws SQLException
	 */
	private static void rowToPreparedStatement(PreparedStatement pstmt, LinkedHashMap<String, Object> keyValuePairs,
			boolean useFloatInsteadOfDouble, boolean postgreSQL) throws SQLException {
		int index = 1;

		for (String key : keyValuePairs.keySet()) {
			Object value = keyValuePairs.get(key);

//			System.out.println(index + ":\t" + key + " - \t" + value);

			if (value == null)
				pstmt.setNull(index++, Types.NULL);
			else if (value instanceof Double && Double.isNaN((double) value))
				pstmt.setNull(index++, Types.NULL);
			else {
				String type = SQLUtils.classToSQLType(value.getClass(), false, useFloatInsteadOfDouble, postgreSQL);
				switch (type) {
				case "BIT(1)":
					pstmt.setBoolean(index++, (boolean) value);
					break;
				case "BIT(8)":
					pstmt.setByte(index++, (byte) value);
					break;
				case "INT":
					pstmt.setInt(index++, (int) value);
					break;
				case "BIGINT(19)":
					pstmt.setLong(index++, (long) value);
					break;
				case "BIGINT":
					pstmt.setLong(index++, (long) value);
					break;
				case "DATE":
					pstmt.setDate(index++, new java.sql.Date(((Date) value).getTime()));
					break;
				case "FLOAT":
					pstmt.setFloat(index++, (float) value);
					break;
				case "FLOAT4":
					pstmt.setFloat(index++, (float) value);
					break;
				case "DOUBLE": // no need to check useFloatInsteadOfDouble
					pstmt.setDouble(index++, (double) value);
					break;
				case "DECIMAL":
					pstmt.setBigDecimal(index++, (BigDecimal) value);
					break;
				case "BLOB":
					pstmt.setString(index++, (String) value);
					break;
				case "TEXT":
					pstmt.setString(index++, (String) value);
					break;
				case "VARCHAR":
					pstmt.setString(index++, (String) value);
					break;

				default:
					System.out.println("SQLTableInserter: unknown SQL attribute type: " + type);
					break;
				}
			}

//			System.out.println(index + ": " + key + " - " + value);
		}
	}

	/**
	 * 
	 * @param tableName
	 * @param insertType        INSERT, INSERT IGNORE, REPLACE
	 * @param keyValuePairs
	 * @param preparedStatement
	 * @return
	 */
	public static String insertRowString(Connection conn, String tableName, String insertType,
			LinkedHashMap<String, Object> keyValuePairs, boolean preparedStatement) {

		String sql = insertType + " INTO `" + tableName + "`";

		String attributes = "(";

		// add the attributes now
		for (String attribute : keyValuePairs.keySet())
			attributes += ("`" + attribute + "`,");
		attributes = attributes.substring(0, attributes.length() - 1);
		attributes += ")";
		sql += attributes;

		String values = "VALUES(";

		// add the attribute values now
		for (String attribute : keyValuePairs.keySet())
			values += (preparedStatement) ? "?," : (formatValueString(keyValuePairs.get(attribute)) + ",");
		values = values.substring(0, values.length() - 1);
		values += ")";
		sql += values;

		if (PostgreSQL.isPostgreSQLConnection(conn))
			sql = PostgreSQL.replaceMySQLQuotes(sql);

		return sql;
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
			return "'" + (((Boolean) object) == true) != null ? "1" : "0" + "'";
		else if (object instanceof String)
			return "'" + ((String) object).replace("'", "''") + "'";
		else
			return "'" + object + "'";
	}

}
