package com.github.TKnudsen.ComplexDataObject.data.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IFeatureVectorObject;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IKeyValueProvider;

/**
 * <p>
 * Stores and manages collections of Feature Vectors. A FeatureSchema manages
 * the features of the collection.
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2016-2020
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.05
 */
public class FeatureVectorContainer<FV extends IFeatureVectorObject<?, ?>> implements Iterable<FV> {

	private Map<Long, FV> featureVectorMap = new HashMap<Long, FV>();

	/**
	 * created to allow values() operation without iterator to be more thread-safe.
	 * 
	 * Important: always needs to be kept in sync with the featureVectorMap.
	 */
	private ArrayList<FV> values = new ArrayList<FV>();

	protected Map<String, Map<Long, Object>> featureValues = new HashMap<String, Map<Long, Object>>();

	protected FeatureSchema featureSchema;

	public FeatureVectorContainer(FeatureSchema featureSchema) {
		this.featureSchema = featureSchema;
	}

	public FeatureVectorContainer(Map<Long, FV> featureVectorMap) {
		this.featureVectorMap = featureVectorMap;
		featureSchema = new FeatureSchema();
		for (Long ID : featureVectorMap.keySet())
			extendDataSchema(featureVectorMap.get(ID));
	}

	public FeatureVectorContainer(Iterable<FV> objects) {
		featureSchema = new FeatureSchema();
		for (FV object : objects) {
			featureVectorMap.put(object.getID(), object);
			values.add(object);
			extendDataSchema(object);
		}
	}

	private void extendDataSchema(FV object) {
		for (String feature : object.getFeatureKeySet())
			if (!featureSchema.contains(feature))
				if (feature == null || object.getFeature(feature) == null
						|| object.getFeature(feature).getFeatureValue() == null)
					throw new IllegalArgumentException("FeatureContainer: feature in object was associated with null");
				else
					featureSchema.add(feature, object.getFeature(feature).getFeatureValue().getClass(),
							object.getFeature(feature).getFeatureType());

		// TODO maybe add type information to Feature class itself (currently an
		// enum)
	}

	/**
	 * Introduces or updates a new feature.
	 *
	 * @param feature
	 * @return the data schema instance for call-chaining.
	 */
	public FeatureSchema addFeature(Feature<?> feature) {
		featureValues = new HashMap<String, Map<Long, Object>>();

		featureSchema.add(feature.getFeatureName(), feature.getFeatureValue().getClass(), feature.getFeatureType());

		Iterator<FV> objectIterator = iterator();
		while (objectIterator.hasNext()) {
			FV next = objectIterator.next();
			if (next.getFeature(feature.getFeatureName()) == null)
				next.addFeature(feature.getFeatureName(), null, feature.getFeatureType());
		}

		return featureSchema;
	}

	/**
	 * Introduces or updates a feature.
	 *
	 * @param featureName  the feature name
	 * @return the data schema instance for call-chaining.
	 */
	public FeatureSchema addFeature(String featureName, Class<Feature<?>> featureClass, FeatureType featureType) {
		featureValues = new HashMap<String, Map<Long, Object>>();

		featureSchema.add(featureName, featureClass, featureType);

		Iterator<FV> objectIterator = iterator();
		while (objectIterator.hasNext()) {
			FV next = objectIterator.next();
			if (next.getFeature(featureName) == null)
				next.addFeature(featureName, null, featureType);
		}

		return featureSchema;
	}

	/**
	 * Remove functionality. For test purposes. Maybe this functionality will be
	 * removed sometime.
	 *
	 * @param featureVector
	 * @return
	 */
	public boolean remove(FV featureVector) {
		if (featureVector == null)
			return false;

		long id = featureVector.getID();
		if (!featureVectorMap.containsKey(id))
			return false;

		for (String featureName : featureValues.keySet()) {
			if (featureValues.get(featureName) != null)
				featureValues.get(featureName).remove(id);
		}

		featureVectorMap.remove(id);
		values.remove(featureVector);

		return true;
	}

	/**
	 * Removes a feature from the container and the set of objects.
	 *
	 * @param featureName the feature name.
	 * @return the data schema instance for call-chaining.
	 */
	public FeatureSchema remove(String featureName) {
		Iterator<FV> iterator = iterator();
		while (iterator.hasNext()) {
			FV o = iterator.next();
			o.removeFeature(featureName);
		}

		return featureSchema.remove(featureName);
	}

	@Override
	public Iterator<FV> iterator() {
		return featureVectorMap.values().iterator();
	}

	/**
	 * thread-safe as no iterator of the featureVectorMap is needed.
	 * 
	 * @return
	 */
	public Collection<FV> values() {
		return new CopyOnWriteArrayList<FV>(values);
	}

	public Boolean isNumeric(String featureName) {
		if (Number.class.isAssignableFrom(featureSchema.getType(featureName)))
			return true;
		return false;
	}

	public Boolean isBoolean(String feature) {
		if (Boolean.class.isAssignableFrom(featureSchema.getType(feature)))
			return true;
		return false;
	}

	public Collection<String> getFeatureNames() {
		return featureSchema.getFeatureNames();
	}

	public Map<Long, Object> getFeatureValues(String featureName) {
		if (featureValues.get(featureName) == null) {
			calculateEntities(featureName);
		}
		return featureValues.get(featureName);
	}

	public boolean contains(FV featureVector) {
		if (featureVectorMap.containsKey(featureVector.getID()))
			return true;
		return false;
	}

	private void calculateEntities(String featureName) {
		Map<Long, Object> ent = new HashMap<Long, Object>();

		Iterator<FV> iterator = iterator();
		while (iterator.hasNext()) {
			FV o = iterator.next();
			if (o instanceof IKeyValueProvider)
				ent.put(o.getID(), o.getFeature(featureName));
		}

		this.featureValues.put(featureName, ent);
	}

	@Override
	public String toString() {
		if (featureSchema == null)
			return super.toString();
		return featureSchema.toString();
	}

	public int size() {
		if (featureVectorMap == null)
			return 0;
		return featureVectorMap.size();
	}
}
