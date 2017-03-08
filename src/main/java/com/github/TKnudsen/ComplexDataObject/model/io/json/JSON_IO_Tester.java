package com.github.TKnudsen.ComplexDataObject.model.io.json;

import java.io.IOException;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.examples.TitanicParser;

/**
 * <p>
 * Title: JSONIOTester
 * </p>
 * 
 * <p>
 * Description: tests JSON output-inout.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */
public class JSON_IO_Tester {

	public static void main(String[] args) {

		// create complex data object(s)
		List<ComplexDataObject> titanicData = null;
		TitanicParser p = new TitanicParser("", true);
		try {
			titanicData = p.parse("data/titanic_extended.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		ComplexDataObject complexDataObject = titanicData.get(0);
		System.out.println(complexDataObject);

		// write
		String file = "titanic0.json";
		String JSONString = JSONWriter.writeToString(complexDataObject);
		JSONWriter.writeToFile(complexDataObject, file);
		System.out.println(JSONString);

		// load
		ComplexDataObject loadConfigsFromString = JSONLoader.loadFromString(JSONString);
		System.out.println(loadConfigsFromString);
		ComplexDataObject loadConfigsFromFile = JSONLoader.loadFromFile(file);
		System.out.println(loadConfigsFromFile);
	}
}
