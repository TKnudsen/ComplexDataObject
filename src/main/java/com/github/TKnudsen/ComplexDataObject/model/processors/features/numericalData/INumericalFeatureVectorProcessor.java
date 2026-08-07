package com.github.TKnudsen.ComplexDataObject.model.processors.features.numericalData;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVectorContainer;
import com.github.TKnudsen.ComplexDataObject.model.processors.IDataProcessor;

/**
 * <p>
 * Common contract for processors that operate on numerical feature vectors,
 * either as a plain list or wrapped in a {@link NumericalFeatureVectorContainer}.
 * </p>
 *
 * @version 1.03
 * @since 2016
 *
 *          TODO_GENERICS Could probably unify IMixedDataFeatureVectorProcessor
 *          and INumericalFeatureVectorProcessor and ICompledDataObjectProcessor
 */
public interface INumericalFeatureVectorProcessor extends IDataProcessor<NumericalFeatureVector> {

	public void process(NumericalFeatureVectorContainer container);
}
