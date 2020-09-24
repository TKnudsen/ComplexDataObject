package com.github.TKnudsen.ComplexDataObject.data.dataFactory;

import java.io.IOException;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.examples.TitanicParser;

/**
 * <p>
 * Title: Datasets provides Lists of ComplexDataObject instances for some given
 * data sets.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2020
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class DataSets {

	public static List<ComplexDataObject> titanicDataSet() {
		List<ComplexDataObject> titanicData = null;

		TitanicParser p = new TitanicParser("", true);
		try {
			System.out.println("Working Directory = " + System.getProperty("user.dir"));
			titanicData = p.parse("data/titanic_extended.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return titanicData;
	}
}
