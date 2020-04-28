package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SQLTableInserter {

	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param insertType                 INSERT, INSERT IGNORE, REPLACE
	 * @param listOfMapWithKeyValuePairs list means rows, map contains the key value
	 *                                   pairs for every row.
	 * @param addMissingColumns          determines if columns missing in the table
	 *                                   will be inserted automatically (if
	 *                                   possible/type known)
	 */
	public static void insertRows(Connection conn, String schema, String tableName, String insertType,
			List<LinkedHashMap<String, Object>> listOfMapWithKeyValuePairs, boolean checkIfTableExists,
			boolean addMissingColumns) throws SQLException {

		Objects.requireNonNull(conn);
		Objects.requireNonNull(listOfMapWithKeyValuePairs);

		System.out.print("SQLTableInserter.insertRows: inserting multiple rows in table " + tableName + "...");
		long l = System.currentTimeMillis();

		if (checkIfTableExists)
			if (!SQLUtils.tableExists(conn, schema, tableName)) {
				System.err.println("SQLTableInserter.insertRow: Table " + tableName + " in schema " + schema
						+ " does not exist. stop.");
				return;
			}

		if (addMissingColumns) {
			// check whether all attributes are already in the database
			List<String> columns = SQLTableSelector.columnNames(conn, schema, tableName);
			Collections.sort(columns);

			Set<String> insertColumns = new HashSet<>();
			for (LinkedHashMap<String, Object> keyValuePairs : listOfMapWithKeyValuePairs)
				insertColumns.addAll(keyValuePairs.keySet());

			boolean all = false;
			for (String attribute : insertColumns) {
				if (!columns.contains(attribute) && !all) {
					System.err.print("SQLTableInserter.insertRows: Table " + tableName + " in schema " + schema
							+ " does not contain attribute " + attribute + ". Trying to add column... ");

					Class<?> javaClass = null;
					List<Object> values = new ArrayList<Object>();
					for (LinkedHashMap<String, Object> keyValuePairs : listOfMapWithKeyValuePairs) {
						if (!keyValuePairs.containsKey(attribute))
							continue;
						if (javaClass == null)
							javaClass = keyValuePairs.get(attribute).getClass();
						else if (javaClass.equals(keyValuePairs.get(attribute).getClass()))
							throw new IllegalArgumentException("Attribute " + attribute
									+ " not in table, attempt to add column faile because the data was of different types ("
									+ javaClass + " and " + keyValuePairs.get(attribute).getClass() + ").");
						values.add(keyValuePairs.get(attribute));
					}
					SQLTableCreator.addColumn(conn, schema, tableName, attribute, javaClass, values, columns);

					System.err.println("finished without exceptions.");
				}
			}
		} else {
			List<String> columns = SQLTableSelector.columnNames(conn, schema, tableName);

			// remove attributes that cannot be inserted in columns
			for (String attribute : columns)
				for (LinkedHashMap<String, Object> row : listOfMapWithKeyValuePairs)
					if (!row.keySet().contains(attribute))
						row.remove(attribute);
		}

		int i = 0;
		for (LinkedHashMap<String, Object> keyValuePairs : listOfMapWithKeyValuePairs) {
			if (i++ % 100 == 0)
				if (i > 1)
					System.out.print("[" + (i - 1) + "]");

			insertRow(conn, schema, tableName, insertType, keyValuePairs, false);
		}

		System.out.println("done in " + (System.currentTimeMillis() - l) + " ms");
	}

	/**
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
			LinkedHashMap<String, Object> keyValuePairs, boolean checkIfTableExists) throws SQLException {
		PreparedStatement pstmt = null;

		if (checkIfTableExists)
			if (!SQLUtils.tableExists(conn, schema, tableName)) {
				System.err.println("SQLTableInserter.insertRow: Table " + tableName + " in schema " + schema
						+ " does not exist. skip.");
				return;
			}

		String sql = rowToInsertString(tableName, insertType, keyValuePairs, true);

		pstmt = conn.prepareStatement(sql);

		rowToPreparedStatement(pstmt, keyValuePairs);

		pstmt.execute();
		pstmt.close();
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
	private static String rowToInsertString(String tableName, String insertType,
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
			List<String> attributes, List<List<Object>> values, boolean checkIfTableExists) throws SQLException {

		Statement stmt = null;
		if (checkIfTableExists)
			if (!SQLUtils.tableExists(conn, schema, tableName)) {
				System.err.println("SQLTableInserter.insertRow: Table " + tableName + " in schema " + schema
						+ " does not exist. stop.");
				return;
			}

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
	 * @param values
	 * @param overwriteDuplicates
	 * @return
	 */
	private static String insertColumnWiseString(String tableName, String insertType, List<String> attributes,
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
