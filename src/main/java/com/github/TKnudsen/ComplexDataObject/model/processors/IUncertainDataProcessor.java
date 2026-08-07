package com.github.TKnudsen.ComplexDataObject.model.processors;

import com.github.TKnudsen.ComplexDataObject.data.uncertainty.IUncertainty;

/**
 * <p>
 * Associates a data processing step with a measure of the uncertainty it
 * introduces, by comparing original and processed data and returning an
 * {@link IProcessingUncertaintyMeasure}.
 * </p>
 *
 * @deprecated has not proven to be useful. better try to split processors and
 *             measures. refactoring needed.
 *
 * @param <D>
 * @param <U>
 */
public interface IUncertainDataProcessor<D, U extends IUncertainty<?>> {

	/**
	 * @deprecated has not proven to be useful. better try to split processors and
	 *             measures. refactoring needed.
	 * 
	 * @param originalData
	 * @param processedData
	 * @return
	 */
	public IProcessingUncertaintyMeasure<D, U> getUncertaintyMeasure(D originalData, D processedData);
}
