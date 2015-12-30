package data;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * <p>
 * Title: DataSchema
 * </p>
 * 
 * <p>
 * Description: Interface for data object schema definitions.
 * 
 * It is very similar to IDataObject, it actually inherit from it, but the
 * add(String, Object) method seems to be a bit too ambiguous.
 * 
 * Maybe use a builder for immutable schema objects.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */
public class DataSchema {
	private final String name;
	private final String description;

	// store each property into its own map (this way defaultValue and maybe
	// type could be sparse)?
	public final class SchemaEntry<T> {
		private final String name;
		private final Class<T> type;
		private final DataSchema typeSchema;
		private final T defaultValue;

		public SchemaEntry(String name, Class<T> type, T defaultValue) {
			this(name, type, defaultValue, null);
		}

		public SchemaEntry(String name, Class<T> type, T defaultValue, DataSchema typeSchema) {
			if (!IKeyValueStore.class.isAssignableFrom(type) && typeSchema != null) {
				throw new IllegalArgumentException("types with a defined typeSchema must inherit IDataObject");
			}

			this.name = name;
			this.type = type;
			this.typeSchema = typeSchema;
			this.defaultValue = defaultValue;
		}

		public String getName() {
			return name;
		}

		public Class<T> getType() {
			return type;
		}

		public DataSchema getTypeSchema() {
			return typeSchema;
		}

		public T getDefaultValue() {
			return defaultValue;
		}

		public boolean isComplexType() {
			return typeSchema != null;
		}

		@Override
		public String toString() {
			String output = "";
			output += ("Name: " + name + "\t" + "Type: " + type + "\t" + "Default Value: " + defaultValue);
			return output;
		}
	}

	// why sorted?!
	private final SortedMap<String, SchemaEntry<?>> attributes = new TreeMap<String, SchemaEntry<?>>();

	public DataSchema() {
		this(null, null);
	}

	public DataSchema(String name) {
		this(name, null);
	}

	public DataSchema(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/**
	 * Whether the DataSchema contains a given attribute.
	 * 
	 * @param key
	 * @return
	 */
	public boolean contains(String attribute) {
		return attributes.keySet().contains(attribute);
	}

	/**
	 * @return a string containing the name of the data schema.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return a string containing the description of the data schema.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the number of attributes defined in this schema.
	 */
	public int size() {
		return attributes.size();
	}

	@Override
	public String toString() {
		String output = "";
		for (String key : attributes.keySet())
			output += (key + ": " + attributes.get(key) + "\n");
		return output;
	}

	public String toStringInLine() {
		String output = "";
		for (String key : attributes.keySet())
			output += (key + attributes.get(key).toString() + "/t");
		return output;
	}

	/**
	 * @return a collection of the attributes names defined in this schema.
	 */
	public Collection<String> getAttributeNames() {
		return Collections.unmodifiableCollection(attributes.keySet());
	}

	/**
	 * @return a collection of the attributes entries contained in this schema.
	 */
	public Collection<SchemaEntry<?>> getAttributeEntries() {
		return Collections.unmodifiableCollection(attributes.values());
	}

	public SchemaEntry<?> getAttributeEntry(String attribute) {
		return attributes.get(attribute);
	}

	/**
	 * @return a map containing the types for each attribute defined in this
	 *         schema.
	 */
	public Map<String, Class<?>> getTypes() {
		return Collections.unmodifiableMap(attributes.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getType())));
	}

	/**
	 * @return a map containing the default values for each attribute defined in
	 *         this schema.
	 */
	public Map<String, Object> getDefaultValues() {
		return Collections.unmodifiableMap(attributes.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getDefaultValue())));
	}

	/**
	 * @param attribute
	 *            an attribute name.
	 * @return the type of the given attribute.
	 */
	public Class<?> getType(String attribute) {
		if (!attributes.containsKey(attribute)) {
			throw new IllegalArgumentException(String.format("unknown attribute name '%s'", attribute));
		}
		return attributes.get(attribute).getType();
	}

	/**
	 * @param attribute
	 *            an attribute name.
	 * @return the default value of the given attribute.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getDefaultValue(String attribute) {
		if (!attributes.containsKey(attribute)) {
			throw new IllegalArgumentException(String.format("unknown attribute name '%s'", attribute));
		}
		return (T) attributes.get(attribute).getDefaultValue();
	}

	/**
	 * Introduces or updates a new attribute to the data schema with 'null' as
	 * default value.
	 * 
	 * @param attribute
	 *            the attribute name
	 * @param defaultValue
	 *            the default value in case the
	 * @return the data schema instance for call-chaining.
	 */
	public <T> DataSchema add(String attribute, Class<T> type) {
		return add(attribute, type, null);
	}

	/**
	 * Introduces or updates a new attribute to the data schema.
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
		final SchemaEntry<T> entry = new SchemaEntry<T>(attribute, type, defaultValue);
		this.attributes.put(attribute, entry);

		return this;
	}

	/**
	 * Introduces or updates a new attribute to the data schema.
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
	public <T extends IKeyValueStore> DataSchema add(String attribute, Class<T> type, DataSchema dataSchema) {
		return add(attribute, type, dataSchema, null);
	}

	/**
	 * Introduces or updates a new attribute to the data schema.
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
	public <T extends IKeyValueStore> DataSchema add(String attribute, Class<T> type, DataSchema dataSchema, T defaultValue) {
		final SchemaEntry<T> entry = new SchemaEntry<T>(attribute, type, defaultValue, dataSchema);
		this.attributes.put(attribute, entry);

		return this;
	}

	/**
	 * Removes an attribute from the data schema.
	 * 
	 * @param attribute
	 *            the attribute name.
	 * @return the data schema instance for call-chaining.
	 */
	public DataSchema remove(String attribute) {
		this.attributes.remove(attribute);
		return this;
	}
}
