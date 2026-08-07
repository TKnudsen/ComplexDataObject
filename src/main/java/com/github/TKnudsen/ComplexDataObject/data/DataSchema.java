package com.github.TKnudsen.ComplexDataObject.data;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IKeyValueProvider;

/**
 * <p>
 * Contains and maintains the keys of a given set of key-value attributes.
 * Can be seen as a sort of header for tabular data sets.
 *
 * Maintains a attributes in a sorted way, but uses a LinkedHashMap internally
 * for better performance. Performance can be exploited when no sorted
 * attributes are needed.
 * </p>
 *
 * @version 1.03
 * @since 2015
 */
public class DataSchema {
	private final String name;
	private final String description;

	private final Map<String, DataSchemaEntry<?>> attributes = new LinkedHashMap<>();

	// ---- Sorted view cache (invalidated on mutation) ----
	private transient boolean sortedDirty = true;
	private transient NavigableMap<String, DataSchemaEntry<?>> sortedCache;
	private transient Map<String, Class<?>> cachedTypes;
	private transient Map<String, Object> cachedDefaultValues;

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
	 * Returns a cached sorted-by-name view of attributes. Recomputed only after
	 * mutations (add/remove).
	 */
	private synchronized NavigableMap<String, DataSchemaEntry<?>> sorted() {
		if (sortedDirty || sortedCache == null) {
			sortedCache = new TreeMap<>(attributes);
			sortedDirty = false;
		}
		return sortedCache;
	}

	/** Marks cached derived views as stale. Call on any mutation. */
	private void invalidateCaches() {
		sortedDirty = true;
		sortedCache = null;
		cachedTypes = null;
		cachedDefaultValues = null;
	}

	/**
	 * whether the DataSchemaEntry contains a given attribute.
	 * 
	 * @param attribute
	 * @return
	 */
	public boolean contains(String attribute) {
		return attributes.containsKey(attribute);
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
		StringBuilder output = new StringBuilder("DataSchema with ").append(attributes.size()).append(" attributes\n");

		// Use cached sorted view
		for (DataSchemaEntry<?> entry : sorted().values()) {
			output.append(entry).append("\n");
		}

		return output.toString();
	}

	public String toStringInLine() {
		StringBuilder output = new StringBuilder();

		// Use cached sorted view
		for (Map.Entry<String, DataSchemaEntry<?>> e : sorted().entrySet()) {
			output.append(e.getKey()).append(e.getValue()).append("\t");
		}

		return output.toString();
	}

	/**
	 * Returns an unsorted collection of attribute names in insertion order. This is
	 * faster than {@link #getAttributeNames()} as it doesn't require sorting.
	 * 
	 * @return unmodifiable collection of attribute names
	 */
	public Collection<String> getAttributes() {
		return Collections.unmodifiableCollection(attributes.keySet());
	}

	/**
	 * @deprecated if sorted names are needed, go for getAttributesSorted()
	 * @return a sorted collection of the attribute names defined in this schema.
	 * 
	 *         Note: This is a live view over the cached sorted map; cache is
	 *         invalidated on add/remove.
	 */
	public Collection<String> getAttributeNames() {
		return Collections.unmodifiableNavigableSet(sorted().navigableKeySet());
	}

	/**
	 * @return a sorted collection of the attribute names defined in this schema.
	 * 
	 *         Note: This is a live view over the cached sorted map; cache is
	 *         invalidated on add/remove.
	 */
	public Collection<String> getAttributesSorted() {
		return Collections.unmodifiableNavigableSet(sorted().navigableKeySet());
	}

	/**
	 * @return a collection of the attributes entries contained in this schema.
	 */
	public Collection<DataSchemaEntry<?>> getAttributeEntries() {
		return getAttributeEntries(true);
	}

	/**
	 * @return a sorted collection of the attribute entries contained in this
	 *         schema.
	 */
	public Collection<DataSchemaEntry<?>> getAttributeEntries(boolean sorted) {
		if (sorted)
			// Live view over cached sorted map
			return Collections.unmodifiableCollection(sorted().values());
		else
			return Collections.unmodifiableCollection(attributes.values());
	}

	public DataSchemaEntry<?> getAttributeEntry(String attribute) {
		return attributes.get(attribute);
	}

	/**
	 * @return a map containing the types for each attribute defined in this schema.
	 *         Note: cached after first access; invalidated on add/remove.
	 */
	public Map<String, Class<?>> getTypes() {
		ensureDerivedCaches();
		return cachedTypes;
	}

