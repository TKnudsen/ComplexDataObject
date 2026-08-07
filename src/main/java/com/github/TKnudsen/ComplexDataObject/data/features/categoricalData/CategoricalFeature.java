package com.github.TKnudsen.ComplexDataObject.data.features.categoricalData;

import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureType;

/**
 * <p>
 * A Feature holding a categorical (String-valued) feature value, identified
 * by FeatureType.STRING.
 * </p>
 *
 * @version 1.0
 * @since 2017
 */

public class CategoricalFeature extends Feature<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2550677654103245531L;

	private CategoricalFeature() {
		super(FeatureType.STRING);
	}

	public CategoricalFeature(String featureName, String featureValue) {
		super(featureName, featureValue, FeatureType.STRING);
	}

	@Override
	public CategoricalFeature clone() {
		return new CategoricalFeature(featureName, featureValue);
	}

}
