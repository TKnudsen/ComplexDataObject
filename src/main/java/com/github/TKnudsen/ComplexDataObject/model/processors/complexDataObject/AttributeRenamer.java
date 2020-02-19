package com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.DataSchema;
import com.github.TKnudsen.ComplexDataObject.data.DataSchemaEntry;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainers;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

/**
 * <p>
 * Renames an attribute
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2020
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */

public class AttributeRenamer implements IComplexDataObjectProcessor {

	private final String attributeNameOld;
	private final String attributeNameNew;

	public AttributeRenamer(String attributeNameOld, String attributeNameNew) {
		this.attributeNameOld = attributeNameOld;
		this.attributeNameNew = attributeNameNew;
	}

	@Override
	public void process(ComplexDataContainer container) {
		if (container == null)
			return;

		if (!container.containsAttribute(attributeNameOld))
			System.out.println("AttributeRemover: ComplexDataContainer does not contain attribute " + attributeNameOld
					+ ", skip.");

		DataSchema dataSchema = ComplexDataContainers.dataSchema(container);
		DataSchemaEntry entry = dataSchema.getAttributeEntry(getAttributeNameOld());
		container.addAttribute(getAttributeNameNew(), entry.getType(), entry.getDefaultValue());

		for (ComplexDataObject cdo : container)
			cdo.add(getAttributeNameNew(), cdo.getAttribute(getAttributeNameOld()));

		container.remove(getAttributeNameOld());
	}

	@Override
	public void process(List<ComplexDataObject> data) {
		process(new ComplexDataContainer(data));
	}

	@Override
	public DataProcessingCategory getPreprocessingCategory() {
		return DataProcessingCategory.DATA_CLEANING;
	}

	public String getAttributeNameOld() {
		return attributeNameOld;
	}

	public String getAttributeNameNew() {
		return attributeNameNew;
	}
}
