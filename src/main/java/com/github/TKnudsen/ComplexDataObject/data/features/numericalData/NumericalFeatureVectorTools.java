package com.github.TKnudsen.ComplexDataObject.data.features.numericalData;

import java.util.ArrayList;
import java.util.List;

public class NumericalFeatureVectorTools {

	public static NumericalFeatureVector createNumericalFeatureVector(double[] vector) {
		if (vector == null)
			return null;

		List<NumericalFeature> features = new ArrayList<>();
		for (int i = 0; i < vector.length; i++)
			features.add(new NumericalFeature("[" + i + "]", vector[i]));

		return new NumericalFeatureVector(features);
	}
}
