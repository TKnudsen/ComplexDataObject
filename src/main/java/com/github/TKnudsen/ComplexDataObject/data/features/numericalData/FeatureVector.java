package com.github.TKnudsen.ComplexDataObject.data.features.numericalData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureDataObject;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureType;

import de.javagl.nd.tuples.d.DoubleTuple;

/**
 * <p>
 * Title: FeatureVector
 * </p>
 * 
 * <p>
 * Description: Numerical representation of a high-dimensional object. Can be
 * used for algorithmic models applied in data mining, machine learning,
 * information retrieval, etc.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */

public class FeatureVector extends AbstractFeatureDataObject<Double, NumericalFeature> implements DoubleTuple {

	public FeatureVector(List<NumericalFeature> features) {
		super(features);
	}

	public FeatureVector(NumericalFeature[] features) {
		super(features);
	}

	public FeatureVector(Map<String, NumericalFeature> featuresMap) {
		super((Map<String, NumericalFeature>) featuresMap);
	}

	@Override
	public FeatureVector subTuple(int fromIndex, int toIndex) {
		return new FeatureVector(featuresList.subList(fromIndex, toIndex));
	}

	@Override
	public int getSize() {
		return sizeOfFeatures();
	}

	@Override
	public double get(int arg0) {
		return getFeature(arg0).doubleValue();
	}

	/**
	 * Conversion to primitive double format. Algorithms require the
	 * (traditional) version of double vectors.
	 * 
	 * @return
	 */
	public double[] getVector() {
		return toPrimitive(featuresList);
	}

	public void setVector(double vector[]) {
		featuresList = new ArrayList<>();

		if (vector != null)
			for (int i = 0; i < vector.length; i++)
				featuresList.add(new NumericalFeature("Dim " + i + 1, vector[i]));

		generalizeFromList();
	}

	public double[] getVectorClone() {
		return this.getVector().clone();
	}

	@Override
	public FeatureVector clone() {
		List<NumericalFeature> features = new ArrayList<>();
		for (NumericalFeature f : featuresList)
			features.add(f.clone());

		FeatureVector clone = new FeatureVector(features);

		// attributes and meta information
		clone.setMaster(getMaster());
		for (String s : attributes.keySet())
			clone.add(s, get(s));

		return clone;
	}

	private static double[] toPrimitive(List<NumericalFeature> features) {
		if (features == null)
			return null;
		else if (features.size() == 0)
			return new double[0];

		final double[] result = new double[features.size()];
		for (int i = 0; i < features.size(); i++)
			if (features.get(i) instanceof NumericalFeature)
				result[i] = ((NumericalFeature) features.get(i)).doubleValue();
			else if (features.get(i).getFeatureType().equals(FeatureType.DOUBLE))
				result[i] = ((Double) features.get(i).getFeatureValue()).doubleValue();
		return result;
	}

	@Override
	public void addFeature(String featureName, Double value) {
		addFeature(new NumericalFeature(featureName, value));
	}

}
