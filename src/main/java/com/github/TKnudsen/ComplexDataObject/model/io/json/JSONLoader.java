package com.github.TKnudsen.ComplexDataObject.model.io.json;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
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
 * Copyright: Copyright (c) 2017-2024
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.04
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

	/**
	 * old load from file version that does not ask if an exception shall be caught
	 * internally.
	 * 
	 * @deprecated use the two-parameter version that asks if the exception shall be
	 *             caught internally.
	 * @param file
	 * @return
	 */
	public static ComplexDataObject loadFromFile(String file) {
		try {
			return loadFromFile(file, true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 
	 * @param file           the file location, including the file ending.
	 * @param catchException allows non-interrupted execution if the exception shall
	 *                       be caught in here no matter what. Recommended default:
	 *                       false
	 * @return
	 * @throws IOException
	 */
	public static ComplexDataObject loadFromFile(String file, boolean catchException) throws IOException {
		ComplexDataObject complexDataObject;

		if (!catchException)
			return mapper.readValue(new File(file), ComplexDataObject.class);
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
		} catch (UnrecognizedPropertyException ue) {
			ObjectMapper objectMapper = ObjectMapperFactory.getComplexDataObjectObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			try {
				function = objectMapper.readValue(new File(file), AttributeScoringFunction.class);
				return function;
			} catch (IOException e_) {
				e_.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
