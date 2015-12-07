package data;

import java.util.Map;

public interface IDataObject {

	public Long getID();

	public int Size();

	public String getName();

	public String getDescription();

	public void add(String attribute, Object value);

	public Object get(String attribute);

	public Class<?> getType(String attribute);

	public Map<String, Class<?>> getTypes();

	public void remove(String attribute);

	public String toString();
}
