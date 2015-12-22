package data;

import java.util.Map;

public interface IDataObject {

	public long getID();

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
