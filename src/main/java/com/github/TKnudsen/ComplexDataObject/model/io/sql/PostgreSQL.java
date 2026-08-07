package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Objects;

/**
 * <p>
 * Collection of static PostgreSQL-specific JDBC helper methods, including
 * detecting whether a Connection targets a PostgreSQL database, building
 * schema-qualified and quoted identifiers, translating MySQL-style syntax to
 * PostgreSQL equivalents, and inspecting or increasing the length of VARCHAR
 * columns.
 * </p>
 */
public class PostgreSQL {

	/**
	 * true if postrgreSQL connection
	 * 
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static boolean isPostgreSQLConnection(Connection conn) throws SQLException {
		Objects.requireNonNull(conn);

		// my connections pool connection
		if (conn.getClass().toString().contains("postgresql"))
			return true;
		// Apache connections pool connection
		if (conn.toString().contains("PostgreSQL JDBC Driver"))
			return true;
		// C3Po connection pool - may serve for other connections, too, but comes with
		// an SQL exception

		String dbName = conn.getMetaData().getDatabaseProductName();
		return dbName != null && dbName.toLowerCase(Locale.ROOT).contains("postgres");
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

	public static void dropColumn(Connection conn, String schema, String tableName, String columnName)
			throws SQLException {

		boolean postgreSQL = PostgreSQL.isPostgreSQLConnection(conn);

		// here the schema is already part of the connection
		String schemaAndTable = postgreSQL ? PostgreSQL.schemaAndTableName(schema, tableName) : tableName;

		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String sql = "ALTER TABLE `" + schemaAndTable + "` DROP COLUMN `" + columnName + "`";

			if (postgreSQL)
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

		SQLTableStatistics.clearCache(schema);
	}

	/**
	 * Returns the current defined length of a PostgreSQL VARCHAR column.
	 *
	 * @param conn   an open JDBC Connection
	 * @param schema the schema name (case-sensitive, usually needs quoting)
	 * @param table  the table name
	 * @param column the column name
	 * @return the current VARCHAR length, or -1 if column not found or not VARCHAR
	 * @throws SQLException if the query fails
	 */
	public static int getVarcharColumnLength(Connection conn, String schema, String table, String column,
			boolean showLog) throws SQLException {

		if (showLog)
			System.out.println("PostgreSQL.getVarcharColumnLength for column '" + column + "', in table '" + table
					+ "', in schema '" + schema + "'.");

		// Query metadata from the PostgreSQL information schema
		String checkSql = "SELECT data_type, character_maximum_length " + "FROM information_schema.columns "
				+ "WHERE table_schema = ? AND table_name = ? AND column_name = ?";

		try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
			ps.setString(1, schema);
			ps.setString(2, table);
			ps.setString(3, column);

			try (ResultSet rs = ps.executeQuery()) {

				if (!rs.next()) {
					if (showLog)
						System.err.println("PostgreSQL.getVarcharColumnLength: Column not found in information_schema: "
								+ schema + "." + table + "." + column);
					return -1;
				}

				String dataType = rs.getString("data_type");
				if (dataType == null || !"character varying".equalsIgnoreCase(dataType)) {
					if (showLog)
						System.err.println("PostgreSQL.getVarcharColumnLength: Column " + column
								+ " is not VARCHAR (type=" + dataType + ")");
					return -1;
				}

				// May be null (e.g., if column defined as VARCHAR without length),
				// so fallback to -1 for safety.
				Integer currentLen = (Integer) rs.getObject("character_maximum_length");
				return currentLen != null ? currentLen : -1;
			}
		}
	}

	/**
	 * Increases the VARCHAR length of a PostgreSQL column IF AND ONLY IF the new
	 * required length is greater than the current length.
	 *
	 * @param conn   an open JDBC Connection (must NOT be inside a failed
	 *               transaction)
	 * @param schema the schema name
	 * @param table  the table name
	 * @param column the column to alter
	 * @param newLen the required new VARCHAR length
	 * @throws SQLException if ALTER TABLE fails
	 * @return true when the column length was successfully increased
	 */
	public static boolean increaseVarcharColumnLength(Connection conn, String schema, String table, String column,
			int newLen) throws SQLException {

		System.out.println("PostgreSQL.increaseVarcharColumnLength for column '" + column + "', in table '" + table
				+ "', in schema '" + schema + "', if needed.");

		int currentLen = getVarcharColumnLength(conn, schema, table, column, false);

		// If metadata lookup failed or column is not VARCHAR
		if (currentLen < 0) {
			System.err.println(
					"Skipping column expansion for " + column + " because current length could not be determined.");
			return false;
		}

		// Nothing to do if column already big enough
		if (currentLen >= newLen) {
			System.err.println("No need to expand: current length " + currentLen + " >= new length " + newLen
					+ " for column " + column);
			return false;
		}

		String quotedSchema = quoteIdent(schema);
		String quotedTable = quoteIdent(table);
		String quotedColumn = quoteIdent(column);

		String alterSql = "ALTER TABLE " + quotedSchema + "." + quotedTable + " ALTER COLUMN " + quotedColumn
				+ " TYPE varchar(" + newLen + ")";

		System.err.println("PostgreSQL.increaseVarcharColumnLength: Executing: " + alterSql);

		try (Statement st = conn.createStatement()) {
			st.execute(alterSql);
		}

		System.err.println("PostgreSQL.increaseVarcharColumnLength: Increased " + schema + "." + table + "." + column
				+ " to varchar(" + newLen + ")");
		return true;
	}

	/**
	 * Quotes an identifier for safe use in PostgreSQL SQL commands. Doubles any
	 * internal quotes to avoid SQL injection or syntax errors.
	 *
	 * @param ident identifier (schema, table, or column name)
	 * @return a safely quoted identifier
	 */
	private static String quoteIdent(String ident) {
		return "\"" + ident.replace("\"", "\"\"") + "\"";
	}
}
