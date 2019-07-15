package com.github.TKnudsen.ComplexDataObject.data.complexDataObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.data.DataSchema;

/**
 * <p>
 * Title: ComplexDataContainerTools
 * </p>
 * 
 * <p>
 * Description: Little helpers for ComplexDataContainers
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017-2019
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class ComplexDataContainers {

	public static List<ComplexDataObject> getObjectList(ComplexDataContainer container) {
		List<ComplexDataObject> list = new ArrayList<>();

		Iterator<ComplexDataObject> iterator = container.iterator();

		while (iterator.hasNext())
			list.add(iterator.next());

		return list;
	}

	public static ComplexDataContainer createComplexDataContainer(ComplexDataObject cdo) {
		Objects.requireNonNull(cdo);

		return new ComplexDataContainer(Arrays.asList(new ComplexDataObject[] { cdo }));

	}

	public static DataSchema deduceDataSchema(ComplexDataContainer container) {
		DataSchema schema = new DataSchema();

		for (String attribute : container.getAttributeNames()) {
			schema.add(attribute, container.getType(attribute), container.getDefaultValue(attribute));
		}

		return schema;
	}
}