	/**
	 * @return a map containing the default values for each attribute defined in
	 *         this schema. Note: cached after first access; invalidated on
	 *         add/remove.
	 */
	public Map<String, Object> getDefaultValues() {
		ensureDerivedCaches();
		return cachedDefaultValues;
	}

	private synchronized void ensureDerivedCaches() {
		if (cachedTypes != null && cachedDefaultValues != null) {
			return; // Already cached
		}

		TreeMap<String, Class<?>> types = new TreeMap<>();
		TreeMap<String, Object> defaults = new TreeMap<>();

		for (Map.Entry<String, DataSchemaEntry<?>> e : sorted().entrySet()) {
			types.put(e.getKey(), e.getValue().getType());
			defaults.put(e.getKey(), e.getValue().getDefaultValue());
		}

		cachedTypes = Collections.unmodifiableMap(types);
		cachedDefaultValues = Collections.unmodifiableMap(defaults);
	}

	/**
	 * @param attribute an attribute name.
	 * @return the type of the given attribute.
	 */
	public Class<?> getType(String attribute) {
		DataSchemaEntry<?> e = attributes.get(attribute);
		if (e == null)
			throw new IllegalArgumentException(String.format("unknown attribute name '%s'", attribute));

		return e.getType();
	}

	/**
	 * @param attribute an attribute name.
	 * @return the default value of the given attribute.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getDefaultValue(String attribute) {
		DataSchemaEntry<?> entry = attributes.get(attribute);
		if (entry == null) {
			throw new IllegalArgumentException(String.format("unknown attribute name '%s'", attribute));
		}
		return (T) entry.getDefaultValue();
	}

	/**
	 * Introduces or updates a new attribute to the data schema with 'null' as
	 * default value.
	 * 
	 * @param attribute the attribute name
	 * @param type
	 * @return the data schema instance for call-chaining.
	 */
	public <T> DataSchema add(String attribute, Class<T> type) {
		return add(attribute, type, null);
	}

	/**
	 * Introduces or updates a new attribute to the data schema.
	 * 
	 * @param attribute    the attribute name
	 * @param type         the expected data type.
	 * @param defaultValue the default value in case the attribute is missing from a
	 *                     data object.
	 * @return the data schema instance for call-chaining.
	 */
	public <T> DataSchema add(String attribute, Class<T> type, T defaultValue) {
		if (attribute == null || attribute.trim().isEmpty())
			throw new IllegalArgumentException("Attribute name cannot be null or empty");

		if (type == null)
			throw new IllegalArgumentException("Type cannot be null");

		final DataSchemaEntry<T> entry = new DataSchemaEntry<T>(attribute, type, defaultValue);
		this.attributes.put(attribute, entry);

		// Invalidate sorted cache
		invalidateCaches();

		return this;
	}

	/**
	 * Introduces or updates a new attribute to the data schema.
	 * 
	 * @param attribute  the attribute name
	 * @param type       the expected data type.
	 * @param dataSchema
	 * @return the data schema instance for call-chaining.
	 */
	public <T extends IKeyValueProvider<?>> DataSchema add(String attribute, Class<T> type, DataSchema dataSchema) {
		return add(attribute, type, dataSchema, null);
	}

	/**
	 * Introduces or updates a new attribute to the data schema.
	 * 
	 * @param attribute    the attribute name
	 * @param type         the expected data type.
	 * @param defaultValue the default value in case the attribute is missing from a
	 *                     data object.
	 * @return the data schema instance for call-chaining.
	 */
	public <T extends IKeyValueProvider<?>> DataSchema add(String attribute, Class<T> type, DataSchema dataSchema,
			T defaultValue) {
		if (attribute == null || attribute.trim().isEmpty())
			throw new IllegalArgumentException("Attribute name cannot be null or empty");

		if (type == null)
			throw new IllegalArgumentException("Type cannot be null");

		final DataSchemaEntry<T> entry = new DataSchemaEntry<T>(attribute, type, defaultValue, dataSchema);
		this.attributes.put(attribute, entry);

		// Invalidate sorted cache
		invalidateCaches();

		return this;
	}

	/**
	 * Removes an attribute from the data schema.
	 * 
	 * @param attribute the attribute name.
	 * @return the data schema instance for call-chaining.
	 */
	public DataSchema remove(String attribute) {
		this.attributes.remove(attribute);

		// Invalidate sorted cache
		invalidateCaches();

		return this;
	}
}
