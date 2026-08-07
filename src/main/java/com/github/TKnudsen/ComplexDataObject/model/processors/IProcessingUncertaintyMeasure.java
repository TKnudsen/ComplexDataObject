package com.github.TKnudsen.ComplexDataObject.model.processors;

import com.github.TKnudsen.ComplexDataObject.data.uncertainty.IUncertainty;

/**
 * <p>
 * Baseline behavior of a data processing routine.
 * </p>
 *
 * @version 1.02
 * @since 2011
 */
public interface IProcessingUncertaintyMeasure<D, U extends IUncertainty<?>> {

	/**
	 * 
	 * @param originalData
	 * @param processedData
	 */
	public abstract void calculateUncertainty(D originalData, D processedData);

}
