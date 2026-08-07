package com.github.TKnudsen.ComplexDataObject.data.features.mixedData;

import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.FeatureSchema;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureVectorContainer;

/**
 * <p>
 * A FeatureVectorContainer specialized for MixedDataFeatureVector objects,
 * i.e. feature vectors whose individual features can be of mixed data types
 * (numeric, boolean, string).
 * </p>
 *
 * @version 1.01
 * @since 2016
 */
public class MixedDataFeatureContainer extends FeatureVectorContainer<MixedDataFeatureVector> {

	public MixedDataFeatureContainer(FeatureSchema featureSchema) {
		super(featureSchema);
	}

	public MixedDataFeatureContainer(Map<Long, MixedDataFeatureVector> featureVectorMap) {
		super(featureVectorMap);
	}

	public MixedDataFeatureContainer(Iterable<MixedDataFeatureVector> objects) {
		super(objects);
	}
}
