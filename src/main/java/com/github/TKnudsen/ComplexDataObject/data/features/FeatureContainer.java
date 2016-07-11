package com.github.TKnudsen.ComplexDataObject.data.features;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IKeyValueProvider;

/**
 * <p>
 * Title: FeatureContainer
 * </p>
 * 
 * <p>
 * Description: Stores and manages collections of Feature Vectors. A
 * FeatureSchema manages the keys/attributes of the collection.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */
public class FeatureContainer<O extends Object, F extends Feature<O>, T extends AbstractFeatureDataObject<O, F>> implements Iterable<T> {

	private Map<Long, T> featureVectorMap = new HashMap<Long, T>();

	protected Map<String, Map<Long, Object>> featureValues = new HashMap<String, Map<Long, Object>>();

	protected FeatureSchema featureSchema;

	public FeatureContainer(FeatureSchema featureSchema) {
		this.featureSchema = featureSchema;
	}

	public FeatureContainer(Map<Long, T> featureVectorMap) {
		this.featureVectorMap = featureVectorMap;
		featureSchema = new FeatureSchema();
		for (Long ID : featureVectorMap.keySet())
			extendDataSchema(featureVectorMap.get(ID));
	}

	public FeatureContainer(Iterable<T> objects) {
		featureSchema = new FeatureSchema();
		for (T object : objects) {
			featureVectorMap.put(object.getID(), object);
			extendDataSchema(object);
		}
	}

	private void extendDataSchema(T object) {
		for (String feature : object.getFeatureKeySet())
			if (!featureSchema.contains(feature))
				featureSchema.add(feature, object.getFeature(feature).getClass(), object.getFeature(feature).getFeatureType());
	}

	/**
	 * Introduces or updates a new attribute.
	 * 
	 * @param featureName
	 *            the attribute name
	 * @param type
	 *            the expected data type.
	 * @param defaultValue
	 *            the default value in case the attribute is missing from a data
	 *            object.
	 * @return the data schema instance for call-chaining.
	 */
	public FeatureSchema addFeature(F feature) {
		featureValues = new HashMap<String, Map<Long, Object>>();

		featureSchema.add(feature.getFeatureName(), feature.getFeatureValue().getClass(), feature.getFeatureType());

		Iterator<T> objectIterator = iterator();
		while (objectIterator.hasNext()) {
			T next = objectIterator.next();
			if (next.getFeature(feature.getFeatureName()) == null)
				next.addFeature(feature.getFeatureName(), null);
		}

		return featureSchema;
	}

	/**
	 * Introduces or updates a new attribute.
	 * 
	 * @param featureName
	 *            the attribute name
	 * @param type
	 *            the expected data type.
	 * @param defaultValue
	 *            the default value in case the attribute is missing from a data
	 *            object.
	 * @return the data schema instance for call-chaining.
	 */
	public FeatureSchema addFeature(String featureName, Class<F> featureClass, FeatureType featureType) {
		featureValues = new HashMap<String, Map<Long, Object>>();

		featureSchema.add(featureName, featureClass, featureType);

		Iterator<T> objectIterator = iterator();
		while (objectIterator.hasNext()) {
			T next = objectIterator.next();
			if (next.getFeature(featureName) == null)
				next.addFeature(featureName, null);
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
	public boolean remove(T featureVector) {
		if (featureVector == null)
			return false;

		long id = featureVector.getID();
		if (!featureVectorMap.containsKey(id))
			return false;

		for (String attribute : featureValues.keySet()) {
			if (featureValues.get(attribute) != null)
				featureValues.get(attribute).remove(id);
		}

		featureVectorMap.remove(id);

		return true;
	}

	/**
	 * Removes an attribute from the container and the set of objects.
	 * 
	 * @param featureName
	 *            the attribute name.
	 * @return the data schema instance for call-chaining.
	 */
	public FeatureSchema remove(String featureName) {
		Iterator<T> iterator = iterator();
		while (iterator.hasNext()) {
			T o = iterator.next();
			o.remove(featureName);
		}

		return featureSchema.remove(featureName);
	}

	@Override
	public Iterator<T> iterator() {
		return featureVectorMap.values().iterator();
	}

	public Boolean isNumeric(String featureName) {
		if (Number.class.isAssignableFrom(featureSchema.getType(featureName)))
			return true;
		return false;
	}

	public Boolean isBoolean(String attribute) {
		if (Boolean.class.isAssignableFrom(featureSchema.getType(attribute)))
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

	public boolean contains(T featureVector) {
		if (featureVectorMap.containsKey(featureVector.getID()))
			return true;
		return false;
	}

	private void calculateEntities(String featureName) {
		Map<Long, Object> ent = new HashMap<Long, Object>();

		Iterator<T> iterator = iterator();
		while (iterator.hasNext()) {
			T o = iterator.next();
			if (o instanceof IKeyValueProvider)
				ent.put(o.getID(), o.get(featureName));
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
