package com.github.TKnudsen.ComplexDataObject.data.features.mixedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureType;

/**
 * <p>
 * Feature vector for mixed data types (Double, String, Boolean).
 * Used for objects containing numerical, categorical, and binary attributes.
 *
 * Update: featuresMap does not need to be sorted any more. Improves
 * performance.
 * </p>
 *
 * @version 1.03
 * @since 2015
 */
public class MixedDataFeatureVector extends AbstractFeatureVector<Object, MixedDataFeature> {

	public MixedDataFeatureVector(List<MixedDataFeature> features) {
		super(features);
	}

	public MixedDataFeatureVector(MixedDataFeature[] features) {
		super(features);
	}

	public MixedDataFeatureVector(Map<String, MixedDataFeature> featuresMap) {
		super(featuresMap);
	}

	@Override
	public MixedDataFeatureVector subTuple(int fromIndex, int toIndex) {
		return new MixedDataFeatureVector(getVectorRepresentation().subList(fromIndex, toIndex));
	}

	@Override
	public void addFeature(String featureName, Object value, FeatureType type) {
		addFeature(new MixedDataFeature(featureName, value, type));
	}

	@Override
	public MixedDataFeatureVector clone() {
		List<MixedDataFeature> features = new ArrayList<>();
		for (MixedDataFeature f : getVectorRepresentation())
			features.add(f == null ? null : f.clone());

		MixedDataFeatureVector clone = new MixedDataFeatureVector(features);

		clone.setMaster(getMaster());
		for (String s : attributes.keySet())
			clone.add(s, getAttribute(s));

		clone.setName(getName());
		clone.setDescription(getDescription());

		return clone;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Feature<?> f : getVectorRepresentation())
			if (f != null && f.getFeatureValue() != null)
				sb.append(f.getFeatureName()).append(": ").append(f.getFeatureValue()).append("\n");
		return sb.toString();
	}

	/**
	 * Retrieves feature names of a given FeatureType.
	 * 
	 * @param featureType the feature type to filter by
	 * @return list of feature names matching the type
	 */
	public List<String> getFeatureNames(FeatureType featureType) {
		List<String> featureNames = new ArrayList<>();
		for (Feature<?> f : getVectorRepresentation())
			if (f != null && f.getFeatureType() != null && f.getFeatureType().equals(featureType))
				featureNames.add(f.getFeatureName());
		return featureNames;
	}
}
