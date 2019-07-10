package com.github.TKnudsen.ComplexDataObject.data.complexDataObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
 * @version 1.02
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
}
