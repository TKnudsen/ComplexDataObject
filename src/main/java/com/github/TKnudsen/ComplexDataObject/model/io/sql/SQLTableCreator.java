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

	public static void createSchema(Connection conn, String schema) throws SQLException {

		// String sqlString = postgreSQL ? "CREATE SCHEMA \"" + schema + "\"" : "CREATE
		// SCHEMA `" + schema + "`";
		String sqlString = "CREATE SCHEMA `" + schema + "`";

		if (PostgreSQL.isPostgreSQLConnection(conn))
			sqlString = PostgreSQL.replaceMySQLQuotes(sqlString);

		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sqlString);
			System.out.println("SQLTableCreator.createSchema: schema " + schema + " created");
		} catch (SQLException se) {
//			se.printStackTrace();
		} catch (Exception e) {
//			e.printStackTrace();
		} finally {
			if (stmt != null)
				stmt.close();
		}
	}

	public static void createTable(Connection conn, String schema, String tableName,
			Map<String, Class<?>> schemaEntries, List<String> primaryKeyAttributes, boolean useFloatInsteadOfDouble)
			throws SQLException {

		createTable(conn, schema, tableName, schemaEntries, null, primaryKeyAttributes, useFloatInsteadOfDouble);
	}

	/**
	 * 
	 * @param conn
	 * @param schema               important to avoid confusions, e.g., if a table
	 *                             exists in several schemas.
	 * @param tableName
	 * @param schemaEntries
	 * @param valuesFunctions      optional, used to guess the size of columns in
	 *                             the SQL table.
	 * @param primaryKeyAttributes
	 * @throws SQLException
	 */
	public static void createTable(Connection conn, String schema, String tableName,
			Map<String, Class<?>> schemaEntries, Function<String, Collection<Object>> valuesFunctions,
			List<String> primaryKeyAttributes, boolean useFloatInsteadOfDouble) throws SQLException {

		boolean postgreSQL = PostgreSQL.isPostgreSQLConnection(conn);

		// test if schema exists
		createSchema(conn, schema);

		String sqlString = createTableString(schema, tableName, schemaEntries, valuesFunctions, primaryKeyAttributes,
				useFloatInsteadOfDouble, postgreSQL);

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
			System.err.println("SQL STRING: " + sqlString);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("SQL STRING: " + sqlString);
		} finally {
			if (stmt != null)
				stmt.close();
		}
	}

	public static String createTableString(String tableName, Map<String, Class<?>> schemaEntries,
			Function<String, Collection<Object>> attributeValueDistributions, List<String> primaryKeyAttributes,
			boolean useFloatInsteadOfDouble, boolean postgreSQL) {

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
				typeString = SQLUtils.classToSQLType(schemaEntries.get(entryName), values,
						pkAttributes.contains(entryName), useFloatInsteadOfDouble, postgreSQL);
			else
				typeString = SQLUtils.classToSQLType(schemaEntries.get(entryName), pkAttributes.contains(entryName),
						useFloatInsteadOfDouble, postgreSQL);

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

		if (postgreSQL)
			sql = PostgreSQL.replaceMySQLQuotes(sql);

		return sql;
	}

	public static String createTableString(String schema, String tableName, Map<String, Class<?>> schemaEntries,
			Function<String, Collection<Object>> attributeValueDistributions, List<String> primaryKeyAttributes,
			boolean useFloatInsteadOfDouble, boolean postgreSQL) {
		if (!postgreSQL)
			return createTableString(schema + "." + tableName, schemaEntries, attributeValueDistributions,
					primaryKeyAttributes, useFloatInsteadOfDouble, postgreSQL);

		String sqlString = createTableString(schema + "\".\"" + tableName, schemaEntries, attributeValueDistributions,
				primaryKeyAttributes, useFloatInsteadOfDouble, postgreSQL);

		return sqlString;
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
			Collection<Object> values, Iterable<String> existingColumns, boolean useFloatInsteadOfDouble,
			boolean postgreSQL) throws SQLException {

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

		return addColumnString(tableName, newColumnName, javaClass, values, afterAColumnName, useFloatInsteadOfDouble,
				postgreSQL);
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
			Collection<Object> values, String afterAColumnName, boolean useFloatInsteadOfDouble, boolean postgreSQL) {

		System.out.print("SQLTableCreator.addColumnString: Column " + columnName + " in table " + tableName + "...");

		String typeString = SQLUtils.classToSQLType(javaClass, values, false, useFloatInsteadOfDouble, postgreSQL);

		String sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `" + columnName + "` " + typeString;

		if (afterAColumnName != null)
			if (!postgreSQL)
				sql += (" AFTER `" + afterAColumnName + "`");
			else
				System.err.println(
						"SQLTableCreator.addColumnString: postgreSQL does not allow adding columns AFTER others. column ordering request ignored for attribute "
								+ afterAColumnName);

		if (postgreSQL)
			sql = PostgreSQL.replaceMySQLQuotes(sql);

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
			List<LinkedHashMap<String, Object>> listOfMapWithKeyValuePairs, boolean useFloatInsteadOfDouble)
			throws SQLException {

		boolean postgreSQL = PostgreSQL.isPostgreSQLConnection(conn);

		// check whether all attributes are already in the database
		List<String> columns = SQLTableSelector.columnNames(conn, schema, tableName);
		Collections.sort(columns);

		Set<String> insertColumns = new HashSet<>();
		for (LinkedHashMap<String, Object> keyValuePairs : listOfMapWithKeyValuePairs)
			insertColumns.addAll(keyValuePairs.keySet());

		boolean all = false;
		for (String attribute : insertColumns) {
			if (!columns.contains(attribute) && !all) {
				System.err.print("SQLTableCreator.addMissingColumns: Table " + tableName + " in schema " + schema
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
				String sql = addColumnString(tableName, attribute, javaClass, values, columns, useFloatInsteadOfDouble,
						postgreSQL);
				addColumn(conn, sql);

				System.err.println("finished without exceptions.");
			}
		}
	}

}
