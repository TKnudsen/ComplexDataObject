package com.github.TKnudsen.ComplexDataObject.model.io.sql.complexDataObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.model.io.sql.SQLTableCreator;

public class SQLTableCreators {

	public static void createTable(Connection conn, String schema, String tableName, ComplexDataContainer dataContainer,
			List<String> primaryKeyAttributes) throws SQLException {

		Map<String, Class<?>> dataSchema = dataContainer.getSchema();
		// add ID to the schema
		dataSchema.put("ID", Long.class);

		SQLTableCreator.createTable(conn, schema, tableName, dataSchema, dataContainer::getAttributeValueCollection,
				primaryKeyAttributes);
	}

	/**
	 * for cases where the SQL string is needed externally.
	 * 
	 * @param tableName
	 * @param dataContainer
	 * @param primaryKeyAttributes
	 * @return
	 * @throws SQLException
	 */
	public static String createTableString(String tableName, ComplexDataContainer dataContainer,
			List<String> primaryKeyAttributes) throws SQLException {

		Map<String, Class<?>> dataSchema = dataContainer.getSchema();
		// add ID to the schema
		dataSchema.put("ID", Long.class);

		return SQLTableCreator.createTableString(tableName, dataSchema, dataContainer::getAttributeValueCollection,
				primaryKeyAttributes);
	}

}
