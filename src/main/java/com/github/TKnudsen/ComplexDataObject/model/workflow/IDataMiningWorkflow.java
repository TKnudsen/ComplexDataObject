package com.github.TKnudsen.ComplexDataObject.model.workflow;

import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;
import com.github.TKnudsen.ComplexDataObject.model.descriptors.IDescriptor;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.IDistanceMeasure;
import com.github.TKnudsen.ComplexDataObject.model.preprocessing.IDataProcessing;
import com.github.TKnudsen.ComplexDataObject.model.preprocessing.features.IFeatureVectorProcessor;

/**
 * <p>
 * Title: IDataMiningWorkflow
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 */
public interface IDataMiningWorkflow<O extends IDObject, F extends Object, FV extends AbstractFeatureVector<F, ? extends Feature<F>>> extends Function<List<O>, List<FV>> {

	public void addProcessor(IDataProcessing<O> processor);

	public void setDescriptor(IDescriptor<O, F, FV> descriptor);

	public void setDistanceMeasure(IDistanceMeasure<FV> distanceMeasure);

	public void addFeatureProcessor(IFeatureVectorProcessor<F, FV> featureProcessor);
}
