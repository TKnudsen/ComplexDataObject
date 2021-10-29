package com.github.TKnudsen.ComplexDataObject.model.processors;

import com.github.TKnudsen.ComplexDataObject.data.uncertainty.IUncertainty;

/**
 * <p>
 * Title: IProcessingUncertaintyMeasure
 * </p>
 * 
 * <p>
 * Description: Baseline behavior of a data processing routine.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2011-2018
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public interface IProcessingUncertaintyMeasure<D, U extends IUncertainty<?>> {

	/**
	 * 
	 * @param originalData
	 * @param processedData
	 */
	public abstract void calculateUncertainty(D originalData, D processedData);

}
