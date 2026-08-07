package com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataTransformationCategory;
import com.github.TKnudsen.ComplexDataObject.model.transformations.IDataTransformation;

/**
 * <p>
 * Basic Interface to transform real-world data (represented as a
 * ComplexDataObject) into the feature space.
 * </p>
 *
 * @version 1.05
 * @since 2016
 */
public interface IDescriptor<I, O> extends IDataTransformation<I, O>, IParameterSupport<I, O>, ISelfDescription {

	/**
	 * 
	 */
	public default DataTransformationCategory getDataTransformationCategory() {
		return DataTransformationCategory.DESCRIPTOR;
	}
}
