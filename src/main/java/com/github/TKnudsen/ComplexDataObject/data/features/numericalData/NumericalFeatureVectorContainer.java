package com.github.TKnudsen.ComplexDataObject.data.features.numericalData;

import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.features.FeatureSchema;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureVectorContainer;

/**
 * <p>
 * A FeatureVectorContainer specialized for NumericalFeatureVector objects,
 * i.e. feature vectors whose features are all purely numeric.
 * </p>
 *
 * @version 1.02
 * @since 2017
 */
public class NumericalFeatureVectorContainer extends FeatureVectorContainer<NumericalFeatureVector> {

	public NumericalFeatureVectorContainer(FeatureSchema featureSchema) {
		super(featureSchema);
	}

	public NumericalFeatureVectorContainer(Map<Long, NumericalFeatureVector> featureVectorMap) {
		super(featureVectorMap);
	}

	public NumericalFeatureVectorContainer(Iterable<NumericalFeatureVector> objects) {
		super(objects);
	}
}
