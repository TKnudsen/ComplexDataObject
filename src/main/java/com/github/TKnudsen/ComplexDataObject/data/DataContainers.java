package com.github.TKnudsen.ComplexDataObject.data;

import java.util.HashMap;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

public class DataContainers {

	public static DataSchema getDataSchema(ComplexDataContainer dataContainer) {
		return dataContainer.dataSchema;
	}

	/**
	 * creates a map with the ID attribute of ComplexDataObjects as key.
	 * 
	 * @deprecated uses the ID attribute of ComplexDataObjects that was replaced by
	 *             a dynamic primary key concept. Those primary keys do not need to
	 *             be of type Long any more.
	 * @param attribute
	 * @return
	 */
	public static Map<Long, Object> getAttributeValues(DataContainer<ComplexDataObject> container, String attribute) {
		if (container.dataSchema.contains(attribute))
			// lazy implementation to save memory
			if (!container.attributeValues.containsKey(attribute))
				container.calculateEntities(attribute);

		Map<Long, Object> result = new HashMap<>();
		for (ComplexDataObject cdo : container.attributeValues.get(attribute).keySet())
			result.put(cdo.getID(), container.attributeValues.get(attribute).get(cdo));

		return result;
	}
}
