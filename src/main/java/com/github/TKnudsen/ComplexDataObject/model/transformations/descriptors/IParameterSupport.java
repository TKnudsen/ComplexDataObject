package com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors;

import java.util.List;

/**
 * <p>
 * A concept that helps to implement parameter guidance concepts.
 * Implementing this interface, an algorithm can be asked for alternative
 * parameterizations.
 * </p>
 *
 * @version 1.04
 * @since 2016
 */
public interface IParameterSupport<I, O> {

	public List<IDescriptor<I, O>> getAlternativeParameterizations(int count);
}