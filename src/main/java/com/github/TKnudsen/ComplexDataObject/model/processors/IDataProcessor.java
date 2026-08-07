package com.github.TKnudsen.ComplexDataObject.model.processors;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataProcessingCategory;

/**
 * <p>
 * Baseline behavior of a data processing routine.
 * </p>
 *
 * @version 1.05
 * @since 2011
 */
public interface IDataProcessor<D> {

	public void process(List<D> data);

	public DataProcessingCategory getPreprocessingCategory();
}
