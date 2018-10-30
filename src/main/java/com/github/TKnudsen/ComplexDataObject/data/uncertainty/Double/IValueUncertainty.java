package com.github.TKnudsen.ComplexDataObject.data.uncertainty.Double;

import com.github.TKnudsen.ComplexDataObject.data.uncertainty.IUncertainty;

/**
 * <p>
 * Title: IValueUncertainty
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
 * @version 1.03
 */
public interface IValueUncertainty<T> extends IUncertainty<T> {

	public T getVariation();

}