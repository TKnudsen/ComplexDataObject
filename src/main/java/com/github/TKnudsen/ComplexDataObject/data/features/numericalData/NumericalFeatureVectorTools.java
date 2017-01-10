package com.github.TKnudsen.ComplexDataObject.data.features.numericalData;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Title: NumericalFeatureVectorTools
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */

public class NumericalFeatureVectorTools {

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

	public static List<double[]> toPrimitives(List<NumericalFeatureVector> featureVectors) {
		List<double[]> returnValues = new ArrayList<>();

		for (int i = 0; i < featureVectors.size(); i++)
			returnValues.add(featureVectors.get(i).getVectorClone());

		return returnValues;
	}

	public static void addClassAttribute(List<NumericalFeatureVector> featureVectors, List<String> labels, String classAttribute) {
		for (int i = 0; i < featureVectors.size(); i++)
			featureVectors.get(i).add(classAttribute, labels.get(i));
	}

	public static void addNumericAttribute(List<NumericalFeatureVector> features, List<Double> labels, String attributeName) {
		for (int i = 0; i < features.size(); i++)
			features.get(i).add(attributeName, labels.get(i));
	}

}
