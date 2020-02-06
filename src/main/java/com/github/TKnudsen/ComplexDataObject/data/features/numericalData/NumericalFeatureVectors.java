package com.github.TKnudsen.ComplexDataObject.data.features.numericalData;

import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.features.FeatureVectorUtils;

import de.javagl.nd.tuples.d.DoubleTuple;

/**
 * <p>
 * Little helpers for the creation of NumericalFeatureVectors.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2020
 * </p>
 * 
 * @version 1.05
 */
public class NumericalFeatureVectors {

	/**
	 * Create a numerical feature vector from the given features, with a sensible
	 * <code>toString</code> implementation...
	 * 
	 * @param features The features
	 * @return The feature vector
	 */
	public static NumericalFeatureVector create(List<NumericalFeature> features) {
		NumericalFeatureVector numericalFeatureVector = new NumericalFeatureVector(features) {
			@Override
			public String toString() {
				return FeatureVectorUtils.createString(this);
			}
		};
		return numericalFeatureVector;
	}

	/**
	 * Create a numerical feature vector from the given feature values, with a
	 * sensible <code>toString</code> implementation...
	 * 
	 * @param values The values
	 * @return The feature vector
	 */
	public static NumericalFeatureVector create(Iterable<? extends Number> values) {
		List<NumericalFeature> features = new ArrayList<NumericalFeature>();
		int counter = 0;
		for (Number value : values) {
			features.add(new NumericalFeature("dim" + counter, value.doubleValue()));
			counter++;
		}
		return create(features);
	}

	public static NumericalFeatureVector createNumericalFeatureVector(double[] vector) {
		return createNumericalFeatureVector(vector, "", "");
	}

	public static NumericalFeatureVector createNumericalFeatureVector(Double[] vector) {
		return createNumericalFeatureVector(vector, "", "");
	}

	public static NumericalFeatureVector createNumericalFeatureVector(double[] vector, String name,
			String description) {
		if (vector == null)
			return null;

		List<NumericalFeature> features = new ArrayList<>();
		for (int i = 0; i < vector.length; i++)
			features.add(new NumericalFeature("[" + i + "]", vector[i]));

		NumericalFeatureVector numericalFeatureVector = new NumericalFeatureVector(features);
		numericalFeatureVector.setName(name);
		numericalFeatureVector.setDescription(description);

		return numericalFeatureVector;
	}

	public static NumericalFeatureVector createNumericalFeatureVector(Double[] vector, String name,
			String description) {
		if (vector == null)
			return null;

		List<NumericalFeature> features = new ArrayList<>();
		for (int i = 0; i < vector.length; i++)
			features.add(new NumericalFeature("[" + i + "]", vector[i]));

		NumericalFeatureVector numericalFeatureVector = new NumericalFeatureVector(features);
		numericalFeatureVector.setName(name);
		numericalFeatureVector.setDescription(description);

		return numericalFeatureVector;
	}

	public static NumericalFeatureVector createNumericalFeatureVector(DoubleTuple doubleTuple, String name,
			String description) {
		if (doubleTuple == null)
			return null;

		double[] vector = new double[doubleTuple.getSize()];

		for (int i = 0; i < doubleTuple.getSize(); i++)
			vector[i] = doubleTuple.get(i);

		return createNumericalFeatureVector(vector, name, description);
	}
}
