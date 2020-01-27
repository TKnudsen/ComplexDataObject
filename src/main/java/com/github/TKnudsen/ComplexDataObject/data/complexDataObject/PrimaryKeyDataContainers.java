package com.github.TKnudsen.ComplexDataObject.data.complexDataObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PrimaryKeyDataContainers {

	public static PrimaryKeyDataContainer createPrimaryKeyDataContainer(ComplexDataObject cdo,
			String primaryKeyAttribute) {
		Objects.requireNonNull(cdo);

		return new PrimaryKeyDataContainer(Arrays.asList(cdo), primaryKeyAttribute);

	}

	public static PrimaryKeyDataContainer mergeContainers(Collection<PrimaryKeyDataContainer> containers,
			String primaryKeyAttribute) {

		Objects.requireNonNull(containers);

		Map<String, List<ComplexDataObject>> objects = new HashMap<String, List<ComplexDataObject>>();

		for (PrimaryKeyDataContainer container : containers)
			if (container.getAttributeNames().contains(primaryKeyAttribute))
				for (ComplexDataObject cdo : container) {
					if (cdo.getAttribute(primaryKeyAttribute) != null) {
						String attributeValue = cdo.getAttribute(primaryKeyAttribute).toString();

						if (objects.get(attributeValue) == null)
							objects.put(attributeValue, new ArrayList<>());

						objects.get(attributeValue).add(cdo);
					}
				}

		List<ComplexDataObject> mergedCDOs = new ArrayList<>();
		for (String attributeValue : objects.keySet())
			mergedCDOs.add(ComplexDataObjectTools.mergeObjects(objects.get(attributeValue)));

		return new PrimaryKeyDataContainer(mergedCDOs, primaryKeyAttribute);
	}
}
