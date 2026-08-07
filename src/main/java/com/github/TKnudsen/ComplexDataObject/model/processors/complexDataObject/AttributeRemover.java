package com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

/**
 * <p>
 * Removes a single named attribute from every ComplexDataObject in a
 * container or list, effectively deleting that attribute/column from the
 * data set.
 * </p>
 *
 * @version 1.01
 * @since 2016
 */

public class AttributeRemover implements IComplexDataObjectProcessor {

	private String attributeString;

	public AttributeRemover(String attributeString) {
		this.attributeString = attributeString;
	}

	@Override
	public void process(ComplexDataContainer container) {
		if (container == null)
			return;

		container.remove(attributeString);
	}

	@Override
	public void process(List<ComplexDataObject> data) {
		for (ComplexDataObject object : data)
			object.removeAttribute(attributeString);
	}

	public String getAttributeString() {
		return attributeString;
	}

	public void setAttributeString(String attributeString) {
		this.attributeString = attributeString;
	}

	@Override
	public DataProcessingCategory getPreprocessingCategory() {
		return DataProcessingCategory.DATA_REDUCTION;
	}
}
