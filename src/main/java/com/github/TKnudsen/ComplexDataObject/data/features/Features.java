package com.github.TKnudsen.ComplexDataObject.data.features;

import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeature;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeature;

/**
 * <p>
 * Static helper methods for Feature instances, currently offering a factory
 * that creates a default Feature (numerical or mixed-data) for a given
 * feature name and FeatureType.
 * </p>
 *
 * @version 1.03
 * @since 2017
 */
public class Features {

	public static final String DEFAULT_FEATURE_NAME_PREFIX = "Dim";

	public static Feature<?> createDefaultFeature(String featureName, FeatureType featureType) {
		switch (featureType) {
		case DOUBLE:
			return new NumericalFeature(featureName, Double.NaN);
		default:
			return new MixedDataFeature(featureName, null, featureType);
		}
	}
}
