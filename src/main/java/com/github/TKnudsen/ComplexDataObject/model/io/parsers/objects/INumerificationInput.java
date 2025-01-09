package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

/**
 * @deprecated early version of the INumerificationInput interface that was bond
 *             to double output
 * @param <T>
 */
public interface INumerificationInput<T> {

	public Double addNumerification(T object, double value);
}
