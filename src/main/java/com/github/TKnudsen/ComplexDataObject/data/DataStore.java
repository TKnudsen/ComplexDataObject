package com.github.TKnudsen.ComplexDataObject.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IKeyValueProvider;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.IMasterProvider;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.ITextDescription;

/**
 * <p>
 * Title: DataStore
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2012-2016
 * </p>
 * 
 * @author Juergen Bernard
 */
public class DataStore<T extends IDObject> implements IDObject, ITextDescription, Iterable<T> {

	private IDObject master = null;
	private long ID;
	private String name;
	protected int hash = -1;

	/**
	 * Map<Long, T> dataMap: quick access by objectID
	 */
	private transient Map<Long, T> dataMap = new HashMap<Long, T>();

	/**
	 * List<T> dataArrayList: access the data via an List. Always consistent
	 * with the dataMaps (!)
	 */
	private transient List<T> dataArrayList = new ArrayList<>();

	/**
	 * 
	 */
	private Set<String> attributeKeys = new TreeSet<>();

	public DataStore(List<T> data) {
		this.ID = getRandomLong();
		if (data != null && data.size() > 0)
			this.name = "Data Store for Objects of Type: " + data.get(0).getClass();
		else
			return;

		// initialize data
		for (T dataObject : data) {
			dataArrayList.add(dataObject);
			dataMap.put(dataObject.getID(), dataObject);
		}

		initializeAttributeKeys();
	}

	public DataStore(Map<Long, T> data) {
		this.ID = getRandomLong();
		if (data != null && data.size() > 0)
			this.name = "Data Store for Objects of Type: " + data.values().iterator().next().getClass();
		else
			return;

		// initialize data
		for (Long l : data.keySet()) {
			dataMap.put(l, data.get(l));
			dataArrayList.add(data.get(l));
		}

		initializeAttributeKeys();
	}

	/**
	 * Adds a List of objects to the data store
	 * 
	 * @param objects
	 */
	public void add(List<T> objects) {
		for (T t : objects)
			add(t);
	}

	/**
	 * Adds an object to the data store.
	 * 
	 * @param dobj
	 * @return
	 */
	public boolean add(T dataObject) {
		if (dataMap.containsKey(dataObject.getID()))
			return false;

		dataMap.put(dataObject.getID(), dataObject);
		if (!dataArrayList.contains(dataObject))
			dataArrayList.add(dataObject);

		addAttributeKeys(dataObject);

		return true;
	}

	@Override
	public int hashCode() {
		if (hash == -1) {

			hash = 19;

			for (Long i : dataMap.keySet())
				hash += 31 * hash + (int) dataMap.get(i).hashCode();
		}
		return hash;
	}

	/**
	 * Get all data objects that have an equal name. Compares the name to the
	 * getName method when an object of the store is an instance of
	 * ITextDescription, else the toString value.
	 * 
	 * @param name
	 * @return
	 */
	public List<T> getDataByName(String name) {
		List<T> ret = new ArrayList<T>();

		for (T dataObject : dataArrayList) {
			if (dataObject instanceof ITextDescription)
				if (((ITextDescription) dataObject).getName().equals(name))
					ret.add(dataObject);
				else if (dataObject.toString().equals(name))
					ret.add(dataObject);
		}

		return ret;
	}

	@Override
	public long getID() {
		return ID;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<Long, T> getDataMap() {
		return dataMap;
	}

	public List<T> getDataList() {
		return dataArrayList;
	}

	public List<T> getDataByMaster(IDObject master) {
		List<T> data = new ArrayList<>();
		for (T e : dataArrayList)
			if (e instanceof IMasterProvider)
				if (((IMasterProvider) e).getMaster() != null)
					if (((IMasterProvider) e).getMaster().equals(master))
						data.add(e);
		return data;
	}

	/**
	 * Adds Attributes to the attributeKeys if a given Object has new
	 * attributes.
	 */
	private void addAttributeKeys(T dataObject) {
		if (attributeKeys == null)
			initializeAttributeKeys();

		if (dataObject instanceof IKeyValueProvider) {
			IKeyValueProvider keyValueStore = (IKeyValueProvider) dataObject;
			Map<String, Class<?>> types = keyValueStore.getTypes();
			for (String s : types.keySet())
				attributeKeys.add(s);
		}
	}

	/**
	 * Initializes the attribute keys by re-initializing the data structure. Use
	 * validateAttributeKeys() when adding new DataObjects.
	 */
	private void initializeAttributeKeys() {
		if (attributeKeys == null)
			attributeKeys = new TreeSet<>();
		for (T dataObject : dataArrayList)
			if (dataObject instanceof IKeyValueProvider) {
				IKeyValueProvider keyValueStore = (IKeyValueProvider) dataObject;
				Map<String, Class<?>> types = keyValueStore.getTypes();
				for (String s : types.keySet())
					attributeKeys.add(s);
			}
	}

	public T getDataByObjectID(Long objectID) {
		return dataMap.get(objectID);
	}

	public Set<String> getAttributeKeys() {
		return attributeKeys;
	}

	public boolean contains(T t) {
		if (dataMap.containsKey(t.getID()))
			return true;
		return false;
	}

	public int size() {
		return dataMap.size();
	}

	public IDObject getMaster() {
		return master;
	}

	@Override
	public String getDescription() {
		return getName();
	}

	@Override
	public Iterator<T> iterator() {
		return dataMap.values().iterator();
	}

	/**
	 * Little helper for the generation of a unique identifier.
	 * 
	 * @return unique ID
	 */
	private long getRandomLong() {
		return UUID.randomUUID().getMostSignificantBits();
	}
}
