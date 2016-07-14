package com.github.TKnudsen.ComplexDataObject.data.features.mixedData;

import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureType;

/**
 * <p>
 * Title: MixedDataVector
 * </p>
 *
 * <p>
 * Description: AlgorithmDataObject representation for mixed data involving
 * Double, String, and Boolean. Used for objects containing numerical,
 * categorical, and binary attributes.
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2015-2016
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.0
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
		if (featuresList != null)
			return new MixedDataFeatureVector(featuresList.subList(fromIndex, toIndex));
		return null;
	}

	@Override
	public void addFeature(String featureName, Object value, FeatureType type) {
		addFeature(new MixedDataFeature(featureName, value, type));
	}
}
