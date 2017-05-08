package com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionReduction.features.numeric;

import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.transformations.IDataTransformation;

public interface IDimensionReduction extends IDataTransformation<NumericalFeatureVector, NumericalFeatureVector> {

	public Map<NumericalFeatureVector, NumericalFeatureVector> getMapping();
}