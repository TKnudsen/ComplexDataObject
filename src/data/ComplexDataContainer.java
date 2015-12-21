package data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ComplexDataContainer implements Iterable<ComplexDataObject> {

	private Map<Long, ComplexDataObject> objectsMap = new HashMap<Long, ComplexDataObject>();

	private Map<String, Map<Long, Object>> attributeValues = new HashMap<String, Map<Long, Object>>();

	private DataSchema dataSchema;

	public ComplexDataContainer(DataSchema dataSchema) {
		this.dataSchema = dataSchema;
	}

	public ComplexDataContainer(Map<Long, ComplexDataObject> objectsMap) {
		this.objectsMap = objectsMap;
		dataSchema = new DataSchema();
		for (Long ID : objectsMap.keySet())
			extendDataSchema(objectsMap.get(ID));
	}

	public ComplexDataContainer(Iterable<ComplexDataObject> objects) {
		dataSchema = new DataSchema();
		for (ComplexDataObject object : objects) {
			objectsMap.put(object.getID(), object);
			extendDataSchema(object);
		}
	}

	private void extendDataSchema(ComplexDataObject complexDataObject) {
		for (String string : complexDataObject)
			if (!dataSchema.contains(string) && complexDataObject.get(string) != null)
				dataSchema.add(string, complexDataObject.get(string).getClass());
	}

	/**
	 * Introduces or updates a new attribute.
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
	public <T> DataSchema add(String attribute, Class<T> type, T defaultValue) {
		// TODO What about the default value? Should it be delegated to the
		// ComplexDataObjects?

		attributeValues = new HashMap<String, Map<Long, Object>>();

		return dataSchema.add(attribute, type, defaultValue);
	}

	/**
	 * Removes an attribute from the container and the set of
	 * ComplexDataObjects.
	 * 
	 * @param attribute
	 *            the attribute name.
	 * @return the data schema instance for call-chaining.
	 */
	public DataSchema remove(String attribute) {
		Iterator<ComplexDataObject> iterator = iterator();
		while (iterator.hasNext())
			iterator.next().remove(attribute);

		return dataSchema.remove(attribute);
	}

	@Override
	public Iterator<ComplexDataObject> iterator() {
		return objectsMap.values().iterator();
	}

	public Boolean isNumeric(String attribute) {
		if (Number.class.isAssignableFrom(dataSchema.getType(attribute)))
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

	private void calculateEntities(String attribute) {
		Map<Long, Object> ent = new HashMap<Long, Object>();
		for (Long l : objectsMap.keySet())
			if (objectsMap.get(l).get(attribute) != null)
				ent.put(l, objectsMap.get(l).get(attribute));
		this.attributeValues.put(attribute, ent);
	}

	public int size() {
		if (objectsMap == null)
			return 0;
		return objectsMap.size();
	}
}
