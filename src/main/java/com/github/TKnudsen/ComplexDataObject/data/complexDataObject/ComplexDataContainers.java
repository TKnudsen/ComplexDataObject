package com.github.TKnudsen.ComplexDataObject.data.complexDataObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.data.DataSchema;

/**
 * <p>
 * Description: Little helpers for ComplexDataContainers
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017-2021
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.05
 */
public class ComplexDataContainers {

	public static List<ComplexDataObject> getObjectList(ComplexDataContainer container) {
		List<ComplexDataObject> list = new ArrayList<>();

		for (ComplexDataObject cdo : container)
			list.add(cdo);

		return list;
	}

	public static ComplexDataContainer createComplexDataContainer(
			Iterable<? extends ComplexDataObject> complexDataObjects) {

		List<ComplexDataObject> list = new ArrayList<>();

		for (ComplexDataObject cdo : complexDataObjects)
			list.add(cdo);

		return new ComplexDataContainer(list);
	}

	public static ComplexDataContainer createComplexDataContainer(ComplexDataObject cdo) {
		Objects.requireNonNull(cdo);

		return new ComplexDataContainer(Arrays.asList(new ComplexDataObject[] { cdo }));

	}

	public static DataSchema dataSchema(ComplexDataContainer container) {
		DataSchema schema = new DataSchema();

		for (String attribute : container.getAttributeNames()) {
			schema.add(attribute, container.getType(attribute), container.getDefaultValue(attribute));
		}

		return schema;
	}

	/**
	 * 
	 * @param containers
	 * @param primaryAttribute the attribute that is supposed to be contained in any
	 *                         container. will be used as a basis for identifying
	 *                         and merging ComplexDataObjects within individual
	 *                         containers.
	 * @return new ComplexDataContainer. Schema information of old containers gets
	 *         lost.
	 */
	public static ComplexDataContainer mergeContainers(Collection<ComplexDataContainer> containers,
			String primaryAttribute) {

		Objects.requireNonNull(containers);

		Map<String, List<ComplexDataObject>> objects = new HashMap<String, List<ComplexDataObject>>();

		for (ComplexDataContainer container : containers)
			if (container.getAttributeNames().contains(primaryAttribute))
				for (ComplexDataObject cdo : container) {
					if (cdo.getAttribute(primaryAttribute) != null) {
						String attributeValue = cdo.getAttribute(primaryAttribute).toString();

						if (objects.get(attributeValue) == null)
							objects.put(attributeValue, new ArrayList<>());

						objects.get(attributeValue).add(cdo);
					}
				}

		List<ComplexDataObject> mergedCDOs = new ArrayList<>();
		for (String attributeValue : objects.keySet())
			mergedCDOs.add(ComplexDataObjects.merge(objects.get(attributeValue)));

		return new ComplexDataContainer(mergedCDOs);
	}
}
