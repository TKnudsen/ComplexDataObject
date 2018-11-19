package com.github.TKnudsen.ComplexDataObject.data.features.mixedData;

import com.github.TKnudsen.ComplexDataObject.data.features.FeatureSchema;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureVectorContainer;

import java.util.Map;

/**
 * <p>
 * Title: MixedFeatureContainer
 * </p>
 *
 * <p>
 * Description:
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2016-2017
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.01
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
