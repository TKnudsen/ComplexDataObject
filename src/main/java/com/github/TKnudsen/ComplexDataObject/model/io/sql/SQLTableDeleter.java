package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.Parsers;

/**
 * <p>
 * Deletes columns, tables, and individual rows from SQL tables for both MySQL
 * and PostgreSQL connections. Row deletion builds a WHERE clause from given
 * column/value pairs and reports whether the row count changed as a result.
 * </p>
 */
public class SQLTableDeleter {

	/**
	 * Drops a column from a database table. If it is not a PostgreSQL connection,
	 * the schema is ignored. If it is PostgreSQL this method will throw an
	 * exception (missing schema).
	 *
	 * @param conn       The database connection
	 * @param tableName  The table name
	 * @param columnName The column name to drop
	 */
	public static void dropColumn(Connection conn, String tableName, String columnName) throws SQLException {
		dropColumn(conn, null, tableName, columnName);
	}

	/**
	 * Drops a column from a database table. If it is not a PostgreSQL connection,
	 * the schema is ignored.
	 *
	 * Tested for PostgreSQL and MySQL.
	 *
	 * @param conn       The database connection
	 * @param schema     The schema name (only used for PostgreSQL)
	 * @param tableName  The table name
	 * @param columnName The column name to drop
	 */
	public static void dropColumn(Connection conn, String schema, String tableName, String columnName)
			throws SQLException {

		System.out.println(
				"SQLTableDeleter.dropColumn: column '" + columnName + "' in table " + tableName + ", schema " + schema);
		String sql = null;
		Statement stmt = null;

		try {
			stmt = conn.createStatement();

			boolean isPostgres = PostgreSQL.isPostgreSQLConnection(conn);

			if (isPostgres && (schema == null || schema.length() == 0))
				throw new IllegalArgumentException(
						"SQLTableDeleter.dropColumn: PostgreSQL operation requires schema specification, but was: "
								+ schema);

			String schemaAndTable = isPostgres ? PostgreSQL.schemaAndTableName(schema, tableName) : tableName;

			sql = "ALTER TABLE `" + schemaAndTable + "` DROP COLUMN `" + columnName + "`";

			if (isPostgres)
				sql = PostgreSQL.replaceMySQLQuotes(sql);

			stmt.executeUpdate(sql);

			SQLTableStatistics.clearCache(schema);
		} catch (SQLException e) {
			e.printStackTrace();

			System.err.println("Failed to execute SQL: " + sql);
			throw e;
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	public static void dropTable(Connection conn, String tableName) throws SQLException {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String sql = "DROP TABLE " + tableName;
			stmt.executeUpdate(sql);

			SQLTableStatistics.clearCache();
		} catch (SQLException e) {
			e.printStackTrace();

			throw e;
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	public static void dropTable(Connection conn, String schema, String tableName) throws SQLException {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String sql = "DROP TABLE " + schema + "." + tableName;
			stmt.executeUpdate(sql);

			SQLTableStatistics.clearCache(schema);
		} catch (SQLException e) {
			e.printStackTrace();

			throw e;
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
	public static boolean deleteTableRow(Connection conn, String schema, String tableName, List<String> columns,
			List<String> queryObjects, boolean showTimingLog) throws SQLException {
		Objects.requireNonNull(tableName);
		Objects.requireNonNull(columns);
		Objects.requireNonNull(queryObjects);
		if (columns.size() != queryObjects.size())
			throw new IllegalArgumentException(
					"SQLTableDeleter.deleteTableRow: where clauses and query objects must have same size");

		if (showTimingLog)
			System.out.print("SQLTableDeleter.deleteTableRow: deleting rows in table " + tableName + "...");
		long l = System.currentTimeMillis();

		boolean postgres = PostgreSQL.isPostgreSQLConnection(conn);

		// TODO find out why schema "public" is not needed in postgres "tableName" but
		// others like "prices" are (prices.tableName; without "" by the way). Is it due
		// to the name public? or is there an active/default
		// schema which would be the public in this case?

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

		String likeForFloats = !postgres ? "` LIKE '" : "` = '";
		for (int i = 0; i < columns.size(); i++) {
			String substring = "`" + columns.get(i);
			if (Parsers.parseDouble(queryObjects.get(i)) != null
					&& !Double.isNaN(Parsers.parseDouble(queryObjects.get(i))))
				substring += likeForFloats;
			else
				substring += "` = '";
			substring += queryObjects.get(i) + "' and ";
			sql += substring;
		}
		sql = sql.substring(0, sql.length() - 5); // get rid of and (tail)

		if (postgres)
			sql = PostgreSQL.replaceMySQLQuotes(sql);

		long rowCount = SQLTableStatistics.rowCount(conn, schema, tableName, true);

		PreparedStatement preparedStmt = conn.prepareStatement(sql);
		preparedStmt.execute();
		preparedStmt.close();

		if (showTimingLog)
			System.out.println("done in " + (System.currentTimeMillis() - l) + " ms");

		// refresh
		SQLTableStatistics.clearRowCountCache(schema);

		return rowCount > SQLTableStatistics.rowCount(conn, schema, tableName, true);
	}
}
