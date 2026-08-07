package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

/**
 * <p>
 * Interface for registering a numeric value associated with a given object,
 * used to build a lookup between arbitrary objects and their assigned
 * numeric representation.
 * </p>
 *
 * @deprecated early version of the INumerificationInput interface that was bond
 *             to double output
 * @param <T>
 */
public interface INumerificationInput<T> {

	public Double addNumerification(T object, double value);
}
