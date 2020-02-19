package com.github.TKnudsen.ComplexDataObject.data;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class DataSchemas {

	/**
	 * enables the usage of DataSchema characteristics without the need for the
	 * dataSchema class.
	 * 
	 * @param dataSchema
	 * @return
	 */
	public static Map<String, Class<?>> getClassMap(DataSchema dataSchema) {
		Objects.requireNonNull(dataSchema);

		Map<String, Class<?>> map = new LinkedHashMap<String, Class<?>>();
		for (DataSchemaEntry<?> entry : dataSchema.getAttributeEntries())
			map.put(entry.getName(), entry.getType());

		return map;
	}

	/**
	 * enables the usage of DataSchema / DataSchemaEntry characteristics without the
	 * need for the DataSchema / DataSchemaEntry classes.
	 * 
	 * @param dataSchema
	 * @return
	 */
	public static Map<String, Class<?>> getClassMap(Collection<DataSchemaEntry<?>> schemaEntries) {
		Objects.requireNonNull(schemaEntries);

		Map<String, Class<?>> map = new LinkedHashMap<String, Class<?>>();
		for (DataSchemaEntry<?> entry : schemaEntries)
			map.put(entry.getName(), entry.getType());

		return map;
	}

}
