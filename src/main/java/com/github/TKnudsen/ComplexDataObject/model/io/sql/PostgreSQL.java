package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class PostgreSQL {

	/**
	 * true if postrgreSQL connection
	 * 
	 * @param conn
	 * @return
	 */
	public static boolean isPostgreSQLConnection(Connection conn) {
		Objects.requireNonNull(conn);

		// my connections pool connection
		if (conn.getClass().toString().contains("postgresql"))
			return true;
		// Apache connections pool connection
		if (conn.toString().contains("PostgreSQL JDBC Driver"))
			return true;
		// C3Po connection pool - may serve for other connections, too, but comes with
		// an SQL exception
		try {
			if (conn.getMetaData().getDatabaseProductName().toString().contains("PostgreSQL"))
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	public static String schemaAndTableName(String schema, String tableName) {
		Objects.requireNonNull(schema);
		Objects.requireNonNull(tableName);

		return schema + "\".\"" + tableName;
	}

	public static String replaceMySQLQuotes(String sql) {
		String r = sql.replace("`", "\"");
		return r;

		// .replace("'", "\""); // no, that's for values. keep those (!).
	}

	public static String quotationsForAttribute(String attribute) {
		Objects.requireNonNull(attribute);

		return "\"" + attribute + "\"";
	}

	public static String quotationsForValue(String value) {
		if (value != null)
			return "'" + value + "'";

		return null;
	}

	public static String replaceMySQLAttributeTypes(String sqlString) {
		if (sqlString == null)
			return null;

		String ret = String.valueOf(sqlString);

		ret = ret.replace("BLOB", "TEXT");
		ret = ret.replace("blob", "TEXT");

		ret = ret.replace("DOUBLE", "FLOAT8");
		ret = ret.replace("double", "FLOAT8");

		return ret;
	}

	public static void dropColumn(Connection conn, String schema, String tableName, String columnName) {

		String schemaAndTable = PostgreSQL.isPostgreSQLConnection(conn)
				? PostgreSQL.schemaAndTableName(schema, tableName)
				: tableName; // here the schema is already part of the connection

		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String sql = "ALTER TABLE `" + schemaAndTable + "` DROP COLUMN `" + columnName + "`";

			if (PostgreSQL.isPostgreSQLConnection(conn))
				sql = PostgreSQL.replaceMySQLQuotes(sql);

			stmt.executeUpdate(sql);

			System.out.println("PostgreSQL.dropColumn: Column " + columnName + " in Table " + tableName + " in Schema "
					+ schema + " dropped");
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("PostgreSQL.dropColumn: Column " + columnName + " in Table " + tableName + " in Schema "
					+ schema + " not successful");
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

}
