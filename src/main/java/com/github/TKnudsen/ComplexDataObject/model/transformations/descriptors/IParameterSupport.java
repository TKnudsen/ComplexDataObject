package com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;

public interface IParameterSupport<I extends IDObject, O, X extends AbstractFeatureVector<O, ? extends Feature<O>>, D extends IDescriptor<I, O, X>> {

	public List<D> getAlternativeParameterizations(int count);
}