package com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors;

import java.util.List;

/**
 * <p>
 * Title: IParameterSupport
 * </p>
 * 
 * <p>
 * Description: A concept that helps to implement parameter guidance concepts.
 * Implementing this interface, an algorithm can be asked for alternative
 * parameterizations.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2018
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 * 
 *          TODO_GENERICS Pretty sure that some of the parameters are not really
 *          required, but the IParameterSupport/IDescriptor relationship is not
 *          trivial...
 */
public interface IParameterSupport<I, O> {

	public List<IDescriptor<I, O>> getAlternativeParameterizations(int count);
}