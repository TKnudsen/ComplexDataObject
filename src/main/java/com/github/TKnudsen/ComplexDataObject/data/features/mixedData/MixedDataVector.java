package com.github.TKnudsen.ComplexDataObject.data.features.mixedData;

import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureDataObject;

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

public class MixedDataVector extends AbstractFeatureDataObject<Object, MixedDataFeature> {

	public MixedDataVector(List<MixedDataFeature> features) {
		super(features);
	}

	public MixedDataVector(MixedDataFeature[] features) {
		super(features);
	}

	public MixedDataVector(Map<String, MixedDataFeature> featuresMap) {
		super(featuresMap);
	}

	@Override
	public MixedDataVector subTuple(int fromIndex, int toIndex) {
		if (featuresList != null)
			return new MixedDataVector(featuresList.subList(fromIndex, toIndex));
		return null;
	}

	@Override
	public void addFeature(String featureName, Object value) {
		addFeature(new MixedDataFeature(featureName, value));
	}
}
