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
	 * hint: check if table exists
	 * 
	 * hint: check if missing columns need to be added first using
	 * SQLTableCreator.addMissingColumns(...)
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param insertType                   INSERT, INSERT IGNORE, REPLACE
	 * @param cdos
	 * @param extendColumnCapacityIfNeeded
	 * @param showTimingLog
	 */
	public static void insertRows(Connection conn, String schema, String tableName, String insertType,
			ComplexDataContainer cdos, boolean extendColumnCapacityIfNeeded, boolean useFloatInsteadOfDouble,
			boolean showTimingLog) throws SQLException {

		SQLTableInserter.insertRows(conn, schema, tableName, insertType, SQLUtils.createKeyValuePairs(cdos),
				extendColumnCapacityIfNeeded, useFloatInsteadOfDouble, showTimingLog);
	}

	/**
	 * hint: check if table exists
	 * 
	 * @param conn
	 * @param schema
	 * @param tableName
	 * @param insertType INSERT, INSERT IGNORE, REPLACE
	 * @param cdo
	 */
	public static void insertRow(Connection conn, String schema, String tableName, String insertType,
			ComplexDataObject cdo, boolean checkIfTableExists, boolean useFloatInsteadOfDouble) throws SQLException {

		// create key value pairs (attributes and values)
		LinkedHashMap<String, Object> keyValuePairs = SQLUtils.createKeyValuePairs(cdo);

		SQLTableInserter.insertRow(conn, schema, tableName, insertType, keyValuePairs, useFloatInsteadOfDouble);
	}

}
