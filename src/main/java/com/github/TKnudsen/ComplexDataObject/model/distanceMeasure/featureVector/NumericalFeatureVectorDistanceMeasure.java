package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.featureVector;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;

/**
 * <p>
 * Title: NumericalFeatureVectorDistanceMeasure
 * </p>
 * 
 * <p>
 * Description: Basis class for NumericalFeatureVector distance measures.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author Juergen Bernard
 */
public abstract class NumericalFeatureVectorDistanceMeasure implements INumericalFeatureVectorDistanceMeasure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5056641818588042121L;

	@Override
	public double applyAsDouble(NumericalFeatureVector t, NumericalFeatureVector u) {
		return getDistance(t, u);
	}
}
