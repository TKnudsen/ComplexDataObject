package com.github.TKnudsen.ComplexDataObject.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IKeyValueProvider;

/**
 * <p>
 * Stores and manages collections of IKeyValueProvider objects, such as
 * ComplexDataObjects.
 * 
 * A DataSchema manages the keys/attributes of the collection.
 * 
 * A primary key attribute allows managing objects using a primary key. The ID
 * attribute is the historic default, but it can also be an attribute defined in
 * the constructor, such as the "ISIN" for stocks. Attention: do not use
 * attributes with non-categorical values, such as continuous numbers
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2024
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.07
 */
public class DataContainer<T extends IKeyValueProvider<Object>> implements Iterable<T> {

	protected static final String ID_ATTRIBUTE = "ID";

	/**
	 * attribute that defines the primary key of objects. Historic default:
	 * ID_ATTRIBUTE.
	 */
	private final String primaryKeyAttribute;

	/**
	 * needed in case that the ID is used as primary key. Then the getID function
	 * needs to be called once.
	 */
	private final boolean ID_as_PK;

	private Map<Object, T> objectsMap = new HashMap<>();

	/**
	 * Map of attributes, each storing a Map of T and corresponding attribute vales.
	 */
	protected Map<String, Map<T, Object>> attributeValues = new TreeMap<String, Map<T, Object>>();

	protected DataSchema dataSchema;

	/**
	 * @deprecated while based on a useful principle, practical use of DataContainer
	 *             has shown that constructors always accept objects, not data
	 *             schemas.
	 * @param dataSchema
	 */
	public DataContainer(DataSchema dataSchema) {
		this(dataSchema, ID_ATTRIBUTE);
	}

	/**
	 * @deprecated while based on a useful principle, practical use of DataContainer
	 *             has shown that constructors always accept objects, not data
	 *             schemas.
	 * 
	 * @param dataSchema
	 * @param primaryKeyAttribute A primary key attribute that allows managing
	 *                            objects having a primary key. The ID attribute is
	 *                            the historic default, but it can also be any other
	 *                            attribute defined here, such as the "ISIN" for
	 *                            stocks. Attention: do not use attributes with
	 *                            non-categorical values, such as continuous
	 *                            numbers.
	 */
	public DataContainer(DataSchema dataSchema, String primaryKeyAttribute) {
		if (primaryKeyAttribute == null) {
			System.err.println(this.getClass().getSimpleName()
					+ ": warning for a primary key attribute that is null. The container will use the ID attribute (historic default) instead");
			this.primaryKeyAttribute = ID_ATTRIBUTE;
		} else
			this.primaryKeyAttribute = primaryKeyAttribute;
		this.ID_as_PK = this.primaryKeyAttribute.equals(ID_ATTRIBUTE);

		this.dataSchema = dataSchema;
	}

	/**
	 * @deprecated Use the Iterable<T> constructor instead. Storage in the data
	 *             container now works with the objects directly, IDs have been
	 *             taken out of business.
	 * @param objectsMap
	 */
	public DataContainer(Map<Long, T> objectsMap) {
		this(objectsMap.values(), ID_ATTRIBUTE);
	}

	public DataContainer(T object) {
		this(Arrays.asList(object), ID_ATTRIBUTE);
	}

	public DataContainer(T object, String primaryKeyAttribute) {
		this(Arrays.asList(object), primaryKeyAttribute);
	}

	public DataContainer(Iterable<T> objects) {
		this(objects, ID_ATTRIBUTE);
	}

