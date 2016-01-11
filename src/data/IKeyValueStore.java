package data;

import java.util.Map;

/**
 * <p>
 * Title: IKeyValueStore
 * </p>
 * 
 * <p>
 * Description: The interface for a ComplexDataObject. Currently it is not yet
 * forseeable whether other implementations of IKeyValueStore will follow.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public interface IKeyValueStore {

	public Long getID();

	public int size();

	public String getName();

	public String getDescription();

	public void add(String attribute, Object value);

	public Object get(String attribute);

	public Class<?> getType(String attribute);

	public Map<String, Class<?>> getTypes();

	public boolean remove(String attribute);

	public String toString();
}
