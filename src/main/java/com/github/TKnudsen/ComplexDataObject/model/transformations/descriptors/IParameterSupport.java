package com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;

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
 * Copyright: Copyright (c) 2016-2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public interface IParameterSupport<I extends IDObject, O, X extends AbstractFeatureVector<O, ? extends Feature<O>>, D extends IDescriptor<I, O, X>> {

	public List<D> getAlternativeParameterizations(int count);
}