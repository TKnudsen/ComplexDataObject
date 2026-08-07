package com.github.TKnudsen.ComplexDataObject.model.io.parsers.numerification;

/**
 * <p>
 * Interface for registering a numeric value (a numerification) for a given
 * non-numeric object, used to build a lookup between arbitrary objects and
 * their assigned numeric representation.
 * </p>
 */
public interface INumerificationInput<T, N extends Number> {

	public N addNumerification(T object, N value);
}
