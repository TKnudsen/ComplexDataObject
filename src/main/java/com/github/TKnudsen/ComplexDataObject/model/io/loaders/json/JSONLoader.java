package com.github.TKnudsen.ComplexDataObject.model.io.loaders.json;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

/**
 * <p>
 * Title: JSONLoader
 * </p>
 * 
 * <p>
 * Description: loads a ComplexDataObject from JSON
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.0
 */
public class JSONLoader {

	public static ComplexDataObject loadFromString(String json) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

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
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

		ComplexDataObject complexDataObject;
		try {
			complexDataObject = mapper.readValue(new File(file), ComplexDataObject.class);
			return complexDataObject;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
