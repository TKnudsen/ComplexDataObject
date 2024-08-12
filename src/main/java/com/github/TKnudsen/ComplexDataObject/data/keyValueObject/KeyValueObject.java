package com.github.TKnudsen.ComplexDataObject.data.keyValueObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IKeyValueProvider;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;

/**
 * <p>
 * Title: KeyValueObject
 * </p>
 * 
 * <p>
 * Description: Basic data structure for an objects with keys/attributes and
 * values/objects.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2024
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */

public class KeyValueObject<V> implements IKeyValueProvider<V>, Iterable<String> {

	/**
	 * I regret that I found it necessary to have an ID field/attribute present
	 * always. In practice, even ID-based usage forms simply define a primary key
	 * attribute and do not make use of the ID attribute.
	 * 
	 * @deprecated Prepare for its deletion and replacement by a standard attribute.
	 */

	protected long ID;

	/**
	 * map with the attributes of the KeyValueObject. Historically, this was
	 * implemented as a SortedMap, even if this is not visible and exploited from
	 * the outside.
	 * 
	 * The decision was to go for a Map/HashMap implementation for performance
	 * reasons. In the unexpected case that errors such as sorted-attribute
	 * expectations occur, this decision may need to be reverted.
	 */
	// protected SortedMap<String, V> attributes = new TreeMap<String, V>();
	protected Map<String, V> attributes = new HashMap<>();

	public KeyValueObject() {
		this.ID = MathFunctions.randomLong();
	}

	public KeyValueObject(long ID) {
		this.ID = ID;
	}

	public KeyValueObject(Long ID) {
		if (ID == null)
			throw new IllegalArgumentException("ID was null");

		this.ID = ID.longValue();
	}

	@Override
	/**
	 * I regret that I found it necessary to have an ID field/attribute present
	 * always. In practice, even ID-based usage forms simply define a primary key
	 * attribute and do not make use of the ID attribute.
	 * 
	 * @deprecated Prepare for its deletion and replacement by a standard attribute.
	 */
	public long getID() {
		return ID;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(ID);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final KeyValueObject<?> other = (KeyValueObject<?>) obj;

		return this.hashCode() == other.hashCode() ? true : false;
	}

	/**
	 * Is true if the ComplexDataObject and a given Object have identical attributes
	 * and attribute values. ID, name and description are ignored.
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

		Set<String> keys = attributes.keySet();
		keys.addAll(other.keySet());

		for (String string : keys)
			if (!getAttribute(string).equals(other.getAttribute(string)))
				return false;

		return true;
	}

	public boolean containsAttribute(String attribute) {
		return attributes.containsKey(attribute);
	}

	@Override
	public void add(String attribute, V value) {
		attributes.put(attribute, value);
	}

	@Override
	public V getAttribute(String attribute) {
		return attributes.get(attribute);
	}

	@Override
	public Set<String> keySet() {
		return attributes.keySet();
	}

	/**
	 * convenient method identical with keySet();
	 * 
	 * @return
	 */
	public Set<String> getAttributes() {
		return attributes.keySet();
	}

	@Override
	public V removeAttribute(String attribute) {
		if (attributes.get(attribute) != null)
			return attributes.remove(attribute);
		return null;
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

	@Override
	public Iterator<String> iterator() {
		return attributes.keySet().iterator();
	}

	@Override
	public String toString() {
		String output = "";
		for (String key : attributes.keySet())
			output += (toLineString(key) + "\n");
		return output;
	}

	private String toLineString(String attribute) {
		String output = "Attribute: " + attribute + "\t";

		if (attributes.get(attribute) == null)
			output += ("Type: unknown\tValue: null");
		else
			output += ("Type: " + attributes.get(attribute).getClass() + "\t" + "Value: " + attributes.get(attribute));
		return output;
	}
}
