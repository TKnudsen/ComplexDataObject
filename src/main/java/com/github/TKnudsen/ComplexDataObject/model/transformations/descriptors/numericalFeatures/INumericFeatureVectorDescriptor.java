package com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors.numericalFeatures;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors.IDescriptor;

/**
 * <p>
 * Basic Interface to transform real-world data (represented as a
 * ComplexDataObject) into numerical feature spaces.
 * </p>
 *
 * @version 1.04
 * @since 2016
 */
public interface INumericFeatureVectorDescriptor<I> extends IDescriptor<I, NumericalFeatureVector> {

}
