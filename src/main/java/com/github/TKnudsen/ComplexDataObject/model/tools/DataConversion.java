package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Title: DataConversion
 * </p>
 *
 * <p>
 * Description: Provides little helpers for the conversion of general data
 * structures
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.02
 */
public class DataConversion {

	/**
	 * convertss a list of Double objects to an array of double primitives.
	 * 
	 * @param values
	 * @return
	 */
	public static double[] toPrimitives(List<Double> values) {
		return values.stream().mapToDouble(Double::doubleValue).toArray();

		// if (values == null)
		// return null;
		//
		// double[] ret = new double[values.size()];
		//
		// for (int i = 0; i < values.size(); i++)
		// ret[i] = values.get(i).doubleValue();
		//
		// return ret;
	}

	/**
	 * Converts an array of double primitives to an array of Double objects.
	 * 
	 * @param values
	 * @return
	 */
	public static Double[] doublePrimitivesToArray(double[] values) {
		if (values == null)
			return null;
		Double[] array = new Double[values.length];
		for (int i = 0; i < values.length; i++)
			array[i] = values[i];
		return array;
	}

	/**
	 * Converts an array of double primitives to an array of Double objects.
	 * 
	 * @param values
	 * @return
	 */
	public static List<Double> doublePrimitivesToList(double[] values) {
		return Arrays.asList(DataConversion.doublePrimitivesToArray(values));
	}

	/**
	 * Converts a given List to an array.
	 * 
	 * @param list
	 * @param classType
	 * @return
	 */
	public static <T> T[] listToArray(List<T> list, Class<T> classType) {
		if (list == null)
			return null;
		if (classType == null)
			throw new IllegalArgumentException("DataConversion.listToArray: class information missing.");

		@SuppressWarnings("unchecked")
		T[] array = (T[]) Array.newInstance(classType, list.size());

		for (int i = 0; i < list.size(); ++i) {
			array[i] = list.get(i);
		}
		
		return array;
	}
}
