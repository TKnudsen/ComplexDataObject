package com.github.TKnudsen.ComplexDataObject.model.comparators;

import java.util.Comparator;

/**
 * <p>
 * compares numbers
 * </p>
 *
 * @version 1.01
 * @since 2018
 */
public class NumberComparator implements Comparator<Number> {

	@Override
	public int compare(Number x, Number y) {
		if (x == null && y == null)
			return 0;
		if (x == null)
			return 1;
		else if (y == null)
			return -1;

		Double a = x.doubleValue();
		Double b = y.doubleValue();

		return Double.compare(a, b);
	}
}