package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.featureVector;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeature;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;

/**
 * <p>
 * Title: EuclideanDistanceMeasure
 * </p>
 * 
 * <p>
 * Description: Euclidean's Distance Measure for NumericalFeatureVectors
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2012-2016
 * </p>
 * 
 * @author Juergen Bernard
 */
public class EuclideanDistanceMeasure implements IFeatureVectorDistanceMeasure {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2728016822703430883L;

	@Override
	public double getDistance(NumericalFeatureVector o1, NumericalFeatureVector o2) {
		List<NumericalFeature> featuresO1 = o1.getVectorRepresentation();
		List<NumericalFeature> featuresO2 = o2.getVectorRepresentation();

		double d = 0;
		for (int i = 0; i < Math.min(featuresO1.size(), featuresO2.size()); i++)
			d += Math.pow(featuresO1.get(i).doubleValue() - featuresO2.get(i).doubleValue(), 2);
		d = Math.sqrt(d);

		return d;
	}

	@Override
	public String getName() {
		return "EuclideanDistanceMeasure";
	}

	@Override
	public String getDescription() {
		return "Euclidean's Distance Measure for NumericalFeatureVectors";
	}
}
