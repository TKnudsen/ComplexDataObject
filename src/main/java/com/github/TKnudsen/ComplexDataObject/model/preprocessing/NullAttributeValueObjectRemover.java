package com.github.TKnudsen.ComplexDataObject.model.preprocessing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * @version 1.01
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

		List<ComplexDataObject> removes = new ArrayList<>();

		for (Iterator<ComplexDataObject> iterator = container.iterator(); iterator.hasNext();) {
			ComplexDataObject complexDataObject = iterator.next();
			Object value = complexDataObject.get(attribute);
			if (value == null)
				removes.add(complexDataObject);
		}

		for (ComplexDataObject complexDataObject : removes)
			container.remove(complexDataObject);
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	@Override
	public PreprocessingCategory getPreprocessingCategory() {
		return PreprocessingCategory.DATA_CLEANING;
	}
}
