package com.github.TKnudsen.ComplexDataObject.data.keyValueObject;

import java.util.SortedMap;

public class KeyValueObjects {
	/**
	 * True if two KeyValueObjects have identical attributes and attribute values.
	 * ID, name and description are ignored.
	 * 
	 * @param v
	 * @return
	 */
	public static <V> boolean equalValues(KeyValueObject<V> u, KeyValueObject<V> v) {
		if (v == null) {
			return false;
		}
		SortedMap<String, V> uAttributes = u.attributes;
		SortedMap<String, V> vAttributes = v.attributes;

		if (!uAttributes.equals(vAttributes))
			return false;

		for (String attribute : uAttributes.keySet())
			if (!u.getAttribute(attribute).equals(v.getAttribute(attribute)))
				return false;

		return true;
	}
}
