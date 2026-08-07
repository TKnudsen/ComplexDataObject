package com.github.TKnudsen.ComplexDataObject.model.transformations.featureExtraction;

import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.model.transformations.IDataTransformation;

/**
 * <p>
 * Interface for the extraction of "one-value" information,
 * represented as a Feature. Examples are statistical information, etc.
 * </p>
 *
 * @version 1.02
 * @since 2017
 */
public interface IFeatureExtractor<I, F extends Feature<?>> extends IDataTransformation<I, F> {

}
