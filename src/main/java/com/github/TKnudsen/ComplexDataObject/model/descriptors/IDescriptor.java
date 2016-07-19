package com.github.TKnudsen.ComplexDataObject.model.descriptors;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;

/**
 * <p>
 * Title: IDescriptor
 * </p>
 * 
 * <p>
 * Description: Basic Interface to transform real-world data (represented as a
 * ComplexDataObject) into the feature space.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public interface IDescriptor<O, X extends AbstractFeatureVector<O, ? extends Feature<O>>> extends ISelfDescription {

	public List<X> transform(ComplexDataObject complexDataObject);

	public List<X> transform(List<ComplexDataObject> complexDataObjects);
}
