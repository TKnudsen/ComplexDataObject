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
 * Title: AbstractFeatureVector
 * </p>
 * 
 * <p>
 * Description: general feature representation of a given object. Provides basic
 * attributes and functionality.
 * 
 * Update: featuresMap does not need to be sorted any more. Improves
 * performance.
 * 
 * Update: Changed KeyValueObject<Object> to the non-generic KeyValueObject
 * form.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2024
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.07
 */
public abstract class AbstractFeatureVector<O, F extends Feature<O>> extends KeyValueObject
		implements ISelfDescription, IMasterProvider, Cloneable, IFeatureVectorObject<O, F> {

	private String name;

	private String description;

	private IDObject master;

	protected List<F> featuresList;

	/**
	 * TODO recommendation to replace the SortedMap criterion by a simple Map
	 * criterion. This will increase performance.
	 * 
	 * Though, it needs to be checked if an external source expects a sorted
	 * keySet(). Ongoing process.
	 */
	// protected SortedMap<String, F> featuresMap;
	protected Map<String, F> featuresMap;

	protected AbstractFeatureVector() {
		featuresList = new ArrayList<>();
		createFeatureNamesMap();
	}

	public AbstractFeatureVector(List<F> features) {
		this.featuresList = features;

		generalizeFromList();
	}

	public AbstractFeatureVector(F[] features) {
		generalizeFromArray(features);
	}

	/**
	 * 
	 * @param featuresMap Update: featuresMap does not need to be sorted any more.
	 *                    Improves performance.
	 */

	public AbstractFeatureVector(Map<String, F> featuresMap) {
		this.featuresMap = featuresMap;

		generalizeFromMap();
	}

	@Override
	public abstract AbstractFeatureVector<O, F> clone();

	protected void createFeatureNamesMap() {
		featuresMap = null;

		if (featuresList == null)
			return;

		featuresMap = new HashMap<>();

		for (int i = 0; i < featuresList.size(); i++)
			if (featuresList.get(i) != null && featuresList.get(i).getFeatureName() != null)
				featuresMap.put(featuresList.get(i).getFeatureName(), featuresList.get(i));
		checkFeatureNameConsistency();
	}

	protected void generalizeFromArray(F[] featuresArray) {
		featuresList = null;
		featuresMap = null;

		if (featuresArray == null)
			return;

		featuresList = new ArrayList<>();
		featuresMap = null;

		for (F feature : featuresArray) {
			featuresList.add(feature);
			featuresMap.put(feature.getFeatureName(), feature);
			// refactoring. lazy implementation to save computation time
			// if (feature != null && feature.getFeatureName() != null)
			// featuresMap.put(feature.getFeatureName(), feature);
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

	protected void generalizeFromList() {
		featuresMap = new HashMap<>();

		for (F feature : featuresList)
			featuresMap.put(feature.getFeatureName(), feature);
	}

	/**
	 * really nececssary?
	 * 
	 * @param featureName
	 * @return
	 */
	protected boolean checkForDuplicateFeatureName(String featureName) {
		for (F entry : featuresList)
			if (entry != null && entry.getFeatureName() != null)
				if (featureName.equals(entry.getFeatureName()))
					return true;
		return false;
	}

	/**
	 * really necessary?
	 */
	protected void checkForDuplicateFeatureNames() {
		Set<String> featureNames = new HashSet<String>();
		String errorString = "";

		for (F entry : featuresList)
			if (entry != null && entry.getFeatureName() != null)
				if (featureNames.contains(entry.getFeatureName()))
					errorString = (errorString + entry.getFeatureName() + ", ");
				else
					featureNames.add(entry.getFeatureName());

		if (!errorString.equals(""))
			throw new IllegalArgumentException(
					"FeatureVector - duplicate Features: " + errorString.substring(0, errorString.length() - 2));
	}

	private boolean checkFeatureNameConsistency() {
		if (featuresList != null) {
			if (featuresList.size() != getFeaturesMap().size())
				throw new IllegalArgumentException("FeatureVector: name conflict, features with identical name?");
		}

		return true;
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
	public int getFeatureIndex(String featureName) {
		for (int i = 0; i < featuresList.size(); i++)
			if (featuresList.get(i).getFeatureName().equals(featureName))
				return i;

		return -1;
	}

	@Override
	public F getFeature(String featureName) {
		if (getFeaturesMap() != null)
			return getFeaturesMap().get(featureName);
		return null;
	}

	@Override
	/**
	 * Expensive variant of adding a feature into the internal data representations.
	 */
	public void addFeature(F feature) {
		if (feature == null)
			return;

		if (featuresList == null)
			featuresList = new ArrayList<>();
		if (featuresMap == null)
			featuresMap = new HashMap<>();

		if (!featuresList.contains(feature))
			for (int i = 0; i < featuresList.size(); i++)
				if (featuresList.get(i).getFeatureName().equals(feature.getFeatureName())) {
					featuresList.set(i, feature);
					featuresMap.put(feature.getFeatureName(), feature);
					return;
				}

		featuresList.add(feature);
		featuresMap.put(feature.getFeatureName(), feature);
	}

	@Override
	/**
	 * Expensive variant of adding a feature into the internal data representations.
	 */
	public void addFeature(int index, F feature) {
		if (feature == null)
			return;

		if (featuresList == null)
			featuresList = new ArrayList<>();
		if (featuresMap == null)
			featuresMap = new HashMap<>();

		if (!featuresList.contains(feature))
			for (int i = 0; i < featuresList.size(); i++)
				if (featuresList.get(i).getFeatureName().equals(feature.getFeatureName())) {
					featuresList.remove(i--);
				}

		featuresList.add(index, feature);
		featuresMap.put(feature.getFeatureName(), feature);
	}

	/**
	 * Sets a feature at a given index. Does not handle index exceptions.
	 * 
	 * @param index
	 * @param feature
	 * @return the element previously at the specified position, or null.
	 */
	public F setFeature(int index, F feature) {
		F featureToBeReplaced = null;

		if (featuresList != null)
			featureToBeReplaced = featuresList.set(index, feature);

		if (featuresMap != null) {
			featuresMap.remove(featureToBeReplaced.getFeatureName());
			featuresMap.put(feature.getFeatureName(), feature);
		}

		return featureToBeReplaced;
	}

	public F removeFeature(String featureName) {
		F feature = featuresMap.remove(featureName);

		if (featuresList != null)
			if (feature != null) {
				featuresList.remove(feature);
				return feature;
			} else
				for (F f : featuresList)
					if (f.getFeatureName().equals(featureName)) {
						featuresList.remove(f);
						return f;
					}

		return feature;
	}

	/**
	 * removes a feature at a given index position.
	 * 
	 * @param index
	 * @return
	 */
	public F removeFeature(int index) {
		if (featuresList != null)
			if (featuresList.size() > index) {
				F removed = featuresList.remove(index);
				featuresMap.remove(removed.getFeatureName());
				return removed;
			}
		return null;
	}

	@Override
	public List<F> getVectorRepresentation() {
		return Collections.unmodifiableList(featuresList);
	}

	/**
	 * Features always points to the feature vector columns. In contrast, keySet()
	 * points to the attributes in the metadata.
	 */
	@Override
	public Set<String> getFeatureKeySet() {
		if (getFeaturesMap() == null)
			return null;

		return getFeaturesMap().keySet();
	}

	@Override
	public int sizeOfFeatures() {
		return featuresList.size();
	}

	public int getDimensions() {
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
	public String toString() {
		String n = this.getName();
		if (n == null)
			n = this.getClass().getSimpleName();

		n += (", " + this.getID() + ", dim: " + getDimensions() + "\t");

		return n;
	}

	protected Map<String, F> getFeaturesMap() {
		if (featuresMap == null)
			createFeatureNamesMap();

		return featuresMap;
	}

	/**
	 * retrieves all features of a given FeatureType. Example: all
	 * MixedDataFeatures.
	 * 
	 * @param featureType
	 * @return
	 */
	public List<Feature<?>> getFeaturesByType(FeatureType featureType) {
		List<Feature<?>> list = new ArrayList<>();

		for (Feature<?> feature : featuresList)
			if (feature != null)
				if (feature.getFeatureType().equals(featureType))
					list.add(feature);

		return list;
	}

}
