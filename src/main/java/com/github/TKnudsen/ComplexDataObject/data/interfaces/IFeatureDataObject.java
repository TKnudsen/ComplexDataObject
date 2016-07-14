package com.github.TKnudsen.ComplexDataObject.data.interfaces;

import java.util.List;
import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.data.features.Feature;
import com.github.TKnudsen.ComplexDataObject.data.features.FeatureType;

/**
 * <p>
 * Title: IAlgorithmDataObject
 * </p>
 *
 * <p>
 * Description: Interface for all objects that can be accepted by algorithms
 * (data mining, machine learning, information retrieval, etc.).
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.0
 */

public interface IFeatureDataObject<O extends Object, F extends Feature<O>> extends IDObject, IKeyValueProvider<Object> {

	public F getFeature(int index);

	public F getFeature(String featureName);

	public void addFeature(F feature);

	public void addFeature(String featureName, O value, FeatureType type);

	public void setFeature(int index, F feature);

	public F removeFeature(String featureName);

	public IFeatureDataObject<O, F> subTuple(int fromIndex, int toIndex);

	public List<F> getVectorRepresentation();

	public F[] getArrayRepresentation();

	public Set<String> getFeatureKeySet();

	public int sizeOfFeatures();
}
