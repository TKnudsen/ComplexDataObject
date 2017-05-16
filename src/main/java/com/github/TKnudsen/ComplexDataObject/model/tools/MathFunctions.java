package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.util.List;
import java.util.UUID;

/**
 * <p>
 * Title: MathFunctions
 * </p>
 *
 * <p>
 * Description: little helpers when calculating trivial math stuff.
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.01
 */

public class MathFunctions {

	/**
	 * scales a value w.r.t. a given minimum and maximum value. The value can be
	 * outside the interval.
	 * 
	 * @param min
	 * @param max
	 * @param value
	 * @return
	 */
	public static double linearScale(double min, double max, double value) {
		if (!Double.isNaN(value))
			if (max != min)
				return (value - min) / (max - min);
			else if (max != 0)
				return (value - min) / (max);
			else
				return 1.0;
		else
			return Double.NaN;
	}

	/**
	 * scales a value w.r.t. a given minimum and maximum value. A boolean can be
	 * set to limit the return value between [0...1].
	 * 
	 * @param min
	 * @param max
	 * @param value
	 * @param limitToInterval
	 * @return
	 */
	public static double linearScale(double min, double max, double value, boolean limitToInterval) {
		if (Double.isNaN(value))
			return Double.NaN;

		double retValue = Double.NaN;

		if (max != min)
			retValue = (value - min) / (max - min);
		else if (max != 0)
			retValue = (value - min) / (max);
		else
			retValue = 1.0;

		if (limitToInterval) {
			retValue = Math.min(retValue, 1.0);
			retValue = Math.max(retValue, 0.0);
		}

		return retValue;
	}

	/**
	 * rounds a given value w.r.t a given number of decimals.
	 * 
	 * @param value
	 * @param decimals
	 * @return
	 */
	public static double round(double value, int decimals) {
		if (Double.isNaN(value))
			return value;

		double pow = Math.pow(10, decimals);
		double d = value * pow;
		d = Math.round(d);
		d /= pow;

		return d;
	}

	/**
	 * Returns a random long using Random.nextLong();
	 * 
	 * @return
	 */
	public static long randomLong() {
		return UUID.randomUUID().getMostSignificantBits();

		// Random random = new Random();
		// return random.nextLong();
	}

	/**
	 * Calculates the min value for a given series of values. Ignores
	 * Double.NAN.
	 * 
	 * @param values
	 * @return
	 */
	public static double getMin(List<Double> values) {
		if (values == null)
			return Double.NaN;

		double min = Double.POSITIVE_INFINITY - 1;

		boolean onlyNaN = true;
		for (Double d : values)
			if (Double.isNaN(d))
				continue;
			else {
				min = Math.min(min, d);
				onlyNaN = false;
			}

		if (onlyNaN)
			return Double.NaN;

		return min;
	}

	/**
	 * Calculates the min value for a given series of values. Ignores
	 * Double.NAN.
	 * 
	 * @param values
	 * @return
	 */
	public static double getMin(double[] values) {
		if (values == null)
			return Double.NaN;

		double min = Double.POSITIVE_INFINITY - 1;

		boolean onlyNaN = true;
		for (double d : values)
			if (Double.isNaN(d))
				continue;
			else {
				min = Math.min(min, d);
				onlyNaN = false;
			}

		if (onlyNaN)
			return Double.NaN;

		return min;
	}

	/**
	 * Calculates the min value for a given series of values. Ignores
	 * Double.NAN.
	 * 
	 * @param values
	 * @return
	 */
	public static double getMin(Number[] values) {
		if (values == null)
			return Double.NaN;

		double min = Double.POSITIVE_INFINITY - 1;

		boolean onlyNaN = true;
		for (Number n : values)
			if (n == null)
				continue;
			else if (Double.isNaN(n.doubleValue()))
				continue;
			else {
				min = Math.min(min, n.doubleValue());
				onlyNaN = false;
			}

		if (onlyNaN)
			return Double.NaN;

		return min;
	}

	/**
	 * Calculates the mean value for a given series of values. Ignores
	 * Double.NAN
	 * 
	 * @param values
	 * @return mean value
	 */
	public static double getMean(double values[]) {
		if (values == null)
			return Double.NaN;

		double sum = 0;
		double count = 0;
		for (double d : values)
			if (!Double.isNaN(d)) {
				sum += d;
				count++;
			}

		return sum / count;
	}

