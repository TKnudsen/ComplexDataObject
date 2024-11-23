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
 * Description: Little helpers for ComplexDataContainers. Note that
 * ComplexDataContainer is now more powerful: it has a primaryKeyAttribute
 * extension that shall be used if the primary key attribute is to be determined
 * explicitly. Also, new constructors are available, making several methods here
 * obsolete.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017-2024
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.06
 */
public class ComplexDataContainers {

	public static List<ComplexDataObject> getObjectList(ComplexDataContainer container) {
		List<ComplexDataObject> list = new ArrayList<>();

		for (ComplexDataObject cdo : container)
			list.add(cdo);

		return list;
	}

	/**
	 * @deprecated create ComplexDataContainer directly, with the new constructor.
	 *             Note that it is preferable to determine the primary key with a
	 *             second constructor parameter
	 * @param complexDataObjects
	 * @return
	 */
	public static ComplexDataContainer createComplexDataContainer(
			Iterable<? extends ComplexDataObject> complexDataObjects) {

		List<ComplexDataObject> list = new ArrayList<>();

		for (ComplexDataObject cdo : complexDataObjects)
			list.add(cdo);

		return new ComplexDataContainer(list);
	}

	/**
	 * @deprecated create ComplexDataContainer directly, with the new constructor.
	 * @param complexDataObjects
	 * @return
	 */
	public static ComplexDataContainer createComplexDataContainer(
			Iterable<? extends ComplexDataObject> complexDataObjects, String primaryKeyAttribute) {
		Objects.requireNonNull(primaryKeyAttribute);

		List<ComplexDataObject> list = new ArrayList<>();

		for (ComplexDataObject cdo : complexDataObjects)
			list.add(cdo);

		return new ComplexDataContainer(list, primaryKeyAttribute);
	}

	/**
	 * @deprecated create ComplexDataContainer directly, with the new constructor.
	 * @param cdo
	 * @return
	 */
	public static ComplexDataContainer createComplexDataContainer(ComplexDataObject cdo) {
		Objects.requireNonNull(cdo);

		return new ComplexDataContainer(Arrays.asList(new ComplexDataObject[] { cdo }));

	}

	/**
	 * @deprecated create ComplexDataContainer directly, with the new constructor.
	 * @param cdo
	 * @return
	 */
	public static ComplexDataContainer createComplexDataContainer(ComplexDataObject cdo, String primaryKeyAttribute) {
		Objects.requireNonNull(cdo);
		Objects.requireNonNull(primaryKeyAttribute);

		return new ComplexDataContainer(Arrays.asList(new ComplexDataObject[] { cdo }), primaryKeyAttribute);

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
		Objects.requireNonNull(primaryAttribute);

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

		return new ComplexDataContainer(mergedCDOs, primaryAttribute);
	}

	/**
	 * 
	 * Removes the ComplexDataContainer as a listener from the ComplexDataObjects it
	 * contains.
	 * 
	 * Very useful if a ComplexDataContainer is at the end of life, wants to go into
	 * the garbage container, but can't, due to the references from the
	 * ComplexDataObjects to their listening container.
	 * 
	 * @param container
	 */
	public static void removeAsListener(ComplexDataContainer container) {
		for (ComplexDataObject cdo : container)
			cdo.removeComplexDataObjectListener(container);
	}
}
