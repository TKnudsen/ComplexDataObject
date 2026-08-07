package com.github.TKnudsen.ComplexDataObject.model.scoring.functions;

import java.util.Comparator;

/**
 * <p>
 * Comparator that orders AttributeScoringFunction instances alphabetically
 * by the name of the attribute they score.
 * </p>
 */
public class AttributeScoringFunctionComparator implements Comparator<AttributeScoringFunction<?>> {

	@Override
	public int compare(AttributeScoringFunction<?> o1, AttributeScoringFunction<?> o2) {
		return o1.getAttribute().compareTo(o2.getAttribute());
	}

}
