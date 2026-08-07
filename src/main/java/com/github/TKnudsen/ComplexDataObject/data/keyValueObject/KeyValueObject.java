package com.github.TKnudsen.ComplexDataObject.data.keyValueObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IKeyValueProvider;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StringTools;

/**
 * Basic data structure for objects with string-keyed attributes and arbitrary
 * object values.
 *
 * <p>
 * Attribute storage is backed by either a {@link SmallMap} (for objects with
 * few attributes) or a pre-sized {@link HashMap}, selected at construction time
 * based on {@code expectedAttributeCount} to avoid resizing overhead.
 * </p>
 *
 * <p>
 * <b>ID handling:</b> A special {@link #ID} attribute is maintained for legacy
 * compatibility. If no ID has been set, {@link #getID()} assigns a random
 * {@code long} on first call. This side effect is intentional but regrettable --
 * see field Javadoc for migration guidance.
 * </p>
 *
 * @version 2.0 revised February 2026
 * @since 2015
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyValueObject implements IKeyValueProvider<Object>, Iterable<String> {

	/**
	 * The reserved attribute name for the legacy numeric ID.
	 *
	 * <p>
	 * Prepare for its deletion and replacement by a standard attribute. In
	 * practice, even ID-based usage forms define a primary key attribute and do not
	 * rely on this field.
	 * </p>
	 */
	public static final String ID = "ID";

	/**
	 * Default expected attribute count, used to select the backing map
	 * implementation and initial capacity.
	 */
	private static final int DEFAULT_EXPECTED_ATTRIBUTE_COUNT = 8;

	/**
	 * Attribute map. Uses {@link SmallMap} for small objects and {@link HashMap}
	 * for larger ones to balance memory and lookup performance.
	 */
	protected Map<String, Object> attributes;

	// -----------------------------------------------------------------------
	// Construction
	// -----------------------------------------------------------------------

	/**
	 * Default constructor -- initializes with
	 * {@value #DEFAULT_EXPECTED_ATTRIBUTE_COUNT} expected attributes.
	 */
	public KeyValueObject() {
		this(DEFAULT_EXPECTED_ATTRIBUTE_COUNT);
	}

	/**
	 * Main constructor. Initializes a backing map sized to accommodate the expected
	 * number of attributes without resizing.
	 *
	 * @param expectedAttributeCount estimated number of attributes; used for
	 *                               capacity pre-allocation, not enforced as a
	 *                               limit
	 */
	public KeyValueObject(int expectedAttributeCount) {
		this.attributes = createAttributeMap(expectedAttributeCount);
		attributes.put(ID, MathFunctions.randomLong()); // assign directly, skip getID() overhead
	}

	// -----------------------------------------------------------------------
	// Deprecated ID-based constructors
	// -----------------------------------------------------------------------

	/** @deprecated prefer {@code add(KeyValueObject.ID, yourID)} */
	@Deprecated
	public KeyValueObject(long id) {
		this(DEFAULT_EXPECTED_ATTRIBUTE_COUNT);
		attributes.put(ID, id);
	}

	/** @deprecated prefer {@code add(KeyValueObject.ID, yourID)} */
	@Deprecated
	public KeyValueObject(int expectedAttributeCount, long id) {
		this(expectedAttributeCount);
		attributes.put(ID, id);
	}

	/** @deprecated prefer {@code add(KeyValueObject.ID, yourID)} */
	@Deprecated
	public KeyValueObject(Long id) {
		this(DEFAULT_EXPECTED_ATTRIBUTE_COUNT);
		if (id == null)
			throw new IllegalArgumentException("ID must not be null");
		attributes.put(ID, id);
	}

	/** @deprecated prefer {@code add(KeyValueObject.ID, yourID)} */
	@Deprecated
	public KeyValueObject(int expectedAttributeCount, Long id) {
		this(expectedAttributeCount);
		if (id == null)
			throw new IllegalArgumentException("ID must not be null");
		attributes.put(ID, id);
	}

	// -----------------------------------------------------------------------
	// Identity
	// -----------------------------------------------------------------------

	/**
	 * Returns the numeric ID of this object.
	 *
	 * <p>
	 * <b>Side effect:</b> if no {@link #ID} attribute is present or its value is
	 * not a {@link Number}, a random {@code long} is assigned and stored. This
	 * mutation on first access is intentional for legacy compatibility but should
	 * be avoided in new code.
	 * </p>
	 *
	 * @deprecated Prepare to make protected. The primary key should not be limited
	 *             to {@code long}.
	 */
	@JsonIgnore
	@Override
	public long getID() {
		Object idValue = getAttribute(ID);
		if (!(idValue instanceof Number)) {
			long random = MathFunctions.randomLong();
			attributes.put(ID, random);
			return random;
		}
		return ((Number) idValue).longValue();
	}

	@Override
	public int hashCode() {
		return Long.hashCode(getID());
	}

	/**
	 * Two {@link KeyValueObject} instances are equal if and only if they have the
	 * same class and the same numeric ID.
	 *
	 * <p>
	 * Note: hash collisions are theoretically possible but extremely unlikely given
	 * 64-bit IDs. ID comparison is used directly to avoid false positives from hash
	 * collisions.
	 * </p>
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return this.getID() == ((KeyValueObject) obj).getID();
	}

	/**
	 * Returns {@code true} if this object and {@code obj} have identical attribute
	 * sets and equal values for all attributes.
	 *
	 * @param obj the object to compare; may be null
	 * @return {@code true} if all attributes and values are equal
	 */
	public boolean equalValues(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		KeyValueObject other = (KeyValueObject) obj;
		if (!this.keySet().equals(other.keySet()))
			return false;

		for (String key : keySet())
			if (!Objects.equals(getAttribute(key), other.getAttribute(key)))
				return false;

		return true;
	}

	// -----------------------------------------------------------------------
	// Attribute access
	// -----------------------------------------------------------------------

	/**
	 * Returns {@code true} if an attribute with the given name is present.
	 *
	 * @param attribute the attribute name; must not be null
	 * @return {@code true} if the attribute exists
	 */
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

	@Override
	public Object removeAttribute(String attribute) {
		return attributes.remove(attribute);
	}

	@Override
	public Class<?> getType(String attribute) {
		Object value = attributes.get(attribute);
		return value != null ? value.getClass() : null;
	}

	@Override
	public Map<String, Class<?>> getTypes() {
		Map<String, Class<?>> result = new HashMap<>();
		for (Map.Entry<String, Object> entry : attributes.entrySet())
			result.put(entry.getKey(), entry.getValue() != null ? entry.getValue().getClass() : null);
		return result;
	}

	@Override
	public Iterator<String> iterator() {
		return attributes.keySet().iterator();
	}

	// -----------------------------------------------------------------------
	// String representation
	// -----------------------------------------------------------------------

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128 + attributes.size() * 64);
		sb.append("Attribute:").append('\t').append("Type:").append('\t').append("Value:").append('\n');

		for (String key : new TreeSet<>(attributes.keySet())) {
			Object value = attributes.get(key);
			String type = value != null ? value.getClass().getSimpleName() : "unknown";
			sb.append(StringTools.padRight(key, 36)).append('\t').append(StringTools.padRight(type, 10)).append('\t')
					.append(String.valueOf(value)).append('\n');
		}
		return sb.toString();
	}

	// -----------------------------------------------------------------------
	// Private helpers
	// -----------------------------------------------------------------------

	private static Map<String, Object> createAttributeMap(int expectedCount) {
		if (expectedCount <= DEFAULT_EXPECTED_ATTRIBUTE_COUNT)
			return new SmallMap<>();
		return new HashMap<>((int) Math.ceil(expectedCount / 0.75) + 1);
	}
}