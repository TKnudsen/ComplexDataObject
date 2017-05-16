package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.featureVector;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.IIDObjectDistanceMeasure;

/**
 * <p>
 * Title: IFeatureVectorDistanceMeasure
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 */
public interface IFeatureVectorDistanceMeasure<FV extends AbstractFeatureVector<?, ? extends Feature<?>>> extends IIDObjectDistanceMeasure<FV> {

}
