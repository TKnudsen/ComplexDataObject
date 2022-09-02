package com.github.TKnudsen.ComplexDataObject.data.dataFactory;

import java.io.IOException;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.examples.TitanicParser;
import com.github.TKnudsen.ComplexDataObject.model.tools.ReflectionTools;

/**
 * <p>
 * provides ComplexDataObjects for data sets. Requires that ComplexDataObject is
 * checked out.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2022
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.05
 */
public class DataSets {

	public static List<ComplexDataObject> titanicDataSet() {
		List<ComplexDataObject> titanicData = null;

		TitanicParser p = new TitanicParser("", true);
		try {
			String dataLocation = ReflectionTools.classLocation(ComplexDataObject.class);
			try {
				dataLocation = dataLocation.substring(0, dataLocation.indexOf("/target"));
			} catch (Exception e) {
				if (dataLocation.indexOf(".m2") != -1) {
					dataLocation = dataLocation.substring(dataLocation.indexOf(".m2"));
					dataLocation += "ComplexDataObject";
				}
			}

			dataLocation += "/data/titanic_extended.txt";
			dataLocation = dataLocation.replace("%20", " ");
			titanicData = p.parse(dataLocation);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return titanicData;
	}
}
