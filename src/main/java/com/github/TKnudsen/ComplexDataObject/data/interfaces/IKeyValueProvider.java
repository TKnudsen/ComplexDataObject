package com.github.TKnudsen.ComplexDataObject.data.interfaces;

import java.util.Map;
import java.util.Set;

/**
 * Interface for objects containing key-value pairs, where keys are referred to
 * as <em>attributes</em> to avoid confusion with other types of identifiers.
 *
 * <p>
 * Implementations maintain a mapping from string attribute names to values of
 * type {@code V}, with optional type metadata per attribute.
 * </p>
 *
 * @param <V> the value type stored per attribute
 *
 * @version 1.09 revised February 2026
 * @since 2015
 */
public interface IKeyValueProvider<V> extends IDObject {

	/**
	 * Adds or replaces the value for the given attribute.
	 *
	 * @param attribute the attribute name; must not be null
	 * @param value     the value to store; may be null
	 */
	void add(String attribute, V value);

	/**
	 * Returns the value associated with the given attribute.
	 *
	 * @param attribute the attribute name; must not be null
	 * @return the value, or {@code null} if the attribute is not present
	 */
	V getAttribute(String attribute);

	/**
	 * Returns the set of all attribute names currently stored.
	 *
	 * <p>
	 * Convenience alias for {@link #keySet()} using attribute-oriented terminology.
	 * </p>
	 *
	 * @return the set of attribute names; never null
	 */
	default Set<String> getAttributes() {
		return keySet();
	}

	/**
	 * Returns the runtime type of the value stored for the given attribute.
	 *
	 * @param attribute the attribute name; must not be null
	 * @return the type, or {@code null} if the attribute is not present
	 */
	Class<?> getType(String attribute);

	/**
	 * Returns the set of all attribute names currently stored.
	 *
	 * @return the set of attribute names; never null
	 */
	Set<String> keySet();

	/**
	 * Returns a map of all attribute names to their runtime value types.
	 *
	 * @return attribute name to type map; never null
	 */
	Map<String, Class<?>> getTypes();

	/**
	 * Removes the attribute and returns its previously associated value.
	 *
	 * @param attribute the attribute name; must not be null
	 * @return the previously associated value, or {@code null} if not present
	 */
	V removeAttribute(String attribute);

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Implementations must base equality on attribute contents rather than object
	 * identity.
	 * </p>
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Must be consistent with {@link #equals(Object)}.
	 * </p>
	 */
	@Override
	int hashCode();
}