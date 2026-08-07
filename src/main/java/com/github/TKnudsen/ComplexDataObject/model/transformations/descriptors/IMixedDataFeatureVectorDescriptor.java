package com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors;

import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;

/**
 * <p>
 * Basic Interface to transform real-world data (represented as a
 * ComplexDataObject) into the mixed data feature space.
 * </p>
 *
 * @version 1.03
 * @since 2016
 */
public interface IMixedDataFeatureVectorDescriptor<I extends IDObject> extends IDescriptor<I, MixedDataFeatureVector> {

}
