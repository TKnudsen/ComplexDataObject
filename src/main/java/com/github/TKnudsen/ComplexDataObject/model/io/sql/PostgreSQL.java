package com.github.TKnudsen.ComplexDataObject.model.io.sql;

import java.sql.Connection;
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

		return conn.getClass().toString().contains("postgresql");
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

}
