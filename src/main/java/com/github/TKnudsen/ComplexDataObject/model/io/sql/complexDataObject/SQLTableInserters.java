package com.github.TKnudsen.ComplexDataObject.model.io.sql.complexDataObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.sql.SQLTableInserter;
import com.github.TKnudsen.ComplexDataObject.model.io.sql.SQLUtils;

public class SQLTableInserters {

	/**
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param insertType         INSERT, INSERT IGNORE, REPLACE
	 * @param cdos
	 * @param checkIfTableExists
	 * @param addMissingColumns  determines if columns missing in the table will be
	 *                           inserted automatically (if possible/type known)
	 */
	public static void insertRows(Connection conn, String schema, String tableName, String insertType,
			ComplexDataContainer cdos, boolean checkIfTableExists, boolean addMissingColumns) throws SQLException {

		SQLTableInserter.insertRows(conn, schema, tableName, insertType, SQLUtils.createKeyValuePairs(cdos),
				checkIfTableExists, addMissingColumns);
	}

	/**
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param insertType         INSERT, INSERT IGNORE, REPLACE
	 * @param cdo
	 * @param checkIfTableExists
	 */
	public static void insertRow(Connection conn, String schema, String tableName, String insertType,
			ComplexDataObject cdo, boolean checkIfTableExists) throws SQLException {

		// create key value pairs (attributes and values)
		LinkedHashMap<String, Object> keyValuePairs = SQLUtils.createKeyValuePairs(cdo);

		SQLTableInserter.insertRow(conn, schema, tableName, insertType, keyValuePairs, checkIfTableExists);
	}

}
