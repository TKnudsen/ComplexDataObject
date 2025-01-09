package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLTableAlternator {

	public static void modifyColumnVarCharSize(Connection conn, String schema, String tableName, String columnName,
			int size, boolean checkIfTableExists) throws SQLException {

		modifyColumn(conn, schema, tableName, columnName, "varchar(" + size + ")", checkIfTableExists);
	}

	public static void modifyColumn(Connection conn, String schema, String tableName, String columnName,
			String columnDefinition, boolean checkIfTableExists) throws SQLException {

		Statement stmt = null;
		try {

			if (checkIfTableExists)
				if (!SQLUtils.tableExists(conn, schema, tableName)) {
					System.err.println("SQLTableAlternator.alterTable: Table " + tableName + " in schema " + schema
							+ " does not exist.");
					return;
				}

			System.out.print("SQLTableCreator.alterTable: alter table " + tableName + " in schema " + schema + "...");

			String sqlString = createModifyColumnString(schema, tableName, columnName, columnDefinition,
					PostgreSQL.isPostgreSQLConnection(conn));

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

	private static String createModifyColumnString(String schema, String tableName, String columnName,
			String columnDefinition, boolean postgres) {
		String sqlString = "ALTER TABLE `" + tableName + "` MODIFY `" + columnName + "` ";

		if (postgres)
			sqlString = sqlString + "TYPE ";

		sqlString = sqlString + columnDefinition;

		if (!postgres)
			sqlString = sqlString + " NULL";

		if (!postgres)
			sqlString = sqlString + ";";

		if (postgres) {
			sqlString = PostgreSQL.replaceMySQLQuotes(sqlString);
			sqlString = PostgreSQL.replaceMySQLAttributeTypes(sqlString);
			sqlString = sqlString.replace(tableName, schema + "\".\"" + tableName);
			sqlString = sqlString.replace("MODIFY", "ALTER COLUMN");
			sqlString = sqlString.replace("NULL", "");
		}

		return sqlString;
	}
}
