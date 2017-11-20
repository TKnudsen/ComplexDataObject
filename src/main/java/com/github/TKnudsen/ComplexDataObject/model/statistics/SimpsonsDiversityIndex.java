package com.github.TKnudsen.ComplexDataObject.model.statistics;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;

/**
 * <p>
 * Title: SimpsonsDiversityIndex
 * </p>
 *
 * <p>
 * Description: measure of diversity, often used to quantify biodiversity in
 * ecology.
 * 
 * Simpson EH. Measurement of diversity. Nature (1949).
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2016-2017
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.02
 */
public class SimpsonsDiversityIndex {

	/**
	 * 
	 * @param distribution
	 * @return
	 */
	public static double calculate(int[] distribution) {
		if (distribution == null)
			throw new IllegalArgumentException("SimpsonsDiversityIndex: given distribution was null");

		int n = 0;
		int N = 0;

		for (int i = 0; i < distribution.length; i++) {
			if (distribution[i] < 0)
				throw new IllegalArgumentException("SimpsonsDiversityIndex: given distribution contained value < 0");

			n += (distribution[i] * (distribution[i] - 1));
			N += distribution[i];
		}

		if (N == 0)
			n = 0;
		else if (N * (N - 1) != 0)
			n /= N * (N - 1);
		else
			n = 1;

		return n;
	}

	public static double calculate(List<Integer> distribution) {
		return calculate(DataConversion.toIntPrimitives(distribution));
	}

	/**
	 * converts a double distribution to a Simpsonizable int distribution. The
	 * smallest double value will achieve the int value 1. The remaining values
	 * will be rescaled with the same factor (linear transformation).
	 * 
	 * @param values
	 * @return
	 */
	public static int[] transformToIntDistribution(double[] values) {
		if (values == null)
			return null;

		double minUsedToScaleValues = Double.MAX_VALUE;
		for (int i = 0; i < values.length; i++)
			if (values[i] > 0)
				minUsedToScaleValues = Math.min(minUsedToScaleValues, values[i]);
			else
				throw new IllegalArgumentException("SimpsonsDiversityIndex: transformation failed because a given values was <= 0");

		if (minUsedToScaleValues != Double.MAX_VALUE)
			minUsedToScaleValues = 1 / minUsedToScaleValues;
		else
			minUsedToScaleValues = 1;

		int[] distribution = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			double value = values[i] * minUsedToScaleValues;
			distribution[i] = (int) Math.ceil(value);
		}

		return distribution;
	}

	/**
	 * converts a double distribution to a Simpsonizable int distribution. The
	 * smallest double value will achieve the int value 1. The remaining values
	 * will be rescaled with the same factor (linear transformation).
	 * 
	 * @param values
	 * @return
	 */
	public static int[] transformToIntDistribution(Double[] values) {
		return transformToIntDistribution(ArrayUtils.toPrimitive(values));
	}
}
