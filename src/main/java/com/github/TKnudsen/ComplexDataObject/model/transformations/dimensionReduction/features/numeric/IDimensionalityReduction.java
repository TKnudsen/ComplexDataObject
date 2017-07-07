package com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionReduction.features.numeric;

import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.transformations.IDataTransformation;

/**
 * <p>
 * Title: IDimensionalityReduction
 * </p>
 * 
 * <p>
 * Description:
 * 
 * <p>
 * Copyright: Copyright (c) 2012-2017 Jürgen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public interface IDimensionalityReduction extends IDataTransformation<NumericalFeatureVector, NumericalFeatureVector> {

	public int getOutputDimensionality();
	
	public Map<NumericalFeatureVector, NumericalFeatureVector> getMapping();
}