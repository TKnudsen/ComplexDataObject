package com.github.TKnudsen.ComplexDataObject.model.descriptors;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;

/**
 * <p>
 * Title: INumericFeatureVectorDescriptor
 * </p>
 * 
 * <p>
 * Description: Basic Interface to transform real-world data (represented as a
 * ComplexDataObject) into numerical feature spaces.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public interface INumericFeatureVectorDescriptor<I extends IDObject> extends IDescriptor<I, Double, NumericalFeatureVector> {

}
