package com.github.TKnudsen.ComplexDataObject.data.features.mixedData;

import com.github.TKnudsen.ComplexDataObject.data.features.FeatureType;

public class MixedDataFeatureTools {

	public static FeatureType guessFeatureType(Object feature) throws IllegalArgumentException {
		if (feature == null)
			throw new IllegalArgumentException("MixedDataFeatureTools.guessFeatureType: object was null - unable to guess");
		if (feature instanceof Number)
			return FeatureType.DOUBLE;
		else if (feature instanceof Boolean)
			return FeatureType.BOOLEAN;
		else if (feature instanceof String)
			return FeatureType.STRING;
		else if (feature instanceof Character)
			return FeatureType.STRING;
		throw new IllegalArgumentException("MixedDataFeatureTools.guessFeatureType: undefined object type");
	}
}
