package com.github.TKnudsen.ComplexDataObject.data.features.numericalData;

import com.github.TKnudsen.ComplexDataObject.data.features.FeatureSchema;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureVectorContainer;

import java.util.Map;

/**
 * <p>
 * Title: NumericalFeatureVectorContainer
 * </p>
 *
 * <p>
 * Description:
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.02
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
