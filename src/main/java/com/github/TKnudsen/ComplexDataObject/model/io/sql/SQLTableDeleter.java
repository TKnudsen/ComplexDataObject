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
	 * @param table
	 * @param columns
	 * @param queryObjects
	 * @throws SQLException
	 */
	public static void deleteTableRow(Connection conn, String table, List<String> columns, List<String> queryObjects)
			throws SQLException {

		Objects.requireNonNull(columns);
		Objects.requireNonNull(queryObjects);
		if (columns.size() != queryObjects.size())
			throw new IllegalArgumentException(
					"SQLTableDeleter.deleteTableRow: where clauses and query objects must have same size");

		String sql = "DELETE FROM `" + table + "` WHERE ";

		for (int i = 0; i < columns.size(); i++)
			sql += ("`" + columns.get(i) + "` = '" + queryObjects.get(i) + "' and");
		sql = sql.substring(0, sql.length() - 4); // get rid of tail-and

		if (PostgreSQL.isPostgreSQLConnection(conn))
			sql = PostgreSQL.replaceMySQLQuotes(sql);

		PreparedStatement preparedStmt = conn.prepareStatement(sql);
		preparedStmt.execute();
		preparedStmt.close();
	}
}
