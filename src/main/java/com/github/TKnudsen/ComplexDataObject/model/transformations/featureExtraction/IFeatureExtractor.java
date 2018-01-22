package com.github.TKnudsen.ComplexDataObject.model.transformations.featureExtraction;

import com.github.TKnudsen.ComplexDataObject.model.transformations.IDataTransformation;

/**
 * <p>
 * Title: IFeatureExtractor
 * </p>
 * 
 * <p>
 * Description: Interface for the extraction of "one-value" information,
 * represented as a Feature<?>. Examples are statistical information, etc.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 * 
 * TODO_GENERICS Now equivalent to IDataTransformation
 */
public interface IFeatureExtractor<I, F> extends IDataTransformation<I, F> {

}
