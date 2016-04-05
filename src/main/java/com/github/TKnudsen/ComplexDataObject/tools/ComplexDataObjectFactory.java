package com.github.TKnudsen.ComplexDataObject.tools;

import com.github.TKnudsen.ComplexDataObject.data.ComplexDataObject;

public class ComplexDataObjectFactory {

	public static ComplexDataObject createObject(String attribute1, Object value1, String attribute2, Object value2) {
		ComplexDataObject a = new ComplexDataObject();
		a.add(attribute1, value1);
		a.add(attribute2, value2);
		return a;

	}
}
