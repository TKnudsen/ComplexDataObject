package com.github.TKnudsen.ComplexDataObject.model.io.sql.complexDataObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.data.DataSchema;
import com.github.TKnudsen.ComplexDataObject.data.DataSchemaEntry;
import com.github.TKnudsen.ComplexDataObject.data.DataSchemas;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.sql.SQLTableSelector;
import com.github.TKnudsen.ComplexDataObject.model.io.sql.SQLTableSelector.Order;

public class SQLTableSelectors {

	/**
	 * 
	 * @param conn
	 * @param tableName
	 * @param schema         the target schema that is selected from the database
	 * @param orderAttribute
	 * @param order
	 * @return
	 * @throws SQLException
	 */
	public static List<ComplexDataObject> selectAllFromTable(Connection conn, String tableName, DataSchema schema,
			String orderAttribute, Order order) throws SQLException {
		Objects.requireNonNull(schema);

		return SQLTableSelector.selectAllFromTable(conn, tableName, orderAttribute, order,
				DataSchemas.getClassMap(schema));
	}

	/**
	 * 
	 * @param conn
	 * @param tableName
	 * @param attributeCharacterization the target schema that is selected from the
	 *                                  database
	 * @param orderAttribute
	 * @param order
	 * @return
	 * @throws SQLException
	 */
	public static List<ComplexDataObject> selectAllFromTable(Connection conn, String tableName,
			Collection<DataSchemaEntry<?>> attributeCharacterization, String orderAttribute, Order order)
			throws SQLException {
		Objects.requireNonNull(attributeCharacterization);

		return SQLTableSelector.selectAllFromTable(conn, tableName, orderAttribute, order,
				DataSchemas.getClassMap(attributeCharacterization));
	}

	/**
	 * 
	 * @param conn
	 * @param tableName
	 * @param attributeCharacterization the target schema that is selected from the
	 *                                  database
	 * @param String                    searchString the WHERE condition (without
	 *                                  WHERE). can be null. example a) columname
	 *                                  >='2012-12-25 00:00:00'. b) searchColumn =
	 *                                  'searchQuery'. Make sure to use ' where
	 *                                  needed
	 * @param orderAttribute
	 * @param order
	 * @return
	 * @throws SQLException
	 */
	public static List<ComplexDataObject> selectFromTableWhere(Connection conn, String tableName,
			Collection<DataSchemaEntry<?>> attributeCharacterization, String searchString, String orderAttribute,
			Order order) throws SQLException {
		return SQLTableSelector.selectFromTableWhere(conn, tableName, searchString, orderAttribute, order,
				DataSchemas.getClassMap(attributeCharacterization));
	}

}
