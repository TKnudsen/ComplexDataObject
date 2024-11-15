package com.github.TKnudsen.ComplexDataObject.data.keyValueObject;

/**
 * 
 * @deprecated seems like no support functionality is needed
 *
 */
public class KeyValueObjects {
	/**
	 * True if two KeyValueObjects have identical keys and attribute values. ID,
	 * name and description are ignored.
	 * 
	 * @deprecated use one of the two instances and ask for u.equalValues(v)
	 *
	 * @param u
	 * @param v
	 * @return
	 */
	public static <V> boolean equalValues(KeyValueObject u, KeyValueObject v) {
		if (u == null)
			return false;

		return u.equalValues(v);
	}
}
