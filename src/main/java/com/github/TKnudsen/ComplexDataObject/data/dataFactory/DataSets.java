package com.github.TKnudsen.ComplexDataObject.data.dataFactory;

import java.io.IOException;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.examples.TitanicParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.ReflectionTools;

/**
 * <p>
 * provides ComplexDataObjects for data sets.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2021
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
 */
public class DataSets {

	public static List<ComplexDataObject> titanicDataSet() {
		List<ComplexDataObject> titanicData = null;

		TitanicParser p = new TitanicParser("", true);
		try {
			String dataLocation = ReflectionTools.classLocation(ComplexDataObject.class);
			dataLocation = dataLocation.substring(0, dataLocation.indexOf("/target"));
			dataLocation += "/data/titanic_extended.txt";
			titanicData = p.parse(dataLocation);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return titanicData;
	}
}
