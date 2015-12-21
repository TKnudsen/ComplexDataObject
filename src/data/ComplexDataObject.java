package data;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * ComplexDataObject is a key-value store that can be used to model complex
 * real-world objects.
 * 
 * @author Juergen Bernard
 *
 */
public class ComplexDataObject implements IDataObject, Iterable<String> {

	protected Long ID = -1L;
	protected String name;
	protected String description;

	protected SortedMap<String, Object> attributes = new TreeMap<String, Object>();

	public ComplexDataObject() {
		this.ID = getRandomLong();
	}

	public ComplexDataObject(Long ID) {
		this.ID = ID;
	}

	public ComplexDataObject(String name, String description) {
		this.ID = getRandomLong();
		this.name = name;
		this.description = description;
	}

	public ComplexDataObject(Long ID, String name, String description) {
		this.ID = ID;
		this.name = name;
		this.description = description;
	}

	/**
	 * Little Helper for the Generation of a unique Identifier.
	 * 
	 * @return unique ID
	 */
	private Long getRandomLong() {
		return (long) (Math.random() * (Long.MAX_VALUE - 1));
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<String> keySet() {
		return attributes.keySet();
	}

	@Override
	public Iterator<String> iterator() {
		return attributes.keySet().iterator();
	}

	@Override
	public void remove(String attribute) {
		if (attributes.containsKey(attribute))
			attributes.remove(attribute);
	}

	@Override
	public String toString() {
		String output = "";
		for (String key : attributes.keySet())
			output += (toLineString(key) + "\n");
		return output;
	}

	private String toLineString(String attribute) {
		if (attributes.get(attribute) == null)
			return "";

		String output = "";
		output += ("Attribute: " + attribute + "\t" + "Type: " + attributes.get(attribute).getClass() + "\t" + "Value: " + attributes.get(attribute));
		return output;
	}

	public String toStringInLine() {
		String output = "";
		for (String key : attributes.keySet())
			output += (key + attributes.get(key).toString() + "/t");
		return output;
	}

	@Override
	public Long getID() {
		return ID;
	}

	@Override
	public int Size() {
		if (attributes == null)
			return 0;
		return attributes.size();
	}

	@Override
	public String getName() {
		if (name == null)
			return toString();
		return name;
	}

	@Override
	public String getDescription() {
		if (description == null)
			return toString();
		return description;
	}

	@Override
	public void add(String attribute, Object value) {
		attributes.put(attribute, value);
	}

	@Override
	public Object get(String attribute) {
		return attributes.get(attribute);
	}

	@Override
	public Class<?> getType(String attribute) {
		if (attributes.get(attribute) != null)
			return attributes.get(attribute).getClass();
		return null;
	}

	@Override
	public Map<String, Class<?>> getTypes() {
		// TODO Auto-generated method stub
		return null;
	}

}
