package com.github.TKnudsen.ComplexDataObject.model.processors;

import com.github.TKnudsen.ComplexDataObject.data.uncertainty.IUncertainty;

/**
 * @deprecated has not proven to be useful. better try to split processors and
 *             measures. refactoring needed.
 * @author jubernar
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
