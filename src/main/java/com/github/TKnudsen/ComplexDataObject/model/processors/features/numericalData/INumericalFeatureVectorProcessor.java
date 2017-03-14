package com.github.TKnudsen.ComplexDataObject.model.processors.features.numericalData;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVectorContainer;
import com.github.TKnudsen.ComplexDataObject.model.processors.features.IFeatureVectorProcessor;

public interface INumericalFeatureVectorProcessor extends IFeatureVectorProcessor<Double, NumericalFeatureVector> {

	public void process(NumericalFeatureVectorContainer container);
}
