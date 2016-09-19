package com.github.TKnudsen.ComplexDataObject.model.tools;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

/**
 * <p>
 * Title: ComplexDataObjectTools
 * </p>
 *
 * <p>
 * Description:
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.01
 */
public class ComplexDataObjectTools {

	public static ComplexDataObject clone(ComplexDataObject object) {
		if (object == null)
			return null;

		ComplexDataObject newObject = new ComplexDataObject(object.getName(), object.getDescription());
		for (String string : object) {
			newObject.add(string, object.get(string));
		}

		return newObject;
	}
}
