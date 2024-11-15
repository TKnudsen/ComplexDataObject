package com.github.TKnudsen.ComplexDataObject.data.complexDataObject;

/**
 * <p>
 * Title: PrimaryKeyDataContainer
 * </p>
 * 
 * <p>
 * Description: PrimaryKeyDataContainer stores and manages ComplexDataObjects.
 * 
 * A DataSchema contains all keys of the ComplexDataObjects.
 * 
 * The primary key specifics enables the quick lookup of objects with
 * primary-key characteristics.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2020-2024
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 * @deprecated the inherited DataContainer now contains a more flexible primary
 *             key implementation that adopts the best-practice functionality
 *             from PrimaryKeyDataContainer completely. Just just
 *             ComplexDataContainer and provide a primaryKey attribute;
 *             PrimaryKeyDataContainer is not needed any more. The new solution
 *             is also more scalable, as it consumes less memory and does a
 *             little bit less compute.
 */
public class PrimaryKeyDataContainer extends ComplexDataContainer {

//	private String primaryKeyAttribute;
//
//	/**
//	 * always kept consistent with the baseline data structures.
//	 */
//	private Map<Object, ComplexDataObject> primaryKeysSelectedAttributesCDOMap = new HashMap<>();

	public PrimaryKeyDataContainer(Iterable<ComplexDataObject> objects, String primaryKeyAttribute) {
		super(objects, primaryKeyAttribute);

//		this.primaryKeyAttribute = primaryKeyAttribute;
//
//		for (ComplexDataObject cdo : objects) {
//			Object pk = cdo.getAttribute(primaryKeyAttribute);
//			if (pk == null)
//				System.err.println(
//						"PrimaryKeyDataContainer: warning for a ComplexDataObject that does not contain a value for primary key "
//								+ primaryKeyAttribute);
//			if (primaryKeysSelectedAttributesCDOMap.containsKey(pk))
//				System.err.println(
//						"PrimaryKeyDataContainer: warning for a ComplexDataObject that collides with an existing one with primary key "
//								+ primaryKeyAttribute);
//			primaryKeysSelectedAttributesCDOMap.put(pk, cdo);
//		}
	}

	public PrimaryKeyDataContainer(ComplexDataObject cdo, String primaryKeyAttribute) {
		super(cdo, primaryKeyAttribute);
	}

	public PrimaryKeyDataContainer(ComplexDataContainer dataContainer, String primaryKeyAttribute) {
		super(ComplexDataContainers.getObjectList(dataContainer), primaryKeyAttribute);

//		this.primaryKeyAttribute = primaryKeyAttribute;
//
//		this.dataSchema = DataContainers.getDataSchema(dataContainer);
//
//		for (ComplexDataObject cdo : ComplexDataContainers.getObjectList(dataContainer))
//			primaryKeysSelectedAttributesCDOMap.put(cdo.getAttribute(primaryKeyAttribute), cdo);
	}

//	public boolean contains(Object primaryKey) {
//		return primaryKeysSelectedAttributesCDOMap.containsKey(primaryKey);
//	}

//	@Override
//	public boolean add(ComplexDataObject cdo) {
//		if (cdo.getAttribute(primaryKeyAttribute) == null)
//			throw new IllegalArgumentException(
//					"PrimaryKeyComplexDataStore: cdo did not have the necessary primary key attribute "
//							+ primaryKeyAttribute);
//
//		Object primaryKey = cdo.getAttribute(primaryKeyAttribute);
//		if (primaryKeysSelectedAttributesCDOMap.containsKey(primaryKey))
//			remove(primaryKeysSelectedAttributesCDOMap.get(primaryKey));
//
//		primaryKeysSelectedAttributesCDOMap.put(primaryKey, cdo);
//
//		return super.add(cdo);
//	}

//	@Override
//	public boolean contains(ComplexDataObject object) {
//		if (object == null)
//			return false;
//
//		if (primaryKeysSelectedAttributesCDOMap.containsKey(object.getAttribute(primaryKeyAttribute)))
//			return true;
//
//		return false;
//	}

//	/**
//	 * 
//	 * returns null if primaryKey is null.
//	 * 
//	 * @param primaryKey note: the primary key attribute does not need to be of type
//	 *                   String.
//	 * @return
//	 */
//	public ComplexDataObject get(Object primaryKey) {
//		if (primaryKey == null)
//			return null;
//
//		return primaryKeysSelectedAttributesCDOMap.get(primaryKey);
//	}

//	@Override
//	public boolean remove(ComplexDataObject object) {
//		primaryKeysSelectedAttributesCDOMap.remove(object.getAttribute(primaryKeyAttribute));
//
//		return super.remove(object);
//	}

//	public String getPrimaryKeyAttribute() {
//		return primaryKeyAttribute;
//	}

//	public Set<Object> primaryKeySet() {
//		return this.primaryKeysSelectedAttributesCDOMap.keySet();
//	}

}
