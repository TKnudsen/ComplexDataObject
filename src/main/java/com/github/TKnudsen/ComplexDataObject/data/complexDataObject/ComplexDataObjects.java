package com.github.TKnudsen.ComplexDataObject.data.complexDataObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ComplexDataObjects {

	/**
	 * True if two KeyValueObjects have identical attributes and attribute values.
	 * ID, name and description are ignored.
	 *
	 * @deprecated use one of the two instances and ask for u.equalValues(v)
	 * @param u
	 * @param v
	 * @return
	 */
	public static boolean equalValues(ComplexDataObject u, ComplexDataObject v) {
		if (u == null)
			return false;

		return u.equalValues(v);
	}

	public static ComplexDataObject clone(ComplexDataObject object) {
		if (object == null)
			return null;

		ComplexDataObject newObject = new ComplexDataObject(object.getName(), object.getDescription());
		for (String string : object) {
			newObject.add(string, object.getAttribute(string));
		}

		return newObject;
	}

	/**
	 * Merges ComplexDataObjects. Conflicting attributes are defined by the last
	 * occurrence in the input data.
	 * 
	 * @param objects
	 * @return
	 */
	public static ComplexDataObject merge(Iterable<ComplexDataObject> objects) {
		return merge(objects, false);
	}

	/**
	 * Merges ComplexDataObjects. Conflicting attributes are defined by the last
	 * occurrence in the input data that is not null.
	 * 
	 * @param objects
	 * @param skipNullValues
	 * @return
	 */
	public static ComplexDataObject merge(Iterable<ComplexDataObject> objects, boolean skipNullValues) {
		ComplexDataObject mergedObject = new ComplexDataObject();

		for (ComplexDataObject object : objects) {
			for (String attribute : object.keySet()) {
				if (!mergedObject.containsAttribute(attribute))
					mergedObject.add(attribute, object.getAttribute(attribute));
				else if (object.getAttribute(attribute) != null)
					mergedObject.add(attribute, object.getAttribute(attribute));
			}

			if (object.getName() != null)
				mergedObject.setName(object.getName());

			if (object.getDescription() != null)
				mergedObject.setDescription(object.getDescription());
		}
		return mergedObject;
	}

	/**
	 * ignores name and description.
	 * 
	 * @param cdo
	 * @return
	 */
	public static Map<String, Object> keyValuePairs(ComplexDataObject cdo) {
		Map<String, Object> values = new HashMap<>();

		for (String attribute : cdo.getAttributes())
			values.put(attribute, cdo.getAttribute(attribute));

		return values;
	}

	/**
	 * returns all attributes for which the objects differ in values.
	 * 
	 * @param list
	 * @return
	 */
	public static Set<String> diff(List<ComplexDataObject> list) {
		SortedSet<String> diff = new TreeSet<>();

		if (list == null || list.isEmpty())
			return diff;

		Map<String, Object> values = null;
		for (ComplexDataObject cdo : list) {
			if (values == null)
				values = keyValuePairs(cdo);
			else {
				for (String key : cdo.getAttributes())
					if (!values.containsKey(key))
						diff.add(key);
					else if (values.get(key) == null && cdo.getAttribute(key) != null)
						diff.add(key);
					else if (values.get(key) != null && !values.get(key).equals(cdo.getAttribute(key)))
						diff.add(key);
			}
		}

		return diff;
	}

}
