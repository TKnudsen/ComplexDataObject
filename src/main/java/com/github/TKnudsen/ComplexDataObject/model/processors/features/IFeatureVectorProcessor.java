package com.github.TKnudsen.ComplexDataObject.model.processors.features;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.model.processors.IDataProcessor;

public interface IFeatureVectorProcessor<O extends Object, FV extends AbstractFeatureVector<O, ? extends Feature<O>>> extends IDataProcessor<FV> {

}
