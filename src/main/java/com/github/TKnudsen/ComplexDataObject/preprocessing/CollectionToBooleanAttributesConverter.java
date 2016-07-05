package com.github.TKnudsen.ComplexDataObject.preprocessing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.data.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.enums.FuzzyBooleanCategory;

/**
 * <p>
 * Title: StringCollectionToBooleanAttributesConverter
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

public class CollectionToBooleanAttributesConverter implements IPreprocessingRoutine {

	private String attributeString;

	public CollectionToBooleanAttributesConverter(String attributeString) {
		this.attributeString = attributeString;
	}

	@Override
	public void process(ComplexDataContainer container) {

		// 1) gather all observations
		Set<String> observations = new HashSet<>();
		Iterator<ComplexDataObject> iterator = container.iterator();
		while (iterator.hasNext()) {
			ComplexDataObject next = iterator.next();
			Object object = next.get(attributeString);
			if (object instanceof Collection<?>) {
				Collection<?> collection = (Collection<?>) object;
				for (Object element : collection)
					if (element instanceof String)
						observations.add((String) element);
			}
		}

		// 2) create new FuzzyBoolenCategory attributes
		for (String string : observations)
			container.add(attributeString + "_" + string, FuzzyBooleanCategory.class, FuzzyBooleanCategory.NO_INFORMATION);

		// 3) add entries for the objects
		iterator = container.iterator();
		while (iterator.hasNext()) {
			ComplexDataObject next = iterator.next();
			Object object = next.get(attributeString);
			Collection<?> collection = null;
			if (object != null && object instanceof Collection<?>)
				collection = (Collection<?>) object;

			for (String string : observations) {
				if (collection == null)
					next.add(attributeString + "_" + string, FuzzyBooleanCategory.NO_INFORMATION);
				else if (collection != null && collection.contains(string))
					next.add(attributeString + "_" + string, FuzzyBooleanCategory.YES);
				else
					next.add(attributeString + "_" + string, FuzzyBooleanCategory.NO);
			}
		}
	}

	public String getAttributeString() {
		return attributeString;
	}

	public void setAttributeString(String attributeString) {
		this.attributeString = attributeString;
	}

}
