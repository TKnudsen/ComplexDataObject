package com.github.TKnudsen.ComplexDataObject.model.io.json;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.TKnudsen.ComplexDataObject.data.DataSchema;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.features.numericalData.NumericalFeatureVector;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.AttributeScoringFunction;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.BooleanAttributeScoringFunction;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.Double.DoubleAttributePositiveScoringFunction;

/**
 * <p>
 * Title: JSONLoader
 * </p>
 * 
 * <p>
 * Description: loads ComplexDataObjects from JSON
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017-2019
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public class JSONLoader {

	private static ObjectMapper mapper = ObjectMapperFactory.getComplexDataObjectObjectMapper();

	public static ComplexDataObject loadFromString(String json) {
//		ObjectMapper mapper = ObjectMapperFactory.getComplexDataObjectObjectMapper();

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
//		ObjectMapper mapper = ObjectMapperFactory.getComplexDataObjectObjectMapper();

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
//		ObjectMapper mapper = ObjectMapperFactory.getComplexDataObjectObjectMapper();

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
//		ObjectMapper mapper = ObjectMapperFactory.getComplexDataObjectObjectMapper();

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
