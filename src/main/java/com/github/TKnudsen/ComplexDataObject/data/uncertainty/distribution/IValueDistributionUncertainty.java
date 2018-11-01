package com.github.TKnudsen.ComplexDataObject.data.uncertainty.distribution;

import com.github.TKnudsen.ComplexDataObject.data.uncertainty.Double.IValueUncertainty;

/**
 * <p>
 * Title: IValueDistributionUncertainty
 * </p>
 * 
 * <p>
 * Description: Interface for uncertainty information for qualitative data.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2018
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public interface IValueDistributionUncertainty<T> extends IValueUncertainty<T> {

	public T getUncertaintyMinimum();

	public T getUncertaintyMedian();

	public T getUncertaintyMaximum();

}