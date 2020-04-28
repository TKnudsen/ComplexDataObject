package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLTableAlternator {

	public static void modifyColumnVarCharSize(Connection conn, String schema, String tableName, String columnName,
			int size, boolean checkIfTableExists) throws SQLException {

		modifyColumn(conn, schema, tableName, columnName, "varchar(" + size + ") NULL", checkIfTableExists);
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

			String sql = createModifyColumnString(tableName, columnName, columnDefinition);
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
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

	private static String createModifyColumnString(String tableName, String columnName, String columnDefinition) {
		String sql = "ALTER TABLE `" + tableName + "` MODIFY `" + columnName + "` " + columnDefinition + ";";

		return sql;
	}
}
