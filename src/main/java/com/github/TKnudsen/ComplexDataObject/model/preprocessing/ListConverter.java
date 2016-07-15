package com.github.TKnudsen.ComplexDataObject.model.preprocessing;

import com.github.TKnudsen.ComplexDataObject.data.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.preprocessing.utility.IItemSplitter;

/**
 * Used to split each value for a given attribute of each
 * {@link ComplexDataObject} in the {@link ComplexDataContainer}, using a
 * routine implementing {@link IItemSplitter}.
 * 
 * @author Robert Heimbach
 *
 */
public class ListConverter implements IPreprocessingRoutine {

	private String attribute;
	private IItemSplitter splitter;

	public ListConverter(String attribute, IItemSplitter splitter) {
		this.attribute = attribute;
		this.splitter = splitter;
	}

	@Override
	public void process(ComplexDataContainer container) {

		for (ComplexDataObject complexDataObject : container) {

			// Load the old value
			Object oldValue = complexDataObject.get(attribute);

			// Split the old value and store if not null
			if (oldValue != null) {
				complexDataObject.add(attribute, splitter.split(oldValue));
			}
		}
	}
}
