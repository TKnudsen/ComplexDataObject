package com.github.TKnudsen.ComplexDataObject.data.interfaces;

import java.util.Map;
import java.util.Set;

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
 * @version 1.07
 */
public interface IKeyValueProvider<V extends Object> extends IDObject {

	public void add(String attribute, V value);

	public V get(String attribute);

	public Class<?> getType(String attribute);

	public Set<String> keySet();

	public Map<String, Class<?>> getTypes();

	public V remove(String attribute);

	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
