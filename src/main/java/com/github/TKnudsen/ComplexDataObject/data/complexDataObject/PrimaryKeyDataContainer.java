package com.github.TKnudsen.ComplexDataObject.data.complexDataObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.data.DataContainers;

public class PrimaryKeyDataContainer extends ComplexDataContainer {

	private final String primaryKeyAttribute;

	/**
	 * always kept consistent with the baseline data structures.
	 */
	private Map<Object, ComplexDataObject> primaryKeysSelectedAttributesCDOMap = new HashMap<>();

	public PrimaryKeyDataContainer(Iterable<ComplexDataObject> objects, String primaryKeyAttribute) {
		super(objects);

		this.primaryKeyAttribute = primaryKeyAttribute;

		for (ComplexDataObject cdo : objects)
			primaryKeysSelectedAttributesCDOMap.put(cdo.getAttribute(primaryKeyAttribute), cdo);
	}

	public PrimaryKeyDataContainer(ComplexDataObject cdo, String primaryKeyAttribute) {
		this(new ComplexDataContainer(Arrays.asList(cdo)), primaryKeyAttribute);
	}

	public PrimaryKeyDataContainer(ComplexDataContainer dataContainer, String primaryKeyAttribute) {
		super(ComplexDataContainers.getObjectList(dataContainer));

		this.primaryKeyAttribute = primaryKeyAttribute;

		this.dataSchema = DataContainers.getDataSchema(dataContainer);

		for (ComplexDataObject cdo : ComplexDataContainers.getObjectList(dataContainer))
			primaryKeysSelectedAttributesCDOMap.put(cdo.getAttribute(primaryKeyAttribute), cdo);
	}

	public boolean contains(Object primaryKey) {
		return primaryKeysSelectedAttributesCDOMap.containsKey(primaryKey);
	}

	@Override
	public boolean add(ComplexDataObject cdo) {
		if (cdo.getAttribute(primaryKeyAttribute) == null)
			throw new IllegalArgumentException(
					"PrimaryKeyComplexDataStore: cdo did not have the necessary primary key attribute "
							+ primaryKeyAttribute);

		Object primaryKey = cdo.getAttribute(primaryKeyAttribute);
		if (primaryKeysSelectedAttributesCDOMap.containsKey(primaryKey))
			remove(primaryKeysSelectedAttributesCDOMap.get(primaryKey));

		primaryKeysSelectedAttributesCDOMap.put(primaryKey, cdo);

		return super.add(cdo);
	}

	/**
	 * 
	 * returns null if primaryKey is null.
	 * 
	 * @param primaryKey note: the primary key attribute does not need to be of type
	 *                   String.
	 * @return
	 */
	public ComplexDataObject get(Object primaryKey) {
		if (primaryKey == null)
			return null;

		if (getType(primaryKeyAttribute) != primaryKey.getClass())
			System.err.println(
					"PrimaryDataContainer - warning: object type of primaryKey does not match type of primary key attribute ("
							+ getType(primaryKeyAttribute) + " vs. " + primaryKey.getClass() + ")");

		return primaryKeysSelectedAttributesCDOMap.get(primaryKey);
	}

	@Override
	public boolean remove(ComplexDataObject object) {
		primaryKeysSelectedAttributesCDOMap.remove(object.getAttribute(primaryKeyAttribute));

		return super.remove(object);
	}

	public String getPrimaryKeyAttribute() {
		return primaryKeyAttribute;
	}

	public Set<Object> primaryKeySet() {
		return this.primaryKeysSelectedAttributesCDOMap.keySet();
	}

}
