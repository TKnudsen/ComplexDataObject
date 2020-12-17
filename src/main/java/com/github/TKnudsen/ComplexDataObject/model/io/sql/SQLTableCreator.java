package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class SQLTableCreator {

	public static void createTable(Connection conn, String schema, String tableName,
			Map<String, Class<?>> schemaEntries, List<String> primaryKeyAttributes) throws SQLException {

		createTable(conn, schema, tableName, schemaEntries, null, primaryKeyAttributes);
	}

	/**
	 * 
	 * @param conn
	 * @param schema               important to avoid confusions, e.g., if a table
	 *                             exists in several schemas.
	 * @param tableName
	 * @param schemaEntries
	 * @param valuesFunctions      optional, used to guess the size of columns in
	 *                             the sql table.
	 * @param primaryKeyAttributes
	 * @throws SQLException
	 */
	public static void createTable(Connection conn, String schema, String tableName,
			Map<String, Class<?>> schemaEntries, Function<String, Collection<Object>> valuesFunctions,
			List<String> primaryKeyAttributes) throws SQLException {

		String sqlString = createTableString(tableName, schemaEntries, valuesFunctions, primaryKeyAttributes);

		createTable(conn, schema, tableName, sqlString);
	}

	/**
	 * 
	 * @param conn
	 * @param sqlString SQL string that will create the table
	 * @throws SQLException
	 */
	public static void createTable(Connection conn, String schema, String tableName, String sqlString)
			throws SQLException {

		Statement stmt = null;
		try {
			if (SQLUtils.tableExists(conn, schema, tableName)) {
				System.err.println("Table " + tableName + " in schema " + schema + " already exists - nothing to do.");
				return;
			}

			System.out.print("SQLTableCreator.createTable: Table " + tableName + " in schema " + schema + "...");
			stmt = conn.createStatement();
			stmt.executeUpdate(sqlString);
			System.out.println("done");
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stmt != null)
				stmt.close();
		}
	}

	public static String createTableString(String tableName, Map<String, Class<?>> schemaEntries,
			Function<String, Collection<Object>> attributeValueDistributions, List<String> primaryKeyAttributes) {

		String sql = "CREATE TABLE `" + tableName + "` " + "(";

		List<String> pkAttributes = new ArrayList<String>();
		if (primaryKeyAttributes != null)
			pkAttributes = primaryKeyAttributes;

		for (String entryName : schemaEntries.keySet()) {
			String typeString = "";

			Collection<Object> values = null;
			if (attributeValueDistributions != null)
				try {
					values = attributeValueDistributions.apply(entryName);
				} catch (Exception e) {
				}

			if (values != null)
				typeString = SQLUtils.classToMySQLType(schemaEntries.get(entryName), values,
						pkAttributes.contains(entryName));
			else
				typeString = SQLUtils.classToMySQLType(schemaEntries.get(entryName), pkAttributes.contains(entryName));

			if (pkAttributes.contains(entryName)) {
				sql += ("`" + entryName + "` " + typeString + " not NULL, ");
			} else
				sql += ("`" + entryName + "` " + typeString + ", ");
		}

		if (!pkAttributes.isEmpty()) {
			String primaryKeys = "";
			for (String pk : pkAttributes)
				primaryKeys += ("`" + pk + "`,");
			primaryKeys = primaryKeys.substring(0, primaryKeys.length() - 1);
			sql += " PRIMARY KEY (" + primaryKeys + ")";
		} else
			sql = sql.substring(0, sql.lastIndexOf(", "));

		sql += ")";

		return sql;
	}

	/**
	 * 
	 * @param tableName
	 * @param newColumnName
	 * @param javaClass       used to infer the column type
	 * @param values          used to infer the column type and lengths
	 * @param existingColumns can be used to determine the alphabetically correct
	 *                        position of column insertion.
	 * @throws SQLException
	 */
	public static String addColumnString(String tableName, String newColumnName, Class<?> javaClass,
			Collection<Object> values, Iterable<String> existingColumns) throws SQLException {

		Iterator<String> iterator = existingColumns.iterator();
		String afterAColumnName = null;
		if (iterator.hasNext())
			afterAColumnName = iterator.next(); // ID

		while (iterator.hasNext()) {
			String next = iterator.next();
			if (next.compareTo(newColumnName) < 0)
				afterAColumnName = next;
			else
				break;
		}

		return addColumnString(tableName, newColumnName, javaClass, values, afterAColumnName);
	}

	/**
	 * 
	 * @param tableName
	 * @param columnName
	 * @param javaClass
	 * @param values
	 * @param afterAColumnName can be null
	 * @throws SQLException
	 */
	public static String addColumnString(String tableName, String columnName, Class<?> javaClass,
			Collection<Object> values, String afterAColumnName) {

		System.out.print("SQLTableCreator.addColumn: Column " + columnName + " in table " + tableName + "...");

		String typeString = SQLUtils.classToMySQLType(javaClass, values, false);

		String sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `" + columnName + "` " + typeString;

		if (afterAColumnName != null)
			sql += (" AFTER `" + afterAColumnName + "`");

		return sql;
	}

	/**
	 * 
	 * @param conn
	 * @param sql
	 * @throws SQLException
	 */
	public static void addColumn(Connection conn, String sql) throws SQLException {

		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
			System.out.println("done");
		} catch (Exception e) {
			if (e.getMessage().startsWith("Duplicate column name")) {
				System.out.println();
//				System.err.println("SQLTableCreator.addColumn: Column " + columnName + " already in table " + tableName
//						+ ", skip.");
				System.err.println("SQLTableCreator.addColumn: column already in table, skip.");
			} else
				e.printStackTrace();
		} finally {
			if (stmt != null)
				stmt.close();
		}
	}

	/**
	 * adds missing columns to a table
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param insertType
	 * @param listOfMapWithKeyValuePairs
	 * @throws SQLException
	 */
	public static void addMissingColumns(Connection conn, String schema, String tableName,
			List<LinkedHashMap<String, Object>> listOfMapWithKeyValuePairs) throws SQLException {

		// check whether all attributes are already in the database
		List<String> columns = SQLTableSelector.columnNames(conn, schema, tableName);
		Collections.sort(columns);

		Set<String> insertColumns = new HashSet<>();
		for (LinkedHashMap<String, Object> keyValuePairs : listOfMapWithKeyValuePairs)
			insertColumns.addAll(keyValuePairs.keySet());

		boolean all = false;
		for (String attribute : insertColumns) {
			if (!columns.contains(attribute) && !all) {
				System.err.print("SQLTableCreator.insertRows: Table " + tableName + " in schema " + schema
						+ " does not contain attribute " + attribute + ". Trying to add column... ");

				Class<?> javaClass = null;
				List<Object> values = new ArrayList<Object>();
				for (LinkedHashMap<String, Object> keyValuePairs : listOfMapWithKeyValuePairs) {
					if (!keyValuePairs.containsKey(attribute))
						continue;
					if (keyValuePairs.get(attribute) == null)
						continue;
					if (javaClass == null)
						javaClass = keyValuePairs.get(attribute).getClass();
					else if (javaClass.equals(keyValuePairs.get(attribute).getClass()))
						throw new IllegalArgumentException("Attribute " + attribute
								+ " not in table, attempt to add column faile because the data was of different types ("
								+ javaClass + " and " + keyValuePairs.get(attribute).getClass() + ").");
					values.add(keyValuePairs.get(attribute));
				}
				String sql = addColumnString(tableName, attribute, javaClass, values, columns);
				addColumn(conn, sql);

				System.err.println("finished without exceptions.");
			}
		}
	}

}
