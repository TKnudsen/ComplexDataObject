package com.github.TKnudsen.ComplexDataObject.model.statistics;

import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;

import com.github.TKnudsen.ComplexDataObject.model.tools.DataConversion;

/**
 * <p>
 * Title: SimpsonsIndex
 * </p>
 *
 * <p>
 * Description: Simpson's Index (D) measures the probability that two
 * individuals randomly selected from a sample will belong to the same species
 * (or some category other than species). The value of D ranges between 0 and 1.
 * With this index, 0 represents infinite diversity and 1, no diversity. That
 * is, the bigger the value of D, the lower the diversity.
 * 
 * Source of the text: http://www.countrysideinfo.co.uk/simpsons.htm
 * 
 * NOTE: The Simpson's Index is NOT Simpson's index of Diversity!
 * 
 * Variation: Simpson's Index of Diversity: 1 - D
 * 
 * Variation: Simpson's Reciprocal Index: 1 / D
 * 
 * Simpson EH. Measurement of diversity. Nature (1949).
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2016-2020
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.03
 */
public class SimpsonsIndex {

	/**
	 * The value of D ranges between 0 and 1. With this index, 0 represents infinite
	 * diversity and 1, no diversity. That is, the bigger the value of D, the lower
	 * the diversity.
	 * 
	 * To calculate Simpson's Index of Diversity, do 1-D.
	 * 
	 * @param distribution
	 * @return
	 */
	public static double calculateSimpsonsIndex(int[] distribution) {
		if (distribution == null)
			throw new IllegalArgumentException("SimpsonsIndex: given distribution was null");

		int numbers = 0;
		int totalNumber = 0;

		for (int i = 0; i < distribution.length; i++) {
			if (distribution[i] == 0) // skip 0 observations
				continue;
			else if (distribution[i] < 0)
				throw new IllegalArgumentException("SimpsonsIndex: given distribution contained value < 0");

			numbers += (distribution[i] * (distribution[i] - 1));
			totalNumber += distribution[i];
		}

		// bug fix
		if (totalNumber == 1 && distribution.length > 1)
			return 1.0;

		if (totalNumber < 2)
			return 0.0;
		else
			return numbers / (double) (totalNumber * (totalNumber - 1));
	}

	/**
	 * The value of D ranges between 0 and 1. With this index, 0 represents infinite
	 * diversity and 1, no diversity. That is, the bigger the value of D, the lower
	 * the diversity.
	 * 
	 * To calculate Simpson's Index of Diversity, do 1-D.
	 * 
	 * @param distribution
	 * @return
	 */
	public static double calculateSimpsonsIndex(Collection<Integer> distribution) {
		return calculateSimpsonsIndex(DataConversion.toIntPrimitives(distribution));
	}

	/**
	 * Simpson's Index of Diversity: 1 - D
	 * 
	 * 1.0 means infinite diversity while 0.0 means no diversity.
	 * 
	 * @param distribution
	 * @return
	 */
	public static double calculateSimpsonsIndexOfDiversity(int[] distribution) {
		return 1 - calculateSimpsonsIndex(distribution);
	}

	/**
	 * Simpson's Index of Diversity: 1 - D
	 * 
	 * 1.0 means infinite diversity while 0.0 means no diversity.
	 * 
	 * @param distribution
	 * @return
	 */
	public static double calculateSimpsonsIndexOfDiversity(Collection<Integer> distribution) {
		return 1 - calculateSimpsonsIndex(distribution);
	}

	/**
	 * converts a double distribution to a Simpsonizable int distribution. The
	 * smallest double value less than 0 will achieve the int value 1. The remaining
	 * values will be rescaled with the same factor (linear transformation). 0
	 * remains 0.
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
			else if (values[i] < 0)
				throw new IllegalArgumentException(
						"SimpsonsIndex: transformation failed because a given values was < 0");

		if (minUsedToScaleValues != Double.MAX_VALUE)
			minUsedToScaleValues = 1 / minUsedToScaleValues;
		else
			minUsedToScaleValues = 1;

		int[] distribution = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			if (values[i] > 0) {
				double value = values[i] * minUsedToScaleValues;
				distribution[i] = (int) Math.ceil(value);
			}
		}

		return distribution;
	}

	/**
	 * converts a double distribution to a Simpsonizable int distribution. The
	 * smallest double value less than 0 will achieve the int value 1. The remaining
	 * values will be rescaled with the same factor (linear transformation). 0
	 * remains 0.
	 * 
	 * @param values
	 * @return
	 */
	public static int[] transformToIntDistribution(Double[] values) {
		return transformToIntDistribution(ArrayUtils.toPrimitive(values));
	}
}
