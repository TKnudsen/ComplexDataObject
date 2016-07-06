package com.github.TKnudsen.ComplexDataObject.preprocessing;

import com.github.TKnudsen.ComplexDataObject.data.ComplexDataContainer;

/**
 * <p>
 * Title: AttributeRemover
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */

public class AttributeRemover implements IPreprocessingRoutine {

	private String attributeString;

	public AttributeRemover(String attributeString) {
		this.attributeString = attributeString;
	}

	@Override
	public void process(ComplexDataContainer container) {
		container.remove(attributeString);
	}

	public String getAttributeString() {
		return attributeString;
	}

	public void setAttributeString(String attributeString) {
		this.attributeString = attributeString;
	}
}
