package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

import java.util.function.Function;

/**
 * <p>
 * Function-based interface for parsing an arbitrary input Object into a
 * typed output value of type T, additionally exposing the target output
 * class type.
 * </p>
 */
public interface IObjectParser<T> extends Function<Object, T> {

	public Class<T> getOutputClassType();
}
