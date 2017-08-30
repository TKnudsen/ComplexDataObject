package com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionReduction.features;

import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
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
 * Copyright: Copyright (c) 2012-2017 J�rgen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public interface IDimensionalityReduction<O, X extends AbstractFeatureVector<O, ? extends Feature<O>>> extends IDataTransformation<X, NumericalFeatureVector> {

	public int getOutputDimensionality();

	public void calculateDimensionalityReduction();

	public Map<X, NumericalFeatureVector> getMapping();
}