package com.github.TKnudsen.ComplexDataObject.model.io.json.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeature;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.io.json.JSONLoader;
import com.github.TKnudsen.ComplexDataObject.model.io.json.JSONWriter;
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

		// create ComplexDataObject(s)
		List<ComplexDataObject> titanicData = null;
		TitanicParser p = new TitanicParser("", true);
		try {
			titanicData = p.parse("data/titanic_extended.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		ComplexDataObject complexDataObject = titanicData.get(0);
		System.out.println(complexDataObject);

		// write ComplexDataObject
		String file = "titanic0.json";
		String JSONString = JSONWriter.writeToString(complexDataObject);
		JSONWriter.writeToFile(complexDataObject, file);
		System.out.println(JSONString);

		// load ComplexDataObject
		ComplexDataObject loadConfigsFromString = JSONLoader.loadFromString(JSONString);
		System.out.println(loadConfigsFromString);
		ComplexDataObject loadConfigsFromFile = JSONLoader.loadFromFile(file);
		System.out.println(loadConfigsFromFile);

		// side track: save ComplexDataContainer
		ComplexDataContainer container = new ComplexDataContainer(titanicData);
		System.out.println(container);
		JSONWriter.writeToFile(container, file);
		container = JSONLoader.loadComplexDataContainerFromFile(file);
		System.out.println(container);

		List<NumericalFeature> features = new ArrayList<>();

		features.add(new NumericalFeature("1", (double) 1));
		features.add(new NumericalFeature("2", (double) 2));
		features.add(new NumericalFeature("3", (double) 3));
		features.add(new NumericalFeature("4", (double) 4));

		NumericalFeatureVector nfv = new NumericalFeatureVector(features);

		nfv.add("testA", "a");
		nfv.add("testB", "b");
		nfv.add("testC", "c");

		System.out.println(nfv);

		// write
		String fileNFV = "testNFV.json";
		String JSONStringNFV = JSONWriter.writeToString(nfv);
		JSONWriter.writeToFile(nfv, fileNFV);
		System.out.println(JSONStringNFV);

		// load
		NumericalFeatureVector loadConfigsFromStringNFV = JSONLoader
				.loadNumericalFeatureVectorFromString(JSONStringNFV);
		System.out.println(loadConfigsFromStringNFV);
		NumericalFeatureVector loadConfigsFromFileNFV = JSONLoader.loadNumericalFeatureVectorFromFile(fileNFV);
		System.out.println(loadConfigsFromFileNFV);

	}
}
