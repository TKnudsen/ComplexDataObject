package com.github.TKnudsen.ComplexDataObject.model.processors.features;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.model.processors.IDataProcessor;

/**
 * <p>
 * Title: IFeatureVectorProcessor
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
 * @version 1.03
 */
public interface IFeatureVectorProcessor<FV extends AbstractFeatureVector<?, ? extends Feature<?>>> extends IDataProcessor<FV> {

}
