package com.github.TKnudsen.ComplexDataObject.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IKeyValueProvider;

/**
 * <p>
 * Title: DataContainer
 * </p>
 * 
 * <p>
 * Description: Stores and manages collections of IDObjects. A DataSchema
 * manages the keys/attributes of the collection iff the objects are instanceOf
 * IKeyValueProvider.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */
public class DataContainer<T extends IKeyValueProvider<Object>> implements Iterable<T> {

	private Map<Long, T> objectsMap = new HashMap<Long, T>();

	protected Map<String, Map<Long, Object>> attributeValues = new HashMap<String, Map<Long, Object>>();

	protected DataSchema dataSchema;

	public DataContainer(DataSchema dataSchema) {
		this.dataSchema = dataSchema;
	}

	public DataContainer(Map<Long, T> objectsMap) {
		this.objectsMap = objectsMap;
		dataSchema = new DataSchema();
		for (Long ID : objectsMap.keySet())
			extendDataSchema(objectsMap.get(ID));
	}

	public DataContainer(Iterable<T> objects) {
		dataSchema = new DataSchema();
		for (T object : objects) {
			objectsMap.put(object.getID(), object);
			extendDataSchema(object);
		}
	}

	private void extendDataSchema(T object) {
		if (object instanceof IKeyValueProvider) {
			IKeyValueProvider<?> keyValueProvider = (IKeyValueProvider<?>) object;
			for (String string : keyValueProvider.keySet())
				if (string instanceof String)
					if (!dataSchema.contains(string) && keyValueProvider.get(string) != null)
						dataSchema.add(string, keyValueProvider.get(string).getClass());
		}
	}

	/**
	 * Introduces or updates a new attribute.
	 * 
	 *
	 * 
	 * @param attribute
	 *            the attribute name
	 * @param type
	 *            the expected data type.
	 * @param defaultValue
	 *            the default value in case the attribute is missing from a data
	 *            object.
	 * @return the data schema instance for call-chaining.
	 */
	public <A> DataSchema addAttribute(String attribute, Class<A> type, A defaultValue) {
		// TODO t makes no sense. a new attribute is introduced here - not a new
		// object consisting of multiple attributes

		// either a new object is introduced or a new attribute.
		attributeValues = new HashMap<String, Map<Long, Object>>();

		dataSchema.add(attribute, type, defaultValue);

		Iterator<T> objectIterator = iterator();
		while (objectIterator.hasNext()) {
			T next = objectIterator.next();
			if (next.get(attribute) == null)
				next.add(attribute, defaultValue);
		}

		return dataSchema;
	}

	/**
	 * Remove functionality. For test purposes. Maybe this functionality will be
	 * removed sometime.
	 * 
	 * @param object
	 * @return
	 */
	public boolean remove(T object) {
		if (object == null)
			return false;

		long id = object.getID();
		if (!objectsMap.containsKey(id))
			return false;

		for (String attribute : attributeValues.keySet()) {
			if (attributeValues.get(attribute) != null)
				attributeValues.get(attribute).remove(id);
		}

		objectsMap.remove(id);

		return true;
	}

	/**
	 * Removes an attribute from the container and the set of objects.
	 * 
	 * @param attribute
	 *            the attribute name.
	 * @return the data schema instance for call-chaining.
	 */
	public DataSchema remove(String attribute) {
		Iterator<T> iterator = iterator();
		while (iterator.hasNext()) {
			T o = iterator.next();
			o.remove(attribute);
		}

		return dataSchema.remove(attribute);
	}

	@Override
	public Iterator<T> iterator() {
		return objectsMap.values().iterator();
	}

	public Boolean isNumeric(String attribute) {
		if (Number.class.isAssignableFrom(dataSchema.getType(attribute)))
			return true;
		return false;
	}

	public Boolean isBoolean(String attribute) {
		if (Boolean.class.isAssignableFrom(dataSchema.getType(attribute)))
			return true;
		return false;
	}

	public Collection<String> getAttributeNames() {
		return dataSchema.getAttributeNames();
	}

	public Map<Long, Object> getAttributeValues(String attribute) {
		if (attributeValues.get(attribute) == null) {
			calculateEntities(attribute);
		}
		return attributeValues.get(attribute);
	}

	public boolean contains(T object) {
		if (objectsMap.containsKey(object.getID()))
			return true;
		return false;
	}

	private void calculateEntities(String attribute) {
		Map<Long, Object> ent = new HashMap<Long, Object>();

		Iterator<T> iterator = iterator();
		while (iterator.hasNext()) {
			T o = iterator.next();
			if (o instanceof IKeyValueProvider)
				ent.put(o.getID(), o.get(attribute));
		}

		this.attributeValues.put(attribute, ent);
	}

	@Override
	public String toString() {
		if (dataSchema == null)
			return super.toString();
		return dataSchema.toString();
	}

	public int size() {
		if (objectsMap == null)
			return 0;
		return objectsMap.size();
	}
}
