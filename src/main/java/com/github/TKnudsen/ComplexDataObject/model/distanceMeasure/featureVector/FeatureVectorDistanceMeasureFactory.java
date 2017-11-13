package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.featureVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.github.TKnudsen.ComplexDataObject.data.features.AbstractFeatureVector;
import com.github.TKnudsen.ComplexDataObject.data.features.mixedData.MixedDataFeature;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.mixedData.MixedDataFeatureVectorFeatureSubsetDistanceMeasure;

/**
 * <p>
 * Title: FeatureVectorDistanceMeasureFactory
 * </p>
 * 
 * <p>
 * Description: what if the implementation of distance measure (be it numerical
 * or mixed is only available at runtime)? use this factory.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class FeatureVectorDistanceMeasureFactory {

	public static <FV extends AbstractFeatureVector<?, ?>> IFeatureVectorDistanceMeasure<FV> createDistanceMeasure(Collection<FV> featureVectors) {
		if (featureVectors == null || featureVectors.size() == 0)
			return null;

		Iterator<FV> iterator = featureVectors.iterator();
		if (iterator.hasNext()) {
			FV next = iterator.next();
			if (next instanceof NumericalFeatureVector)
				return (IFeatureVectorDistanceMeasure<FV>) new EuclideanDistanceMeasure();
			else
				return (IFeatureVectorDistanceMeasure<FV>) new MixedDataFeatureVectorFeatureSubsetDistanceMeasure(new ArrayList<>(next.getFeatureKeySet()));
		}

		return null;
	}
}
