package com.github.TKnudsen.ComplexDataObject.data.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IFeatureVectorObject;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IMasterProvider;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;
import com.github.TKnudsen.ComplexDataObject.data.keyValueObject.KeyValueObject;

/**
 * <p>
 * Title: AbstractFeatureDataObject
 * </p>
 * 
 * <p>
 * Description: general feature representation of a given object. Provides basic
 * attributes and functionality.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */

public abstract class AbstractFeatureVector<O, F extends Feature<O>> extends KeyValueObject<Object> implements ISelfDescription, IMasterProvider, Cloneable, IFeatureVectorObject<O, F> {

	private String name;

	private String description;

	private IDObject master;

	protected List<F> featuresList;

	// @Deprecated
	// /**
	// * array representation of the features. Deprecated due to redundancy.
	// */
	// protected F[] featuresArray;

	protected Map<String, F> featuresMap;

	public AbstractFeatureVector(List<F> features) {
		this.featuresList = features;

		generalizeFromList();

		checkForDuplicateFeatures();
	}

	public AbstractFeatureVector(F[] features) {
		generalizeFromArray(features);

		checkForDuplicateFeatures();
	}

	public AbstractFeatureVector(Map<String, F> featuresMap) {
		this.featuresMap = featuresMap;

		generalizeFromMap();

		checkForDuplicateFeatures();
	}

	protected void generalizeFromList() {
		featuresMap = null;

		if (featuresList == null)
			return;

		featuresMap = new HashMap<>();

		for (int i = 0; i < featuresList.size(); i++)
			if (featuresList.get(i) != null && featuresList.get(i).getFeatureName() != null)
				featuresMap.put(featuresList.get(i).getFeatureName(), featuresList.get(i));
	}

	protected void generalizeFromArray(F[] featuresArray) {
		featuresList = null;
		featuresMap = null;

		if (featuresArray == null)
			return;

		featuresList = new ArrayList<>();
		featuresMap = new HashMap<>();

		for (F feature : featuresArray) {
			featuresList.add(feature);
			if (feature != null && feature.getFeatureName() != null)
				featuresMap.put(feature.getFeatureName(), feature);
		}
	}

	protected void generalizeFromMap() {
		featuresList = null;

		if (featuresMap == null)
			return;

		featuresList = new ArrayList<>();

		for (String s : featuresMap.keySet())
			featuresList.add(featuresMap.get(s));
	}

	protected boolean checkForDuplicateFeatureName(String featureName) {
		for (F entry : featuresList)
			if (entry != null && entry.getFeatureName() != null)
				if (featureName.equals(entry.getFeatureName()))
					return true;
		return false;
	}

	protected void checkForDuplicateFeatures() {
		Set<String> featureNames = new HashSet<String>();
		String errorString = "";

		for (F entry : featuresList)
			if (entry != null && entry.getFeatureName() != null)
				if (featureNames.contains(entry.getFeatureName()))
					errorString = (errorString + entry.getFeatureName() + ", ");
				else
					featureNames.add(entry.getFeatureName());

		if (!errorString.equals(""))
			throw new IllegalArgumentException("FeatureDataObject - duplicate Features: " + errorString.substring(0, errorString.length() - 2));
	}

	@Override
	public F getFeature(int index) {
		if (featuresList != null)
			if (featuresList.size() > index)
				return featuresList.get(index);
			else
				throw new IndexOutOfBoundsException();
		return null;
	}

	@Override
	public F getFeature(String featureName) {
		if (featuresMap != null)
			return featuresMap.get(featureName);
		return null;
	}

	@Override
	/**
	 * Expensive variant of adding a feature into the three internal data
	 * representations. First checks whether a feature is already contained.
	 */
	public void addFeature(F feature) {
		if (feature == null)
			return;

		if (checkForDuplicateFeatureName(feature.getFeatureName()))
			return;

		if (featuresList == null)
			featuresList = new ArrayList<>();
		if (!featuresList.contains(feature))
			featuresList.add(feature);

		if (featuresMap == null)
			featuresMap = new HashMap<>();
		featuresMap.put(feature.getFeatureName(), feature);
	}

	@Override
	/**
	 * Sets a feature at a given index. Does not handle index exceptions.
	 * 
	 * @param index
	 * @param feature
	 */
	public void setFeature(int index, F feature) {
		if (featuresList != null)
			featuresList.set(index, feature);
		if (featuresMap == null)
			featuresMap = new HashMap<>();
		featuresMap.put(feature.getFeatureName(), feature);
	}

	@Override
	public F removeFeature(String featureName) {
		F feature = getFeature(featureName);

		if (feature != null)
			if (featuresList != null)
				featuresList.remove(feature);

		if (featuresMap != null)
			featuresMap.remove(feature.getFeatureName());

		return feature;
	}

	@Override
	public List<F> getVectorRepresentation() {
		return Collections.unmodifiableList(featuresList);
	}

	@SuppressWarnings("unchecked")
	@Override
	public F[] getArrayRepresentation() {
		return featuresList.toArray((F[]) new Object[featuresList.size()]);
	}

	@Override
	public Set<String> getFeatureKeySet() {
		if (featuresMap == null)
			return null;

		return featuresMap.keySet();
	}

	@Override
	public int sizeOfFeatures() {
		return featuresList.size();
	}

	@Override
	/**
	 * If the FeatureDataObject is derived from another IDObject this can be
	 * specified here.
	 */
	public IDObject getMaster() {
		return master;
	}

	/**
	 * Get the IDObject of the FeatureDataObject - if specified.
	 * 
	 * @param master
	 */
	public void setMaster(IDObject master) {
		this.master = master;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		int hash = 1;

		if (master != null)
			hash = 37 * hash + master.hashCode();

		if (featuresList == null)
			hash = 37 * hash;
		else
			for (F feature : featuresList)
				hash = 37 * hash + feature.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String string = this.getName() + ", " + this.getID() + "\t";

		string += "[";
		for (F feature : this.getVectorRepresentation())
			string += feature.toString();
		string += "]\t";

		return string;
	}

}