	/**
	 * 
	 * @param objects
	 * @param primaryKeyAttribute A primary key attribute that allows managing
	 *                            objects having a primary key. The ID attribute is
	 *                            the historic default, but it can also be any other
	 *                            attribute defined here, such as the "ISIN" for
	 *                            stocks. Attention: do not use attributes with
	 *                            non-categorical values, such as continuous
	 *                            numbers.
	 */
	public DataContainer(Iterable<T> objects, String primaryKeyAttribute) {
		if (primaryKeyAttribute == null)
			throw new IllegalArgumentException(this.getClass().getSimpleName()
					+ ": warning for a primary key attribute that is null. The container will use the ID attribute (historic default) instead");

		this.primaryKeyAttribute = primaryKeyAttribute;
		this.ID_as_PK = this.primaryKeyAttribute.equals(ID_ATTRIBUTE);

		dataSchema = new DataSchema();

		for (T object : objects) {
			Object pk = getPrimaryKey(object);
			if (pk == null)
				System.err.println(this.getClass().getSimpleName()
						+ ": warning for an input object with primary key that is null: " + primaryKeyAttribute);

			if (objectsMap.containsKey(pk))
				System.err.println(this.getClass().getSimpleName()
						+ ": warning for an input object with primary key that is already existing: "
						+ primaryKeyAttribute);

			objectsMap.put(pk, object);

			extendDataSchema(object);
		}

		// lazy implementation of attributeValues
		// for (String attribute : getAttributeNames())
		// calculateEntities(attribute);
	}

	/**
	 * utility method that provides the primary key (value) for the primary key
	 * attribute. Default: ID is the primary key attribute. Historic problem here:
	 * the getID method needs to be triggered such that the getAttribute(ID) will
	 * provide a (lazily computed) primary key value.
	 * 
	 * @param object
	 * @return
	 */
	private Object getPrimaryKey(T object) {
		if (object == null)
			return null;

		if (this.ID_as_PK)
			object.getID();

		return object.getAttribute(this.primaryKeyAttribute);
	}

	/**
	 * fast method to check if an object (represented through its primary key -
	 * default: the ID) is contained in the container.
	 * 
	 * @param primaryKey
	 * @return
	 */
	public boolean contains(Object primaryKey) {
		return objectsMap.containsKey(primaryKey);
	}

	/**
	 * adds a new object. updates data schema and attribute values.
	 * 
	 * @param object
	 * @return
	 */
	public boolean add(T object) {
		Object pk = getPrimaryKey(object);
		if (pk == null)
			throw new IllegalArgumentException(this.getClass().getSimpleName()
					+ ": object did not have the necessary primary key attribute " + primaryKeyAttribute);

		objectsMap.put(pk, object);

		extendDataSchema(object);

		for (String attribute : getAttributeNames())
			// lazy implementation of attributeValues
			if (attributeValues.get(attribute) != null)
				attributeValues.get(attribute).put(object, object.getAttribute(attribute));

		return true;
	}

	protected final void extendDataSchema(T object) {
		for (String string : object.keySet())
			if (!dataSchema.contains(string) && object.getAttribute(string) != null)
				dataSchema.add(string, object.getAttribute(string).getClass());
	}

	/**
	 * Introduces or updates a new attribute.
	 * 
	 *
	 * 
	 * @param attribute    the attribute name
	 * @param type         the expected data type.
	 * @param defaultValue the default value in case the attribute is missing from a
	 *                     data object.
	 * @return the data schema instance for call-chaining.
	 */
	public <A> DataSchema addAttribute(String attribute, Class<A> type, A defaultValue) {

		dataSchema.add(attribute, type, defaultValue);

		Iterator<T> objectIterator = iterator();
		while (objectIterator.hasNext()) {
			T next = objectIterator.next();
			if (next.getAttribute(attribute) == null)
				next.add(attribute, defaultValue);
		}

		return dataSchema;
	}

	/**
	 * 
	 * returns null if primary Key is null.
	 * 
	 * @param primaryKey
	 * @return
	 */
	public T get(Object primaryKey) {
		if (primaryKey == null)
			return null;

		return objectsMap.get(primaryKey);
	}

	/**
	 * Remove functionality for objects that are contained in the container.
	 * 
	 * @param object
	 * @return
	 */
	public boolean remove(T object) {
		if (object == null)
			return false;

		Object pk = getPrimaryKey(object);
		if (!objectsMap.containsKey(pk))
			return false;

		for (String attribute : attributeValues.keySet()) {
			if (attributeValues.get(attribute) != null)
				attributeValues.get(attribute).remove(object);
		}

		objectsMap.remove(object);

		return true;
	}

