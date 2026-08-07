package com.github.TKnudsen.ComplexDataObject.data.features.numericalData;

import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureType;

/**
 * <p>
 * A Feature holding a numeric (Double-valued) feature value, identified by
 * FeatureType.DOUBLE, and providing convenience access as a primitive double.
 * </p>
 *
 * @version 1.02
 * @since 2016
 */

public class NumericalFeature extends Feature<Double> {

	/**
	 *
	 */
	private static final long serialVersionUID = 7244765037515290604L;

	private NumericalFeature() {
		super(FeatureType.DOUBLE);
	}

	public NumericalFeature(String featureName, Double featureValue) {
		super(featureName, featureValue, FeatureType.DOUBLE);
	}

	@Override
	public String toString() {
		if (featureName != null)
			return featureName + ", " + featureValue + " (" + featureType.name() + ") ";
		return featureValue + " (" + featureType.name() + ") ";
	}

	public double doubleValue() {
		return featureValue.doubleValue();
	}

	@Override
	public NumericalFeature clone() {
		return new NumericalFeature(featureName, featureValue);
	}
}
