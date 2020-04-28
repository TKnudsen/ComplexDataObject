package com.github.TKnudsen.ComplexDataObject.model.transformations.dimensionalityReduction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.transformations.descriptors.IDescriptor;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.LinearNormalizationFunction;
import com.github.TKnudsen.ComplexDataObject.model.transformations.normalization.NormalizationFunction;

/**
 * <p>
 * The dimensionality reduction pipeline helps to streamline dim-red approaches.
 * Embedded is the following:
 * 
 * - an upstream descriptor which transforms the objects of type X into
 * NumericalFeatureVector
 * 
 * - a IDimensionalityReduction routine
 * 
 * - some useful downstream functionality to look-up X in the low-dimensional
 * space, as well as in the relative low-dimensional space.
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2019 Juergen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class DimensionalityReductionPipeline<X> {

	private final IDescriptor<X, NumericalFeatureVector> descriptor;
	private final Collection<X> data;

	private IDimensionalityReduction<NumericalFeatureVector> dimensionalityReduction;

	private Map<X, NumericalFeatureVector> featureVectors;
	private Map<NumericalFeatureVector, NumericalFeatureVector> featureVectorsLowDMap;

	// relative coordinates per dimension
	private SortedMap<Integer, Function<X, Double>> lowDimRelativeWorldCoordinates;

	public DimensionalityReductionPipeline(Collection<X> data, IDescriptor<X, NumericalFeatureVector> descriptor) {
		this.data = data;
		this.descriptor = descriptor;
	}

	/**
	 * pre-processing: transform x into NumericalFeatureVectors
	 * 
	 * @return
	 */
	public Map<X, NumericalFeatureVector> getFeatureVectorsMap() {
		if (featureVectors == null) {
			featureVectors = new HashMap<X, NumericalFeatureVector>();

			ArrayList<X> dataList = new ArrayList<>(data);
			List<NumericalFeatureVector> transform = descriptor.transform(dataList);

			for (int i = 0; i < transform.size(); i++) {
				NumericalFeatureVector fv = transform.get(i);
				X x = dataList.get(i);
				featureVectors.put(x, fv);
			}
		}
		return featureVectors;
	}

	/**
	 * Implementations of IDimensionalityReduction either receive X through the
	 * constructor or via transform(X). Also, the particular
	 * IDimensionalityReduction may not be known in this context. Thus, it is
	 * necessary to receive the IDimensionalityReduction routine from external.
	 * 
	 * Note: the data X is used (instantly) to make the transformation!
	 * 
	 * @param dimensionalityReduction
	 */
	public void setDimensionalityReduction(IDimensionalityReduction<NumericalFeatureVector> dimensionalityReduction) {

		featureVectorsLowDMap = null;
		lowDimRelativeWorldCoordinates = null;

		// high-dim feature vectors
		Map<X, NumericalFeatureVector> featureVectorsMap = getFeatureVectorsMap();
		List<NumericalFeatureVector> input = new ArrayList<>(featureVectorsMap.values());

		// transform (refers to the mapping, not necessarily the calculation)
		dimensionalityReduction.transform(input);

		// store result
		featureVectorsLowDMap = dimensionalityReduction.getMapping();

		// set dim red
		this.dimensionalityReduction = dimensionalityReduction;
	}

	/**
	 * receive the low-dim representation of X
	 * 
	 * @param X
	 * @return
	 */
	public NumericalFeatureVector getLowDimFeatureVector(X x) {

		// the maps are calculated in a lazy way. it they still produce no output for X,
		// the data element maybe unknown (not part of the dim-red process). In such a
		// case the dim red model may be able to transform the new data element.
		if (getFeatureVectorsMap() == null || getFeatureVectorsMap().get(x) == null) {
			if (dimensionalityReduction != null) {
				List<NumericalFeatureVector> fv = descriptor.transform(x);
				List<NumericalFeatureVector> transform = dimensionalityReduction.transform(fv);

				if (transform.size() == 1)
					return transform.get(0);
			}

			return null;
		}

		return featureVectorsLowDMap.get(getFeatureVectorsMap().get(x));
	}

	/**
	 * provides relative coordinates for the low-dimensional output space. Can be
	 * used for visualization purposes (position/color mappings), etc.
	 * 
	 * @return
	 */
	public Double getLowDimRelativeWorldCoordinates(X x, int dim) {
		if (lowDimRelativeWorldCoordinates == null)
			refreshLowDimRelativeWorldCoordinates();

		if (lowDimRelativeWorldCoordinates.containsKey(dim))
			return lowDimRelativeWorldCoordinates.get(dim).apply(x);

		return 0.0;
	}

	/**
	 * provides relative coordinates for the low-dimensional output space. Can be
	 * used for visualization purposes (position/color mappings), etc.
	 * 
	 * @return
	 */
	public Double[] getLowDimRelativeWorldCoordinates(X x) {
		if (lowDimRelativeWorldCoordinates == null)
			refreshLowDimRelativeWorldCoordinates();

		NumericalFeatureVector lowDimFeatureVector = getLowDimFeatureVector(x);

		int dim = lowDimFeatureVector.getDimensions();
		Double[] relativeValues = new Double[dim];
		for (int i = 0; i < dim; i++)
			relativeValues[i] = getLowDimRelativeWorldCoordinates(x, i);

		return relativeValues;
	}

	private void refreshLowDimRelativeWorldCoordinates() {
		lowDimRelativeWorldCoordinates = null;

		if (featureVectorsLowDMap == null)
			return;

		lowDimRelativeWorldCoordinates = new TreeMap<>();

		SortedMap<Integer, Collection<Double>> dimensionsOutput = new TreeMap<Integer, Collection<Double>>();

		for (X x : data) {
			NumericalFeatureVector fv = getLowDimFeatureVector(x);

			if (fv != null) {
				for (int i = 0; i < fv.getDimensions(); i++) {
					if (dimensionsOutput.get(i) == null)
						dimensionsOutput.put(i, new ArrayList<>());
					Collection<Double> collection = dimensionsOutput.get(i);
					collection.add(fv.get(i));
				}
			}
		}

		for (Integer dim : dimensionsOutput.keySet()) {
			Collection<Double> dimOutput = dimensionsOutput.get(dim);

			if (dimOutput.size() > 0) {
				Function<X, Double> function = new Function<X, Double>() {

					NormalizationFunction normalization = new LinearNormalizationFunction(dimOutput, true);

					@Override
					public Double apply(X t) {
						if (getLowDimFeatureVector(t) == null)
							return 0.0;
						if (getLowDimFeatureVector(t).getDimensions() < dim)
							return 0.0;
						double d = getLowDimFeatureVector(t).get(dim);
						return normalization.apply(d).doubleValue();
					}
				};

				lowDimRelativeWorldCoordinates.put(dim, function);
			}
		}
	}
}
