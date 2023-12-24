package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

public class SQLTableSelector {

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
	 * @return
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
	 * @return
	 * @throws SQLException
	 * 
	 */
	public static synchronized List<ComplexDataObject> selectAllFromTable(Connection conn, String schema,
			String tableName, String orderAttribute, Order order, Map<String, Class<?>> attributeCharacterization)
			throws SQLException {

		System.out.print("SQLTableSelector.selectAllFromTable: selecting all rows from table " + tableName + " ...");
		long l = System.currentTimeMillis();

		PreparedStatement preparedStatement = selectAllFromTablePreparedStatement(conn, schema, tableName,
				orderAttribute, order);
		ResultSet resultSet = null;
		try {
			resultSet = preparedStatement.executeQuery();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<ComplexDataObject> result = new ArrayList<ComplexDataObject>();
		if (attributeCharacterization != null)
			result.addAll(SQLUtils.interpreteResultSet(resultSet, attributeCharacterization));
		else
			result.addAll(SQLUtils.interpreteResultSet(resultSet));

		if (resultSet != null)
			resultSet.close();
		if (preparedStatement != null)
			preparedStatement.close();

		System.out.println("done in " + (System.currentTimeMillis() - l) + " ms");

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
	public static synchronized PreparedStatement selectAllFromTablePreparedStatement(Connection conn, String schema,
			String tableName, String orderAttribute, Order order) throws SQLException {

		String schemaAndTable = PostgreSQL.isPostgreSQLConnection(conn)
				? PostgreSQL.schemaAndTableName(schema, tableName)
				: tableName; // here the schema is already part of the connection

		PreparedStatement preparedStatement = null;
		String sql = (orderAttribute == null) ? "SELECT * FROM `" + schemaAndTable + "`"
				: "SELECT * FROM `" + schemaAndTable + "` ORDER BY `" + orderAttribute + "` " + order.name();

		if (PostgreSQL.isPostgreSQLConnection(conn))
			sql = PostgreSQL.replaceMySQLQuotes(sql);

		preparedStatement = conn.prepareStatement(sql);

		return preparedStatement;
	}

	/**
	 * 
	 * @param conn
	 * @param schema         where the table lives in
	 * @param tableName
	 * @param searchString   the WHERE condition (without WHERE). can be null.
	 *                       example a) column name greater or equals'2012-12-25
	 *                       00:00:00'. b) searchColumn equals 'searchQuery'. Make
	 *                       sure to use ' where needed
	 * @param orderAttribute can be null, then order will be ignored
	 * @param order
	 * @return
	 * @throws SQLException
	 */
	public static List<ComplexDataObject> selectFromTableWhere(Connection conn, String schema, String tableName,
			String searchString, String orderAttribute, Order order) throws SQLException {

		return selectFromTableWhere(conn, schema, tableName, searchString, orderAttribute, order, null);
	}

	/**
	 * 
	 * @param conn
	 * @param schema                    name of the schema where the table lives in
	 * @param tableName
	 * @param searchString              the WHERE condition (without WHERE). can be
	 *                                  null. example a) column name greater or
	 *                                  equals'2012-12-25 00:00:00'. b) searchColumn
	 *                                  equals 'searchQuery'. Make sure to use '
	 *                                  where needed
	 * @param orderAttribute            can be null, then order will be ignored
	 * @param order
	 * @param attributeCharacterization the target schema that is selected from the
	 *                                  database
	 * @return
	 * @throws SQLException
	 */
	public static List<ComplexDataObject> selectFromTableWhere(Connection conn, String schema, String tableName,
			String searchString, String orderAttribute, Order order, Map<String, Class<?>> attributeCharacterization)
			throws SQLException {
		return selectFromTableWhere(conn, schema, tableName, null, searchString, orderAttribute, order,
				attributeCharacterization);
	}

	/**
	 * 
	 * @param conn
	 * @param tableName
	 * @param columns                   set of columns to be queried
	 * @param searchString              the WHERE condition (without WHERE). has
	 *                                  problems with null. example a) column name
	 *                                  greater or equals'2012-12-25 00:00:00'. b)
	 *                                  searchColumn equals 'searchQuery'. Make sure
	 *                                  to use ' where needed
	 * @param orderAttribute            can be null, then order will be ignored
	 * @param order
	 * @param attributeCharacterization the target schema that is selected from the
	 *                                  database
	 * @return
	 * @throws SQLException
	 */
	public static List<ComplexDataObject> selectFromTableWhere(Connection conn, String schema, String tableName,
			List<String> columns, String searchString, String orderAttribute, Order order,
			Map<String, Class<?>> attributeCharacterization) throws SQLException {

		Objects.requireNonNull(schema);
		Objects.requireNonNull(tableName);

		System.out.print("SQLTableSelector.selectFromTable: selecting all rows from table " + tableName + " ...");
		long l = System.currentTimeMillis();

		String fromString = columns == null ? "*" : "";
		if (columns != null && !columns.isEmpty()) {
			for (String column : columns)
				if (PostgreSQL.isPostgreSQLConnection(conn))
					fromString += (PostgreSQL.quotationsForAttribute(column) + ", ");
				else
					fromString += (column + ", ");
			fromString = fromString.substring(0, fromString.length() - 2);
		}

		String schemaAndTable = PostgreSQL.isPostgreSQLConnection(conn)
				? PostgreSQL.schemaAndTableName(schema, tableName)
				: tableName; // here the schema is already part of the connection

		List<ComplexDataObject> result = new ArrayList<ComplexDataObject>();

		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			String sql = (orderAttribute == null) ? "SELECT * FROM `" + schemaAndTable + "` WHERE PLACEHOLDER"
					: "SELECT " + fromString + " FROM `" + schemaAndTable + "` WHERE PLACEHOLDER ORDER BY `"
							+ orderAttribute + "` " + order.name();

			if (PostgreSQL.isPostgreSQLConnection(conn))
				sql = PostgreSQL.replaceMySQLQuotes(sql);

			// the search string needs to be postgreSQL conform, values may have the other
			// escape string in use (')
			String ss = PostgreSQL.replaceMySQLQuotes(searchString);
			sql = sql.replace("PLACEHOLDER", ss);

			preparedStatement = conn.prepareStatement(sql);

			resultSet = preparedStatement.executeQuery();
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (attributeCharacterization != null)
			result.addAll(SQLUtils.interpreteResultSet(resultSet, attributeCharacterization));
		else
			result.addAll(SQLUtils.interpreteResultSet(resultSet));

		if (resultSet != null)
			resultSet.close();
		if (preparedStatement != null)
			preparedStatement.close();

		System.out.println("done in " + (System.currentTimeMillis() - l) + " ms");

		return result;
	}

	/**
	 * 
	 * @param conn
	 * @param schema         where the table lives in
	 * @param tableName
	 * @param columns
	 * @param searchString   the WHERE condition (without WHERE). can be null.
	 *                       example a) column name greater or equals'2012-12-25
	 *                       00:00:00'. b) searchColumn equals 'searchQuery'. Make
	 *                       sure to use ' where needed
	 * 
	 * @param orderAttribute
	 * @param order
	 * @return
	 * @throws SQLException
	 */
	public static Collection<Collection<Object>> selectColumnsFromTable(Connection conn, String schema,
			String tableName, List<String> columns, String searchString, String orderAttribute, Order order)
			throws SQLException {

		Objects.requireNonNull(tableName);

		String schemaAndTable = PostgreSQL.isPostgreSQLConnection(conn)
				? PostgreSQL.schemaAndTableName(schema, tableName)
				: tableName; // here the schema is already part of the connection

		Collection<Collection<Object>> values = new ArrayList<>();

		// prepare query
		String query = "SELECT ";
		if (columns != null && !columns.isEmpty()) {
			for (String column : columns)
				if (PostgreSQL.isPostgreSQLConnection(conn))
					query += (PostgreSQL.quotationsForAttribute(column) + ",");
				else
					query += "`" + column + "`,";
			query = query.substring(0, query.length() - 1);
		} else
			query += "*";
		query += " FROM `" + schemaAndTable + "`";

		if (searchString != null)
			query += (" WHERE " + searchString);

		if (orderAttribute != null)
			query += (" ORDER BY `" + orderAttribute + "` " + order.name());

		if (PostgreSQL.isPostgreSQLConnection(conn))
			query = PostgreSQL.replaceMySQLQuotes(query);

		// create a statement
		Statement stmt = conn.createStatement();

		// execute query and return result as a ResultSet
		ResultSet resultSet = stmt.executeQuery(query);
		if (resultSet == null)
			throw new SQLException();

		// get column names
		ResultSetMetaData rsmd = resultSet.getMetaData();
		String[] columnNames = new String[rsmd.getColumnCount()];
		int[] columnTypes = new int[rsmd.getColumnCount()];

		for (int i = 0; i < columnNames.length; i++) {
			columnNames[i] = rsmd.getColumnLabel(i + 1);
			columnTypes[i] = rsmd.getColumnType(i + 1);
		}

		while (resultSet.next()) {
			LinkedHashMap<String, Object> map = SQLUtils.interpreteResultSetRow(resultSet, columnNames, columnTypes);
			values.add(map.values());
		}

		try {
			if (resultSet != null)
				resultSet.close();
			if (stmt != null)
				stmt.close();
		} catch (SQLException e) {
		}

		return values;
	}

	/**
	 * selects all tables in a database for a given schema
	 * 
	 * @param conn
	 * @param schema
	 * @return
	 */
	public static Collection<String> tableNames(Connection conn, String schema) {

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
		} catch (Exception e) {
			e.printStackTrace();
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

}
