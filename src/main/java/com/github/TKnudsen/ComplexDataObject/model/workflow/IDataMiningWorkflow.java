package com.github.TKnudsen.ComplexDataObject.model.workflow;

import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.IDistanceMeasure;
import com.github.TKnudsen.ComplexDataObject.model.processors.IDataProcessor;
import com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors.IDescriptor;

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
 * Copyright: Copyright (c) 2016-2018
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public interface IDataMiningWorkflow<O extends IDObject, F, FV extends AbstractFeatureVector<F, ? extends Feature<F>>, DESC extends IDescriptor<? super O, F, FV>>
		extends Function<List<O>, List<FV>> {

	public void addPreProcessor(IDataProcessor<O> preProcessor);

	public void setDescriptor(DESC descriptor);

	public IDistanceMeasure<FV> getDistanceMeasure();

	public void setDistanceMeasure(IDistanceMeasure<FV> distanceMeasure);

	public void addFeatureProcessor(IDataProcessor<FV> featureProcessor);
}
