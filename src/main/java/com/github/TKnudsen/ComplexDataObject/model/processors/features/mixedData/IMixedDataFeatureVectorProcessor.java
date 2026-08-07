package com.github.TKnudsen.ComplexDataObject.model.processors.features.mixedData;

import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeatureContainer;
import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.processors.IDataProcessor;

/**
 * <p>
 * Interface for processors that operate on MixedDataFeatureVector
 * instances. It extends the generic IDataProcessor with an additional
 * process method that operates directly on a MixedDataFeatureContainer.
 * </p>
 *
 * @version 1.04
 * @since 2017
 *
 *          TODO_GENERICS Could probably unify IMixedDataFeatureVectorProcessor
 *          and INumericalFeatureVectorProcessor and ICompledDataObjectProcessor
 */
public interface IMixedDataFeatureVectorProcessor extends IDataProcessor<MixedDataFeatureVector> {

	public void process(MixedDataFeatureContainer container);
}
