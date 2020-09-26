package com.github.TKnudsen.ComplexDataObject.data.features.numericalData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.data.features.FeatureVectorUtils;

import de.javagl.nd.tuples.d.DoubleTuple;

/**
 * <p>
 * Little helpers for NumericalFeatureVectors.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2020
 * </p>
 * 
 * @version 1.06
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

	/**
	 * retrieves the minimum value.
	 * 
	 * @param featureVector
	 * @return
	 */
	public static Double getMin(NumericalFeatureVector featureVector) {
		if (featureVector == null)
			return Double.NaN;

		Double d = Double.MAX_VALUE - 1;
		for (NumericalFeature feature : featureVector.getVectorRepresentation())
			d = Math.min(d, feature.doubleValue());
		return d;
	}

	/**
	 * retrieves the maximum value.
	 * 
	 * @param featureVector
	 * @return
	 */
	public static Double getMax(NumericalFeatureVector featureVector) {
		if (featureVector == null)
			return Double.NaN;

		Double d = Double.MIN_VALUE + 1;
		for (NumericalFeature feature : featureVector.getVectorRepresentation())
			d = Math.max(d, feature.doubleValue());
		return d;
	}

	/**
	 * calculates the min value for a dimension
	 * 
	 * @param features
	 * @param dim
	 * @return
	 */
	public static double getMin(List<NumericalFeatureVector> features, int dim) {
		if (features == null || features.size() == 0)
			throw new IllegalArgumentException();

		double min = Double.MAX_VALUE - 1;

		for (int n = 0; n < features.size(); n++) {
			if (!Double.isNaN(features.get(n).get(dim))) {
				min = Math.min(min, features.get(n).get(dim));
			}
		}

		return min;
	}

	/**
	 * calculates the max value for a dimension
	 * 
	 * @param features
	 * @param dim
	 * @return
	 */
	public static double getMax(List<NumericalFeatureVector> features, int dim) {
		if (features == null || features.size() == 0)
			throw new IllegalArgumentException();

		double max = Double.MIN_VALUE + 1;

		for (int n = 0; n < features.size(); n++) {
			if (!Double.isNaN(features.get(n).get(dim))) {
				max = Math.max(max, features.get(n).get(dim));
			}
		}

		return max;
	}

	/**
	 * calculates the mean value
	 * 
	 * @param features
	 * @param dim
	 * @return
	 */
	public static double getMean(List<NumericalFeatureVector> features, int dim) {
		double sum = 0;
		double count = 0;

		for (int n = 0; n < features.size(); n++) {
			if (!Double.isNaN(features.get(n).get(dim))) {
				sum += features.get(n).get(dim);
				count += 1;
			}
		}
		return sum / count;
	}

	public static List<double[]> toPrimitives(Set<NumericalFeatureVector> featureVectors) {
		List<double[]> returnValues = new ArrayList<>();

		for (NumericalFeatureVector fv : featureVectors)
			returnValues.add(fv.getVectorClone());

		return returnValues;
	}

	public static List<double[]> toPrimitives(List<NumericalFeatureVector> featureVectors) {
		List<double[]> returnValues = new ArrayList<>();

		for (int i = 0; i < featureVectors.size(); i++)
			returnValues.add(featureVectors.get(i).getVectorClone());

		return returnValues;
	}

	public static void addClassAttribute(List<NumericalFeatureVector> featureVectors, List<String> labels,
			String classAttribute) {
		for (int i = 0; i < featureVectors.size(); i++)
			featureVectors.get(i).add(classAttribute, labels.get(i));
	}

	public static void addNumericAttribute(List<NumericalFeatureVector> features, List<Double> labels,
			String attributeName) {
		for (int i = 0; i < features.size(); i++)
			features.get(i).add(attributeName, labels.get(i));
	}

	/**
	 * creates a matrix-like representation with the number of feature vectors in
	 * the first dimension and the dimensionality of the feature vectors in the
	 * second dimension.
	 * 
	 * @param featureVectors
	 * @return
	 */
	public static double[][] createMatrixRepresentation(List<? extends NumericalFeatureVector> featureVectors) {
		if (featureVectors == null)
			throw new NullPointerException("NumericalFeatureVectorTools: feature vectors must not be null");

		if (featureVectors.size() == 0)
			throw new IllegalArgumentException("NumericalFeatureVectorTools: feature vectors size was 0");

		double[][] values = new double[featureVectors.size()][featureVectors.get(0).getDimensions()];
		for (int i = 0; i < featureVectors.size(); i++)
			values[i] = featureVectors.get(i).getVectorClone();

		return values;
	}

	/**
	 * creates a vector with the information of one feature from the FeatureVectors,
	 * addressed by the index.
	 * 
	 * @param fvs
	 * @param index
	 * @return
	 */
	public static double[] retrieveVariable(List<NumericalFeatureVector> fvs, int index) {
		if (fvs == null)
			throw new NullPointerException("NumericalFeatureVectorTools: feature vectors must not be null");

		if (fvs.size() == 0)
			throw new IllegalArgumentException("NumericalFeatureVectorTools: feature vectors size was 0");

		double[] values = new double[fvs.size()];
		for (int i = 0; i < fvs.size(); i++)
			values[i] = fvs.get(i).get(index);

		return values;
	}

	public static double[] retrieveNumericalAttribute(List<NumericalFeatureVector> fvs, String classAttribute) {
		if (fvs == null)
			throw new NullPointerException("LDA: feature vectors must not be null");

		if (fvs.size() == 0)
			throw new IllegalArgumentException("LDA: feature vectors size was 0");

		double[] values = new double[fvs.size()];
		for (int i = 0; i < fvs.size(); i++) {
			if (fvs.get(i).getAttribute(classAttribute) != null)
				if (fvs.get(i).getAttribute(classAttribute) instanceof Number) {
					values[i] = ((Number) fvs.get(i).getAttribute(classAttribute)).doubleValue();
					continue;
				}
			values[i] = Double.NaN;
		}

		return values;
	}
}
