package com.github.TKnudsen.ComplexDataObject.preprocessing;

import com.github.TKnudsen.ComplexDataObject.data.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.ComplexDataObject;

/**
 * <p>
 * Title: NullAttributeValueObjectRemover
 * </p>
 * 
 * <p>
 * Description: Removes ComplexDataObjects with null values for a given
 * Attribute.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.00
 */
public class NullAttributeValueObjectRemover implements IPreprocessingRoutine {

	private String attribute;

	public NullAttributeValueObjectRemover(String attribute) {
		this.attribute = attribute;
	}

	@Override
	public void process(ComplexDataContainer container) {

		if (attribute == null)
			return;

		for (ComplexDataObject complexDataObject : container) {
			Object value = complexDataObject.get(attribute);
			if (value == null)
				complexDataObject.remove(attribute);
		}
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
}
