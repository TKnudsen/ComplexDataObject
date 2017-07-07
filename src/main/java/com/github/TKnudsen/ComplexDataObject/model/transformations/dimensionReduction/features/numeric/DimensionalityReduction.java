package com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionReduction.features.numeric;

import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.featureVector.EuclideanDistanceMeasure;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.featureVector.INumericalFeatureVectorDistanceMeasure;
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
 * Copyright: Copyright (c) 2012-2017 Jürgen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public abstract class DimensionalityReduction implements IDimensionalityReduction {

	/**
	 * used by many routines to calculate pairwise distances
	 */
	protected INumericalFeatureVectorDistanceMeasure distanceMeasure = new EuclideanDistanceMeasure();

	/**
	 * the dimensionality of the manifold to be learned
	 */
	protected int outputDimensionality;

	protected Map<NumericalFeatureVector, NumericalFeatureVector> mapping;

	@Override
	public DataTransformationCategory getDataTransformationCategory() {
		return DataTransformationCategory.DIMENSION_REDUCTION;
	}

	@Override
	public Map<NumericalFeatureVector, NumericalFeatureVector> getMapping() {
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
