package com.github.TKnudsen.ComplexDataObject.model.io.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ObjectMapperFactory {

	private static ObjectMapper complexDataObjectObjectMapper;

	public static ObjectMapper getComplexDataObjectObjectMapper() {
		if (complexDataObjectObjectMapper == null)
			initComplexDataObjectObjectMapper();

		return complexDataObjectObjectMapper;
	}

	private static void initComplexDataObjectObjectMapper() {
		complexDataObjectObjectMapper = new ObjectMapper();
		complexDataObjectObjectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		complexDataObjectObjectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true);
		complexDataObjectObjectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		complexDataObjectObjectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		complexDataObjectObjectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

	}

	public static ObjectMapper createComplexDataObjectObjectMapper(boolean ignoreUnknownProperties) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, true);
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

		if (ignoreUnknownProperties)
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		return objectMapper;
	}
}
