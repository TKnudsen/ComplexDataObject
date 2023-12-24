package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

public class SQLTableDeleter {

	public static void dropColumn(Connection conn, String tableName, String columnName) {

		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String sql = "ALTER TABLE " + tableName + " DROP COLUMN `" + columnName + "`";

			if (PostgreSQL.isPostgreSQLConnection(conn))
				sql = PostgreSQL.replaceMySQLQuotes(sql);

			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	public static void dropTable(Connection conn, String tableName) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String sql = "DROP TABLE " + tableName;
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	public static void dropTable(Connection conn, String schema, String tableName) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String sql = "DROP TABLE " + schema + "." + tableName;
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * TODO add schema for postgreSQL
	 * 
	 * @param conn
	 * @param tableName
	 * @param columns
	 * @param queryObjects
	 * @throws SQLException
	 */
	public static void deleteTableRow(Connection conn, String schema, String tableName, List<String> columns,
			List<String> queryObjects) throws SQLException {
		Objects.requireNonNull(tableName);
		Objects.requireNonNull(columns);
		Objects.requireNonNull(queryObjects);
		if (columns.size() != queryObjects.size())
			throw new IllegalArgumentException(
					"SQLTableDeleter.deleteTableRow: where clauses and query objects must have same size");

		boolean postgres = PostgreSQL.isPostgreSQLConnection(conn);

		// TODO find out why schema "public" is not needed in postgres "tableName" but
		// others like "prices" are (prices.tableName; without "" by the way). Is it due
		// to the name public? or is there an active/default
		// schema which would be the public in this case?

		// TODO find out why schema Attributes requires "Attributes"."tablename".

		String sql = null;
		if (postgres && schema != null) {
			if (schema.equals("public"))
				sql = "DELETE FROM `" + tableName + "` WHERE ";
			else if (schema.equals("Attributes"))
				sql = "DELETE FROM `" + PostgreSQL.schemaAndTableName(schema, tableName) + "` WHERE ";
//			else
//				sql = "DELETE FROM " + schema + "." + tableName + " WHERE ";
			else
				sql = "DELETE FROM `" + PostgreSQL.schemaAndTableName(schema, tableName) + "` WHERE ";
		} else
			sql = "DELETE FROM `" + tableName + "` WHERE ";

//		String sql = (postgres && schema != null && !schema.equals("public") && !schema.equals("Attributes"))
//				? "DELETE FROM " + schema + "." + tableName + " WHERE "
//				: "DELETE FROM `" + tableName + "` WHERE ";

		for (int i = 0; i < columns.size(); i++)
			sql += ("`" + columns.get(i) + "` = '" + queryObjects.get(i) + "' and ");
		sql = sql.substring(0, sql.length() - 5); // get rid of tail-and

		if (postgres)
			sql = PostgreSQL.replaceMySQLQuotes(sql);

		PreparedStatement preparedStmt = conn.prepareStatement(sql);
		preparedStmt.execute();
		preparedStmt.close();
	}
}
