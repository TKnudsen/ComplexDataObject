package com.github.TKnudsen.ComplexDataObject.model.processors.features.mixedData;

import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeatureContainer;
import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.processors.features.IFeatureVectorProcessor;

/**
 * <p>
 * Title: IMixedDataFeatureVectorProcessor
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
public interface IMixedDataFeatureVectorProcessor extends IFeatureVectorProcessor<Object, MixedDataFeatureVector> {

	public void process(MixedDataFeatureContainer container);
}
