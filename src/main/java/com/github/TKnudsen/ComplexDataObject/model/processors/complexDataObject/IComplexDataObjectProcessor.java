package com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.processors.IDataProcessor;

/**
 * <p>
 * Interface for processors that operate on ComplexDataObject instances. It
 * extends the generic IDataProcessor with an additional process method that
 * operates directly on a ComplexDataContainer.
 * </p>
 *
 * @version 1.01
 * @since 2016
 *
 * TODO_GENERICS Could probably unify IMixedDataFeatureVectorProcessor and INumericalFeatureVectorProcessor and ICompledDataObjectProcessor
 */

public interface IComplexDataObjectProcessor extends IDataProcessor<ComplexDataObject> {

	public void process(ComplexDataContainer container);
}
