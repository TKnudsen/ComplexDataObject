package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.postgresql.util.PSQLException;

/**
 * <p>
 * Alters existing SQL tables, in particular changing column definitions such
 * as VARCHAR sizes for MySQL and PostgreSQL connections. Also provides a
 * helper that inspects a PostgreSQL data truncation exception to identify
 * which columns need to be widened based on the values that failed to
 * insert.
 * </p>
 */
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
			throw se;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
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

	public static List<ColumnSizeFix> detectTooLongColumnsFromException(SQLException e, Map<String, Object> rowValues) {

		List<ColumnSizeFix> fixes = new ArrayList<>();

		// Walk down to root cause
		Throwable cause = e;
		while (cause.getCause() != null && cause instanceof SQLException) {
			cause = cause.getCause();
		}

		if (!(cause instanceof PSQLException)) {
			System.err.println("Not a PostgreSQL PSQLException: " + e.getMessage());
			return fixes;
		}

		PSQLException psqlEx = (PSQLException) cause;
		String msg = psqlEx.getMessage();

		// Extract numeric limit from "character varying(19)"
		Pattern p = Pattern.compile("character varying\\((\\d+)\\)");
		Matcher m = p.matcher(msg);
		if (!m.find()) {
			System.err.println("Could not extract max varchar length.");
			return fixes;
		}

		int maxLength = Integer.parseInt(m.group(1));

		// Identify columns with longer values
		for (Map.Entry<String, Object> entry : rowValues.entrySet()) {
			String column = entry.getKey();
			Object val = entry.getValue();
			if (val == null)
				continue;

			String s = val.toString();

			if (s.length() > maxLength) {
				//System.err.println("Column candidate for expansion: " + column + " (len " + s.length() + " > "
				//		+ maxLength + "), value = \"" + val + "\"");

				fixes.add(new ColumnSizeFix(column, s.length()));
			}
		}

		return fixes;
	}
}
