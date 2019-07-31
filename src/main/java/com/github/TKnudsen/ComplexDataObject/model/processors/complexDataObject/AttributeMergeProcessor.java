package com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject;

import java.util.List;
import java.util.Objects;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;

/**
 * merges a mergeAttribute in a baseAttribute
 *
 */
public class AttributeMergeProcessor implements IComplexDataObjectProcessor {

	private final String baseAttribute;
	private final String mergeAttribute;

	private boolean overwriteExistingValues;

	private boolean provideSummary = true;

	public AttributeMergeProcessor(String baseAttribute, String mergeAttribute) {
		this(baseAttribute, mergeAttribute, false);
	}

	public AttributeMergeProcessor(String baseAttribute, String mergeAttribute, boolean overwriteExistingValues) {
		this.baseAttribute = baseAttribute;
		this.mergeAttribute = mergeAttribute;
		this.overwriteExistingValues = overwriteExistingValues;
	}

	@Override
	public void process(List<ComplexDataObject> data) {
		process(new ComplexDataContainer(data));
	}

	@Override
	public void process(ComplexDataContainer container) {
		Objects.requireNonNull(container);

		int count = 0;

		for (ComplexDataObject cdo : container) {
			Object baseValue = cdo.getAttribute(baseAttribute);
			Object mergeValue = cdo.getAttribute(mergeAttribute);

			if (overwriteExistingValues || (baseValue == null) && mergeValue != null) {
				cdo.add(baseAttribute, mergeValue);
				count++;
			}
		}

		container.remove(mergeAttribute);

		if (provideSummary)
			System.out.println("AttributeMergeProcessor: merged attributes [" + baseAttribute + "] and ["
					+ mergeAttribute + "]. Information used from merged attribute: "
					+ MathFunctions.round(count / (double) container.size(), 4) + "%");
	}

	@Override
	public DataProcessingCategory getPreprocessingCategory() {
		return DataProcessingCategory.DATA_CLEANING;
	}

	public boolean isProvideSummary() {
		return provideSummary;
	}

	public void setProvideSummary(boolean provideSummary) {
		this.provideSummary = provideSummary;
	}

}
