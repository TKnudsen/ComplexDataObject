package com.github.TKnudsen.ComplexDataObject.data.features.numericalData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureType;
import com.github.TKnudsen.ComplexDataObject.data.features.Features;

import de.javagl.nd.tuples.d.DoubleTuple;

/**
 * <p>
 * Numerical representation of a high-dimensional object. Can be
 * used for algorithmic models applied in data mining, machine learning,
 * information retrieval, etc.
 *
 * Update: featuresMap does not need to be sorted any more. Improves
 * performance.
 * </p>
 *
 * @version 1.04
 * @since 2015
 */

public class NumericalFeatureVector extends AbstractFeatureVector<Double, NumericalFeature> implements DoubleTuple {

	private NumericalFeatureVector() {
		super();
	}

	public NumericalFeatureVector(List<NumericalFeature> features) {
		super(features);
	}

	public NumericalFeatureVector(NumericalFeature[] features) {
		super(features);
	}

	/**
	 * @param featuresMap Update: featuresMap does not need to be sorted any more.
	 *                    Improves performance.
	 */
	public NumericalFeatureVector(Map<String, NumericalFeature> featuresMap) {
		super(featuresMap);
	}

	@Override
	public NumericalFeatureVector subTuple(int fromIndex, int toIndex) {
		return new NumericalFeatureVector(getVectorRepresentation().subList(fromIndex, toIndex));
	}

	@Override
	public int getSize() {
		return sizeOfFeatures();
	}

	@Override
	public double get(int index) {
		NumericalFeature f = getFeature(index);
		if (f == null)
			return Double.NaN;
		return f.doubleValue();
	}

	/**
	 * Conversion to primitive double format. Algorithms often require primitive
	 * vectors.
	 *
	 * @return primitive double vector (same order as index-based access)
	 */
	public double[] getVector() {
		return toPrimitive(getVectorRepresentation());
	}

	/**
	 * Replaces the entire feature vector from a primitive double array.
	 *
	 */
	public void setVector(double[] vector) {
		clearAllFeatures();

		if (vector == null)
			return;

		for (int i = 0; i < vector.length; i++) {
			String featureName = Features.DEFAULT_FEATURE_NAME_PREFIX + " " + (i + 1);
			addFeature(new NumericalFeature(featureName, vector[i]));
		}
	}

	public double[] getVectorClone() {
		double[] v = this.getVector();
		return v == null ? null : v.clone();
	}

	@Override
	public NumericalFeatureVector clone() {
		List<NumericalFeature> features = new ArrayList<>();
		for (NumericalFeature f : getVectorRepresentation())
			features.add(f == null ? null : f.clone());

		NumericalFeatureVector clone = new NumericalFeatureVector(features);

		// attributes and meta information
		clone.setMaster(getMaster());
		for (String s : attributes.keySet())
			clone.add(s, getAttribute(s));

		// name and description
		clone.setName(getName());
		clone.setDescription(getDescription());

		return clone;
	}

	@Override
	public void addFeature(String featureName, Double value, FeatureType type) {
		addFeature(new NumericalFeature(featureName, value));
	}

	private static double[] toPrimitive(List<NumericalFeature> features) {
		if (features == null)
			return null;
		else if (features.isEmpty())
			return new double[0];

		final double[] result = new double[features.size()];
		for (int i = 0; i < features.size(); i++) {
			NumericalFeature f = features.get(i);
			if (f == null) {
				result[i] = Double.NaN;
				continue;
			}

			// Fast path for NumericalFeature
			result[i] = f.doubleValue();
		}
		return result;
	}

}
