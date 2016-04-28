package com.github.TKnudsen.ComplexDataObject.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IKeyValueStore;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.ITextDescription;

/**
 * <p>
 * Title: ComplexDataObject
 * </p>
 * 
 * <p>
 * Description: ComplexDataObject is a key-value store that can be used to model
 * complex real-world objects.
 * 
 * For the use of ComplexDataObject in combination with DB solutions some
 * constructors allow the definition of the ID from an external competence.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class ComplexDataObject implements IDObject, IKeyValueStore, ITextDescription, Iterable<String> {

	protected long ID;
	protected String name;
	protected String description;

	protected SortedMap<String, Object> attributes = new TreeMap<String, Object>();

	public ComplexDataObject() {
		this.ID = getRandomLong();
	}

	/**
	 * @deprecated use long instead of Long.
	 * @param ID
	 */
	public ComplexDataObject(Long ID) {
		if (ID == null)
			throw new IllegalArgumentException("ID was null");
		this.ID = ID;
	}

	public ComplexDataObject(long ID) {
		this.ID = ID;
	}

	public ComplexDataObject(String name, String description) {
		this.ID = getRandomLong();
		this.name = name;
		this.description = description;
	}

	public ComplexDataObject(Long ID, String name, String description) {
		if (ID == null)
			throw new IllegalArgumentException("ID was null");
		this.ID = ID;
		this.name = name;
		this.description = description;
	}

	/**
	 * Little helper for the generation of a unique identifier.
	 * 
	 * @return unique ID
	 */
	private long getRandomLong() {
		return UUID.randomUUID().getMostSignificantBits();
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<String> keySet() {
		return attributes.keySet();
	}

	@Override
	public Iterator<String> iterator() {
		return attributes.keySet().iterator();
	}

	@Override
	public Object remove(String attribute) {
		if (attributes.get(attribute) != null)
			return attributes.remove(attribute);
		return null;
	}

	@Override
	public String toString() {
		String output = "";
		for (String key : attributes.keySet())
			output += (toLineString(key) + "\n");
		return output;
	}

	private String toLineString(String attribute) {
		if (attributes.get(attribute) == null)
			return "";

		String output = "";
		output += ("Attribute: " + attribute + "\t" + "Type: " + attributes.get(attribute).getClass() + "\t" + "Value: " + attributes.get(attribute));
		return output;
	}

	public String toStringInLine() {
		String output = "";
		for (String key : attributes.keySet())
			output += (key + attributes.get(key).toString() + "/t");
		return output;
	}

	@Override
	public long getID() {
		return ID;
	}

	@Override
	public int size() {
		if (attributes == null)
			return 0;
		return attributes.size();
	}

	@Override
	public String getName() {
		if (name == null)
			return toString();
		return name;
	}

	@Override
	public String getDescription() {
		if (description == null)
			return toString();
		return description;
	}

	@Override
	public void add(String attribute, Object value) {
		attributes.put(attribute, value);
	}

	@Override
	public Object get(String attribute) {
		return attributes.get(attribute);
	}

	@Override
	public Class<?> getType(String attribute) {
		if (attributes.get(attribute) != null)
			return attributes.get(attribute).getClass();
		return null;
	}

	@Override
	public Map<String, Class<?>> getTypes() {
		Map<String, Class<?>> ret = new HashMap<>();
		for (String string : attributes.keySet())
			if (attributes.get(string) == null)
				ret.put(string, null);
			else
				ret.put(string, attributes.get(string).getClass());
		return null;
	}

	/**
	 * retrieves all attributes for objects matching a given class type.
	 * 
	 * @param classType
	 * @return List of attributes
	 */
	public List<String> getAttributes(Class<?> classType) {
		List<String> properties = new ArrayList<>();
		for (String property : attributes.keySet())
			if (get(property) != null && get(property) != null && get(property).getClass().equals(classType))
				if (!properties.contains(property))
					properties.add(property);
		return properties;
	}

	@Override
	public int hashCode() {
		return (int) ID;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ComplexDataObject other = (ComplexDataObject) obj;
		return this.hashCode() == other.hashCode() ? true : false;
	}

	/**
	 * Is true if the ComplexDataObject and a given Object have identical
	 * attributes and attribute values. ID, name and description are ignored.
	 * 
	 * @param obj
	 * @return
	 */
	public boolean equalValues(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ComplexDataObject other = (ComplexDataObject) obj;

		if (size() != other.size())
			return false;

		Set<String> keys = attributes.keySet();
		keys.addAll(other.keySet());

		for (String string : keys)
			if (!get(string).equals(other.get(string)))
				return false;

		return true;
	}
}
