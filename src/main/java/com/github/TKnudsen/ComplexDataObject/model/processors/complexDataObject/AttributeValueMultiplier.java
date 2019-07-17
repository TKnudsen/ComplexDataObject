package com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject;

import java.util.Arrays;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

/**
 * <p>
 * Multiplies a scalar with the numerical values in the given numerical
 * attribute.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017-2019
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class AttributeValueMultiplier implements IComplexDataObjectProcessor {

	private final String attribute;
	private final double scalar;

	public AttributeValueMultiplier(String attribute, double scalar) {
		this.attribute = attribute;
		this.scalar = scalar;
	}

	@Override
	public void process(List<ComplexDataObject> data) {
		for (ComplexDataObject cdo : data) {
			if (cdo == null)
				return;

			Object o = cdo.getAttribute(attribute);
			if (o == null)
				continue;

			if (!(o instanceof Number))
				continue;

			Number value = (Number) o;

			if (Double.isNaN(value.doubleValue()))
				continue;

			cdo.add(attribute, value.doubleValue() * scalar);
		}
	}

	@Override
	public void process(ComplexDataContainer container) {
		for (ComplexDataObject complexDataObject : container)
			process(Arrays.asList(complexDataObject));
	}

	@Override
	public DataProcessingCategory getPreprocessingCategory() {
		return DataProcessingCategory.SECONDARY_DATA_PROVIDER;
	}

}
