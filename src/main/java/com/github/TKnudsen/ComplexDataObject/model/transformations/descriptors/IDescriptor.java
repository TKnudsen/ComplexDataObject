package com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataTransformationCategory;
import com.github.TKnudsen.ComplexDataObject.model.transformations.IDataTransformation;

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
 * Copyright: Copyright (c) 2016-2024
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.05
 */
public interface IDescriptor<I, O> extends IDataTransformation<I, O>, IParameterSupport<I, O>, ISelfDescription {

	/**
	 * 
	 */
	public default DataTransformationCategory getDataTransformationCategory() {
		return DataTransformationCategory.DESCRIPTOR;
	}
}
