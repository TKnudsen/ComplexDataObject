package com.github.TKnudsen.ComplexDataObject.model.processors.attributes.Double;

import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataProcessingCategory;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.IComplexDataObjectProcessor;

/**
 * <p>
 * Removes ComplexDataObject which contain a missing value.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2018-2020
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class MissingValueObjectRemover implements IComplexDataObjectProcessor {

	private final String attribute;
	private final Double missingValueIdentifier;

	public MissingValueObjectRemover(String attribute, Double missingValueIdentifier) {
		this.attribute = attribute;
		this.missingValueIdentifier = missingValueIdentifier;
	}

	@Override
	public void process(ComplexDataContainer container) {
		List<ComplexDataObject> toBeRemoved = new ArrayList<>();

		for (ComplexDataObject object : container)
			if (!isValid(object))
				toBeRemoved.add(object);

		for (ComplexDataObject fv : toBeRemoved)
			container.remove(fv);
	}

	@Override
	public void process(List<ComplexDataObject> data) {
		for (int i = 0; i < data.size(); i++)
			if (!isValid(data.get(i))) {
				data.remove(i);
				i--;
			}
	}

	private boolean isValid(ComplexDataObject object) {
		if (object == null)
			return false;

		if (object.getAttribute(attribute) == null)
			return false;

		if (!(object.getAttribute(attribute) instanceof Double))
			return false;

		Double d = (Double) object.getAttribute(attribute);

		if (Double.isNaN(d))
			return false;

		if (d == missingValueIdentifier)
			return false;

		return true;
	}

	@Override
	public DataProcessingCategory getPreprocessingCategory() {
		return DataProcessingCategory.DATA_CLEANING;
	}

	public String getAttribute() {
		return attribute;
	}

	public Double getMissingValueIdentifier() {
		return missingValueIdentifier;
	}

}
