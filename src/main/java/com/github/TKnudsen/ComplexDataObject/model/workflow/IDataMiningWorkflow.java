package com.github.TKnudsen.ComplexDataObject.model.workflow;

import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.IDistanceMeasure;
import com.github.TKnudsen.ComplexDataObject.model.processors.IDataProcessor;
import com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors.IDescriptor;

/**
 * <p>
 * Contract for a data mining workflow pipeline that turns a list of input
 * objects into a list of feature vectors: pre-processors condition the raw
 * objects, a descriptor extracts feature vectors, feature processors refine
 * them, and a distance measure enables subsequent comparison of the
 * resulting feature vectors.
 * </p>
 *
 * @version 1.04
 * @since 2016
 */
public interface IDataMiningWorkflow<O, F, FV extends AbstractFeatureVector<F, ? extends Feature<F>>>
		extends Function<List<O>, List<FV>> {

	public void addPreProcessor(IDataProcessor<O> preProcessor);
	
	public void addPreProcessor(IDataProcessor<O> processor, boolean firstPosition);

	public void setDescriptor(IDescriptor<O, FV> descriptor);

	public IDistanceMeasure<FV> getDistanceMeasure();

	public void setDistanceMeasure(IDistanceMeasure<FV> distanceMeasure);

	public void addFeatureProcessor(IDataProcessor<FV> featureProcessor);
}
