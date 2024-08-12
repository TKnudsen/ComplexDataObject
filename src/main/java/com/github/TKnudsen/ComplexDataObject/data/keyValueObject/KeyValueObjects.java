package com.github.TKnudsen.ComplexDataObject.data.keyValueObject;

public class KeyValueObjects {
	/**
	 * True if two KeyValueObjects have identical keys and attribute values. ID,
	 * name and description are ignored.
	 *
	 * @param u
	 * @param v
	 * @return
	 */
	public static <V> boolean equalValues(KeyValueObject<V> u, KeyValueObject<V> v) {
		if (v == null)
			return false;

		if (!u.attributes.keySet().equals(v.attributes.keySet()))
			return false;

		for (String key : u.attributes.keySet())
			if (!u.getAttribute(key).equals(v.getAttribute(key)))
				return false;

		return true;
	}
}
