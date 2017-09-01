package com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionalityReduction;

import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.IDistanceMeasure;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataTransformationCategory;

/**
 * <p>
 * Title: DimensionalityReduction
 * </p>
 * 
 * <p>
 * Description: baseline for dimensionality reduction algorithms. Maintains
 * generalizable data structures.
 * 
 * <p>
 * Copyright: Copyright (c) 2012-2017 Juergen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public abstract class DimensionalityReduction<O, X extends AbstractFeatureVector<O, ? extends Feature<O>>> implements IDimensionalityReduction<O, X> {

	/**
	 * used by many routines to calculate pairwise distances
	 */
	protected IDistanceMeasure<X> distanceMeasure;

	/**
	 * the dimensionality of the manifold to be learned
	 */
	protected int outputDimensionality;

	protected Map<X, NumericalFeatureVector> mapping;

	@Override
	public DataTransformationCategory getDataTransformationCategory() {
		return DataTransformationCategory.DIMENSION_REDUCTION;
	}

	@Override
	public Map<X, NumericalFeatureVector> getMapping() {
		return mapping;
	}

	@Override
	public int getOutputDimensionality() {
		return outputDimensionality;
	}

	public void setOutputDimensionality(int outputDimensionality) {
		this.outputDimensionality = outputDimensionality;

		this.mapping = null;
	}

}
