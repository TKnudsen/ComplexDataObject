package com.github.TKnudsen.ComplexDataObject.data.keyValueObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
 * 
 * Update: Changed KeyValueObject<V> to the non-generic KeyValueObject form
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2024
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */

public class KeyValueObject implements IKeyValueProvider<Object>, Iterable<String> {

	/**
	 * I regret that I found it necessary to have an ID field/attribute present
	 * always. In practice, even ID-based usage forms simply define a primary key
	 * attribute and do not make use of the ID attribute.
	 * 
	 * Prepare for its deletion and replacement by a standard attribute.
	 */
	// protected long ID;
	public final String ID = "ID";

	/**
	 * map with the attributes of the KeyValueObject. Historically, this was
	 * implemented as a SortedMap, even if this is not visible and exploited from
	 * the outside.
	 * 
	 * The decision was to go for a Map/HashMap implementation for performance
	 * reasons. In the unexpected case that errors such as sorted-attribute
	 * expectations occur, this decision may need to be reverted.
	 */
	// protected SortedMap<String, Object> attributes = new TreeMap<String,
	// Object>();
	protected Map<String, Object> attributes = new HashMap<>();

	public KeyValueObject() {
	}

	/**
	 * @deprecated better add IDs as key-value pairs.
	 * 
	 * @param ID use add(KeyValueObject.ID, yourID) instead
	 */
	public KeyValueObject(long ID) {
		attributes.put(this.ID, ID);
	}

	/**
	 * @deprecated better add IDs as key-value pairs.
	 * 
	 * @param ID use add(KeyValueObject.ID, yourID) instead
	 */
	public KeyValueObject(Long ID) {
		if (ID == null)
			throw new IllegalArgumentException("ID was null");

		attributes.put(this.ID, ID.longValue());
	}

	@Override
	/**
	 * I regret that I found it necessary to have an ID field/attribute present
	 * always. In practice, even ID-based usage forms simply define a primary key
	 * attribute and do not make use of the ID attribute.
	 * 
	 * @deprecated Prepare to make it protected. The primary key should not need to
	 *             be limited to the type long.
	 */
	public long getID() {
		if (!keySet().contains(this.ID))
			attributes.put(this.ID, MathFunctions.randomLong());

		if (!(getAttribute(this.ID) instanceof Number))
			attributes.put(this.ID, MathFunctions.randomLong());

		return ((Number) getAttribute(this.ID)).longValue();
	}

	@Override
	public int hashCode() {
		return Long.hashCode(getID());
	}

	@Override
	/**
	 * Revised version does not instantiate a KeyValueObject any more for the other
	 * object
	 */
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		return this.hashCode() == ((KeyValueObject) obj).hashCode() ? true : false;
	}

	/**
	 * True if this and an object have identical attributes and values. Revised
	 * version does not instantiate a KeyValueObject any more for the other object.
	 * 
	 * @param obj
	 * @return
	 */
	public boolean equalValues(Object obj) {
		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		if (!this.keySet().equals(((KeyValueObject) obj).keySet()))
			return false;

		for (String string : keySet())
			if (!getAttribute(string).equals(((KeyValueObject) obj).getAttribute(string)))
				return false;

		return true;
	}

	public boolean containsAttribute(String attribute) {
		return attributes.containsKey(attribute);
	}

	@Override
	public void add(String attribute, Object value) {
		attributes.put(attribute, value);
	}

	@Override
	public Object getAttribute(String attribute) {
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
	public Object removeAttribute(String attribute) {
		return attributes.remove(attribute);
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

		SortedSet<String> a = new TreeSet<>(attributes.keySet());
		for (String key : a)
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
