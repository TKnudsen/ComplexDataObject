package com.github.TKnudsen.ComplexDataObject.data.uncertainty.range;

import com.github.TKnudsen.ComplexDataObject.data.uncertainty.Double.IValueUncertainty;

/**
 * <p>
 * Representation of an uncertainty that is associated with a single value and
 * that covers a range. This means that this uncertainty describes how far a
 * value may deviate from the actual value: For a given value <code>v</code> and
 * an (absolute) value uncertainty range <code>(lower,upper)</code>, the actual
 * value will be in <code>[v+lower,v+upper]</code>.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2015-2018 Juergen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 */
public interface IValueUncertaintyRange extends IValueUncertainty {

	/**
	 * The upper bound for the deviation that the uncertain value may have from the
	 * actual value.
	 * 
	 * @return The upper bound
	 */
	double getUpperBound();

	/**
	 * The lower bound for the deviation that the uncertain value may have from the
	 * actual value.
	 * 
	 * @return The lower bound
	 */
	double getLowerBound();

	@Override
	default Double getAmount() {
		return getUpperBound() - getLowerBound();
	}
}
