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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

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
			boolean showTimingLog) throws SQLException {

		try {
			insertRows(conn, schema, tableName, insertType, listOfMapWithKeyValuePairs, showTimingLog);
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
			List<LinkedHashMap<String, Object>> listOfMapWithKeyValuePairs, boolean showTimingLog) throws SQLException {

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

			insertRow(conn, schema, tableName, insertType, keyValuePairs);
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
	 * @param insertType          INSERT, INSERT IGNORE, REPLACE
	 * @param keyValuePairs       contains column and value information for this row
	 * @param checkIfTableExists  to ensure that no exception will be thrown
	 * @param overwriteDuplicates insert strategy. overwriteDuplicates uses replace
	 *                            into instead
	 */
	public static void insertRow(Connection conn, String schema, String tableName, String insertType,
			LinkedHashMap<String, Object> keyValuePairs) throws SQLException {

		PreparedStatement pstmt = insertRowPreparedStatement(conn, schema, tableName, insertType, keyValuePairs);

		if (pstmt == null)
			return;

		pstmt.execute();
		pstmt.close();
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
			String insertType, LinkedHashMap<String, Object> keyValuePairs) throws SQLException {
		PreparedStatement pstmt = null;

		String sql = insertRowString(tableName, insertType, keyValuePairs, true);

		pstmt = conn.prepareStatement(sql);

		rowToPreparedStatement(pstmt, keyValuePairs);

		return pstmt;
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
			String insertType, LinkedHashMap<String, Object> keyValuePairs) throws SQLException {
		PreparedStatement pstmt = null;

		String sql = insertRowString(tableName, insertType, keyValuePairs, true);

		pstmt = conn.prepareStatement(sql);

		rowToPreparedStatement(pstmt, keyValuePairs);

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
	private static void rowToPreparedStatement(PreparedStatement pstmt, LinkedHashMap<String, Object> keyValuePairs)
			throws SQLException {
		int index = 1;

		for (String key : keyValuePairs.keySet()) {
			Object value = keyValuePairs.get(key);

//			System.out.println(index + ":\t" + key + " - \t" + value);

			if (value == null)
				pstmt.setNull(index++, Types.NULL);
			else if (value instanceof Double && Double.isNaN((double) value))
				pstmt.setNull(index++, Types.NULL);
			else {
				String type = SQLUtils.classToMySQLType(value.getClass(), false);
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
				case "DATE":
					pstmt.setDate(index++, new java.sql.Date(((Date) value).getTime()));
					break;
				case "FLOAT":
					pstmt.setFloat(index++, (float) value);
					break;
				case "DOUBLE":
					pstmt.setDouble(index++, (double) value);
					break;
				case "DECIMAL":
					pstmt.setBigDecimal(index++, (BigDecimal) value);
					break;
				case "BLOB":
					pstmt.setString(index++, (String) value);
					break;
				case "VARCHAR":
					pstmt.setString(index++, (String) value);
					break;

				default:
					System.out.println("SQLTableInserter: unknown SQL attribute type");
					break;
				}
			}
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
	public static String insertRowString(String tableName, String insertType,
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

		return sql;
	}

	/**
	 * hint: check if table exists
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param insertType         INSERT, INSERT IGNORE, REPLACE
	 * @param attributes
	 * @param values
	 * @param checkIfTableExists
	 */
	public static void insertColumnWise(Connection conn, String schema, String tableName, String insertType,
			List<String> attributes, List<List<Object>> values) throws SQLException {

		Statement stmt = null;
		stmt = conn.createStatement();
		String sql = insertColumnWiseString(tableName, insertType, attributes, values);
		stmt.executeUpdate(sql);
		stmt.close();
	}

	/**
	 * 
	 * @param tableName
	 * @param insertType          INSERT, INSERT IGNORE, REPLACE
	 * @param attributes
	 * @param values              outer list is the rows, inner list the attribute
	 *                            values
	 * @param overwriteDuplicates
	 * @return
	 */
	public static String insertColumnWiseString(String tableName, String insertType, List<String> attributes,
			List<List<Object>> values) {
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
