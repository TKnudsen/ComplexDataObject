package com.github.TKnudsen.ComplexDataObject.model.processors.features.numericalData;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVectorContainer;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataProcessingCategory;

import java.util.List;

/**
 * <p>
 * Title: FeatureRemover
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017-2018
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class FeatureRemover implements INumericalFeatureVectorProcessor {

	private String featureName;

	public FeatureRemover(String featureName) {
		this.featureName = featureName;
	}

	@Override
	public void process(NumericalFeatureVectorContainer container) {
		container.remove(featureName);
	}

	@Override
	public void process(List<NumericalFeatureVector> data) {
		for (NumericalFeatureVector object : data)
			object.removeFeature(featureName);
	}

	@Override
	public DataProcessingCategory getPreprocessingCategory() {
		return DataProcessingCategory.DATA_REDUCTION;
	}

	public String getFeatureName() {
		return featureName;
	}

	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}
}