	/**
	 * Removes an attribute from the container and the set of objects.
	 * 
	 * @param attribute the attribute name.
	 * @return the data schema instance for call-chaining.
	 */
	public DataSchema remove(String attribute) {
		Iterator<T> iterator = iterator();
		while (iterator.hasNext()) {
			T o = iterator.next();
			o.removeAttribute(attribute);
		}

		return dataSchema.remove(attribute);
	}

	@Override
	public Iterator<T> iterator() {
		return objectsMap.values().iterator();
	}

	public Boolean isNumeric(String attribute) {
		if (!dataSchema.contains(attribute)) {
			System.err.println(this.getClass().getSimpleName() + ".isNumeric(attribute): attribute " + attribute
					+ " does not exist.");
			return false;
		}
		if (Number.class.isAssignableFrom(dataSchema.getType(attribute)))
			return true;
		return false;
	}

	public Boolean isBoolean(String attribute) {
		if (!dataSchema.contains(attribute))
			return false;
		if (Boolean.class.isAssignableFrom(dataSchema.getType(attribute)))
			return true;
		return false;
	}

	public Collection<String> getAttributeNames() {
		return dataSchema.getAttributeNames();
	}

	/**
	 * @deprecated Use getAttributeValueCollection instead.
	 * 
	 * @param attribute
	 * @return
	 */
	public Map<Long, Object> getAttributeValues(String attribute) {
		if (dataSchema.contains(attribute))
			// lazy implementation to save memory
			if (!attributeValues.containsKey(attribute))
				calculateEntities(attribute);

		Map<Long, Object> result = new HashMap<>();
		for (T t : attributeValues.get(attribute).keySet())
			result.put(t.getID(), t);

		return result;
	}

	public Collection<Object> getAttributeValueCollection(String attribute) {
		if (dataSchema.contains(attribute)) {
			// lazy implementation to save memory
			if (!attributeValues.containsKey(attribute))
				calculateEntities(attribute);

			// if (attributeValues.containsKey(attribute))
			if (attributeValues.get(attribute) != null)
				return attributeValues.get(attribute).values();
		}

		System.err.println(getClass().getSimpleName() + ".getAttributeValueCollection: attribute " + attribute
				+ " not part of the attribute set");
		return null;
	}

	public Class<?> getType(String attribute) {
		return dataSchema.getAttributeEntry(attribute).getType();
	}

	public Map<String, Class<?>> getSchema() {
		return DataSchemas.getClassMap(dataSchema);
	}

	/**
	 * @param attribute an attribute name.
	 * @return the default value of the given attribute.
	 */
	public <X> X getDefaultValue(String attribute) {
		return dataSchema.getDefaultValue(attribute);
	}

	public boolean contains(T object) {
		if (object == null)
			return false;

		Object pk = getPrimaryKey(object);
		if (objectsMap.containsKey(pk))
			return true;

		return false;
	}

	public boolean containsAttribute(String attribute) {
		return dataSchema.contains(attribute);
	}

	protected void calculateEntities(String attribute) {
		Map<T, Object> ent = new HashMap<T, Object>();

		Iterator<T> iterator = iterator();
		while (iterator.hasNext()) {
			T o = iterator.next();
			ent.put(o, o.getAttribute(attribute));
		}

		this.attributeValues.put(attribute, ent);
	}

	@Override
	public String toString() {
		String s = "Container with " + size() + " objects. Schema: ";
		if (dataSchema == null)
			s += super.toString();
		else
			s += dataSchema.toString();
		return s;
	}

	public int size() {
		if (objectsMap == null || objectsMap.isEmpty())
			return 0;
		return objectsMap.size();
	}

	public String getPrimaryKeyAttribute() {
		return primaryKeyAttribute;
	}

	public Set<Object> primaryKeySet() {
		return this.objectsMap.keySet();
	}
}
