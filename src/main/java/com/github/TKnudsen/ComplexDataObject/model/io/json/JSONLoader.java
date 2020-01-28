package com.github.TKnudsen.ComplexDataObject.model.io.json;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.TKnudsen.ComplexDataObject.data.DataSchema;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.AttributeScoringFunction;

/**
 * <p>
 * Loads ComplexDataObjects from JSON. Brings its own ObjectMapper for speedup
 * purposes, but can also be run with an external ObjectMapper parameter.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017-2020
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class JSONLoader {

	private static ObjectMapper mapper = ObjectMapperFactory.getComplexDataObjectObjectMapper();

	public static ComplexDataObject loadFromString(String json) {
		ComplexDataObject complexDataObject;
		try {
			complexDataObject = mapper.readValue(json, ComplexDataObject.class);
			return complexDataObject;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static ComplexDataObject loadFromFile(String file) {
		ComplexDataObject complexDataObject;
		try {
			complexDataObject = mapper.readValue(new File(file), ComplexDataObject.class);
			return complexDataObject;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static ComplexDataObject loadFromFile(String file, ObjectMapper mapper) {
		ComplexDataObject complexDataObject;
		try {
			complexDataObject = mapper.readValue(new File(file), ComplexDataObject.class);
			return complexDataObject;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static DataSchema loadDataSchemaFromString(String json) {

		DataSchema dataSchema;
		try {
			dataSchema = mapper.readValue(json, DataSchema.class);
			return dataSchema;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static DataSchema loadDataSchemaFromFile(String file) {

		DataSchema dataSchema;
		try {
			dataSchema = mapper.readValue(new File(file), DataSchema.class);
			return dataSchema;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static NumericalFeatureVector loadNumericalFeatureVectorFromString(String json) {
		NumericalFeatureVector numericalFeatureVector;
		try {
			numericalFeatureVector = mapper.readValue(json, NumericalFeatureVector.class);
			return numericalFeatureVector;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static NumericalFeatureVector loadNumericalFeatureVectorFromFile(String file) {
		NumericalFeatureVector numericalFeatureVector;
		try {
			numericalFeatureVector = mapper.readValue(new File(file), NumericalFeatureVector.class);
			return numericalFeatureVector;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static AttributeScoringFunction<?> loadAttributeScoringFunctionFromFile(String file) {

		AttributeScoringFunction<?> function;
		try {
			function = mapper.readValue(new File(file), AttributeScoringFunction.class);
			return function;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
