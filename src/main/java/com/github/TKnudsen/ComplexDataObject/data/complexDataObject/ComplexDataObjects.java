package com.github.TKnudsen.ComplexDataObject.data.complexDataObject;

import com.github.TKnudsen.ComplexDataObject.data.keyValueObject.KeyValueObjects;

public class ComplexDataObjects {

	/**
	 * True if two KeyValueObjects have identical attributes and attribute values.
	 * ID, name and description are ignored.
	 * 
	 * @param v
	 * @return
	 */
	public static boolean equalValues(ComplexDataObject u, ComplexDataObject v) {
		return KeyValueObjects.equalValues(u, v);
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
		ComplexDataObject mergedObject = new ComplexDataObject();

		for (ComplexDataObject object : objects) {
			for (String attribute : object.keySet()) {
				if (mergedObject.getAttribute(attribute) != null)
					System.out.println("overwriting attribute " + attribute + "(" + object.getAttribute(attribute) + "->" + mergedObject.getAttribute(attribute) + ")");
				mergedObject.add(attribute, object.getAttribute(attribute));
			}

			if (object.getName() != null)
				mergedObject.setName(object.getName());
			
			if (object.getDescription() != null)
				mergedObject.setDescription(object.getDescription());
		}
		return mergedObject;
	}
}
