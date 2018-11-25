package com.github.TKnudsen.ComplexDataObject.data.uncertainty;

/**
 * <p>
 * Basic interface for uncertainty data modeling.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2015-2018 Juergen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public interface IUncertainty<T> {

	/**
	 * Returns a number representing the amount of the uncertainty.<br>
	 * <br>
	 * This is solely a representation of how "large" the uncertainty is. This
	 * might, for example, be the difference between an original and a processed
	 * value. As such, it might be positive or negative. Some consumers of this
	 * value will only be interested in the <i>absolute</i> value.<br>
	 * 
	 * @return The amount of uncertainty
	 */
	public T getAmount();
}