	/**
	 * Calculates the mean value for a given series of values. Ignores
	 * Double.NAN
	 * 
	 * @param values
	 * @return
	 */
	public static double getMean(List<Double> values) {
		if (values == null)
			return Double.NaN;

		double sum = 0;
		double count = 0;
		for (double d : values)
			if (!Double.isNaN(d)) {
				sum += d;
				count++;
			}

		return sum / count;
	}

	/**
	 * Calculates the min value for a given series of values. Ignores
	 * Double.NAN.
	 * 
	 * @param values
	 * @return
	 */
	public static double getMax(List<Double> values) {
		if (values == null)
			return Double.NaN;

		double max = Double.NEGATIVE_INFINITY + 1;

		boolean onlyNaN = true;
		for (Double d : values)
			if (Double.isNaN(d))
				continue;
			else {
				max = Math.max(max, d);
				onlyNaN = false;
			}

		if (onlyNaN)
			return Double.NaN;

		return max;
	}

	/**
	 * Calculates the min value for a given series of values. Ignores
	 * Double.NAN.
	 * 
	 * @param values
	 * @return
	 */
	public static double getMax(double[] values) {
		if (values == null)
			return Double.NaN;

		double max = Double.NEGATIVE_INFINITY + 1;

		boolean onlyNaN = true;
		for (double d : values)
			if (Double.isNaN(d))
				continue;
			else {
				max = Math.max(max, d);
				onlyNaN = false;
			}

		if (onlyNaN)
			return Double.NaN;

		return max;
	}

	/**
	 * Calculates the min value for a given series of values. Ignores
	 * Double.NAN.
	 * 
	 * @param values
	 * @return
	 */
	public static double getMax(Number[] values) {
		if (values == null)
			return Double.NaN;

		double max = Double.NEGATIVE_INFINITY + 1;

		boolean onlyNaN = true;
		for (Number n : values)
			if (n == null)
				continue;
			else if (Double.isNaN(n.doubleValue()))
				continue;
			else {
				max = Math.max(max, n.doubleValue());
				onlyNaN = false;
			}

		if (onlyNaN)
			return Double.NaN;

		return max;
	}

	/**
	 * calculates the variance of a double array.
	 * 
	 * @param values
	 * @return
	 */
	public static double getVariance(double values[]) {
		double sumOfSquares = 0;

		if (values == null)
			return sumOfSquares;

		double mean = getMean(values);

		for (int i = 0; i < values.length; i++)
			sumOfSquares += Math.pow((values[i] - mean), 2);

		return sumOfSquares / (double) values.length;
	}

	/**
	 * calculates the variance of a double array.
	 * 
	 * @param values
	 * @return
	 */
	public static double getVariance(List<Double> values) {
		double sumOfSquares = 0;

		if (values == null)
			return sumOfSquares;

		double mean = getMean(values);

		for (int i = 0; i < values.size(); i++)
			sumOfSquares += Math.pow((values.get(i) - mean), 2);

		return sumOfSquares / (double) values.size();
	}

	/**
	 * calculates the standard deviation (SD, sDev) of a double array.
	 * 
	 * @param valules
	 * @return
	 */
	public static double getStandardDeviation(double valules[]) {
		double sumOfSquares = 0;

		if (valules == null)
			return sumOfSquares;

		double mean = getMean(valules);

		for (int i = 0; i < valules.length; i++)
			sumOfSquares += Math.pow((valules[i] - mean), 2);

		return Math.sqrt(sumOfSquares / (double) valules.length);
	}

	/**
	 * calculates the standard deviation (SD, sDev) of a double array.
	 * 
	 * @param valules
	 * @return
	 */
	public static double getStandardDeviation(List<Double> valules) {
		double sumOfSquares = 0;

		if (valules == null)
			return sumOfSquares;

		double mean = getMean(valules);

		for (int i = 0; i < valules.size(); i++)
			sumOfSquares += Math.pow((valules.get(i) - mean), 2);

		return Math.sqrt(sumOfSquares / (double) valules.size());
	}
}
