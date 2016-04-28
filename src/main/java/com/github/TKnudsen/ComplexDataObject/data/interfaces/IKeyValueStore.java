package com.github.TKnudsen.ComplexDataObject.data.interfaces;

import java.util.Map;

/**
 * <p>
 * Title: IKeyValueStore
 * </p>
 * 
 * <p>
 * Description: interface for objects containing key-value pairs.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public interface IKeyValueStore {

	public int size();

	public void add(String attribute, Object value);

	public Object get(String attribute);

	public Class<?> getType(String attribute);

	public Map<String, Class<?>> getTypes();

	public boolean remove(String attribute);

	public int hashCode();

	public boolean equals(Object obj);
}
