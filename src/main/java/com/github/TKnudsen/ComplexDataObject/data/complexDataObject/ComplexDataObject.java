package com.github.TKnudsen.ComplexDataObject.data.complexDataObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.events.IComplexDataObjectListener;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;
import com.github.TKnudsen.ComplexDataObject.data.keyValueObject.KeyValueObject;

/**
 * <p>
 * ComplexDataObject is a key-value store that can be used to describe complex
 * real-world objects.
 * 
 * For the use of ComplexDataObject in combination with DB solutions some
 * constructors allow the definition of the ID from an external competence.
 * 
 * Update: Changed KeyValueObject<Object> to the non-generic KeyValueObject
 * form.
 * 
 * Update: added expectedAttributeCount to the constructor to avoid resizing and
 * rehashing of the map. Especially for adding many attributes this should make
 * a considerable difference. For few expected attributes, a new
 * {@code SmallMap} Map implementation is now used in {@code KeyValueObject}.
 * </p>
 *
 * @version 1.11
 * @since 2015
 */
public class ComplexDataObject extends KeyValueObject implements ISelfDescription {

	@JsonIgnore
	protected static String NAME = "Name";

	@JsonIgnore
	protected static String DESCRIPTION = "Description";

	@JsonIgnore
	private List<IComplexDataObjectListener> listeners = new CopyOnWriteArrayList<>();

	@JsonIgnore
	private boolean enableListening = true;

	/*
	 * -------------------------------------------------------------------------
	 * Constructors
	 * ----------------------------------------------------------------------
	 */

	/** Default: 16 expected attributes, no ID, name="no name" */
	public ComplexDataObject() {
		this(10, "no name", "no description");
	}

	/** With expected attribute count (for performance tuning) */
	public ComplexDataObject(int expectedAttributeCount) {
		this(expectedAttributeCount, "no name", "no description");
	}

	/** With ID only */
	public ComplexDataObject(long id) {
		this(10, id, "no name", "no description");
	}

	/** With expected attribute count and ID */
	public ComplexDataObject(int expectedAttributeCount, long id) {
		this(expectedAttributeCount, id, "no name", "no description");
	}

	/** With name only */
	public ComplexDataObject(String name) {
		this(10, name, "no description");
	}

	/** With name and description */
	public ComplexDataObject(String name, String description) {
		this(10, name, description);
	}

	/** With ID, name, and description */
	public ComplexDataObject(Long id, String name, String description) {
		this(10, id, name, description);
	}

	/** Fully parameterized constructor without an ID */
	public ComplexDataObject(int expectedAttributeCount, String name, String description) {
		super(expectedAttributeCount);

		// ensure basic attributes
		if (name != null && !"no name".equals(name))
			setName(name);
		if (description != null && !"no description".equals(description))
			setDescription(description);
	}

	/** Fully parameterized constructor */
	public ComplexDataObject(int expectedAttributeCount, Long id, String name, String description) {
		super(expectedAttributeCount, id);

		// ensure basic attributes
		setName(name != null ? name : "no name");
		setDescription(description != null ? description : "no description");
	}

	/**
	 * @deprecated not called, and only does similar things like the
	 *             toLineString(String attribute) method in the super class.
	 * @return
	 */
	public String toStringInLine() {
		String output = "";
		for (String key : attributes.keySet())
			output += (attributes.get(key) == null) ? (key + attributes.get(key) + "/t")
					: (key + attributes.get(key).toString() + "/t");
		return output;
	}

	@Override
	public String toString() {
		String output = "Name: " + getName() + ", with " + attributes.size() + " attributes" + "\n";
		output += super.toString();

		return output;
	}

	@Override
	public String getName() {
		if (containsAttribute(NAME))
			if (getAttribute(NAME) != null)
				return getAttribute(NAME).toString();

		// return String.valueOf(getID());
		return "no name";
	}

	public void setName(String name) {
		this.add(NAME, name);
	}

	@Override
	public String getDescription() {
		if (containsAttribute(DESCRIPTION))
			if (getAttribute(DESCRIPTION) != null)
				return getAttribute(DESCRIPTION).toString();

		return "no description ";
	}

	public void setDescription(String description) {
		this.add(DESCRIPTION, description);
	}

	@Override
	public void add(String attribute, Object value) {
		super.add(attribute, value);

		fireAttributeValueChanged(attribute);
	}

	@Override
	public Object removeAttribute(String attribute) {
		Object removeAttribute = super.removeAttribute(attribute);

		fireAttributeRemoved(attribute);

		return removeAttribute;
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
			if (getAttribute(property) != null && getAttribute(property) != null
					&& getAttribute(property).getClass().equals(classType))
				if (!properties.contains(property))
					properties.add(property);
		return properties;
	}

	public List<IComplexDataObjectListener> getListeners() {
		return listeners;
	}

	public void addComplexDataObjectListener(IComplexDataObjectListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);

		this.listeners.add(listener);
	}

	public void removeComplexDataObjectListener(IComplexDataObjectListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	private final void fireAttributeValueChanged(String attribute) {
		if (enableListening)
			for (IComplexDataObjectListener listener : listeners)
				listener.attributeValueChanged(this, attribute);
	}

	private final void fireAttributeRemoved(String attribute) {
		if (enableListening)
			for (IComplexDataObjectListener listener : listeners)
				listener.attributeRemoved(this, attribute);
	}

	public boolean isEnableListening() {
		return enableListening;
	}

	public void setEnableListening(boolean enableListening) {
		this.enableListening = enableListening;
	}

}
