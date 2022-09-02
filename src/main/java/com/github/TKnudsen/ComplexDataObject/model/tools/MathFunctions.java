package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>
 * Title: MathFunctions
 * </p>
 *
 * <p>
 * Description: little math helpers.
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2017-2022
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.08
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
		return linearScale(min, max, value, false);
	}

	/**
	 * scales a value w.r.t. a given minimum and maximum value. The value can be
	 * outside the interval.
	 * 
	 * @param min
	 * @param max
	 * @param value
	 * @return
	 */
	public static double linearScale(Number min, Number max, Number value) {
		return linearScale(min.doubleValue(), max.doubleValue(), value.doubleValue(), false);
	}

	/**
	 * scales a value w.r.t. a given minimum and maximum value. A boolean can be set
	 * to limit the return value between [0...1].
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
	 * logarithmic scale. applies a hack for input values between 0 and 1. should be
	 * validated.
	 * 
	 * @param min
	 * @param max
	 * @param value
	 * @param limitToInterval
	 * @return
	 */
	public static double logarithmicScale(double min, double max, double value, boolean limitToInterval) {
		min += 1;
		max += 1;
		value += 1;
		if (min != 0)
			min = Math.log(min);
		if (max != 0)
			max = Math.log(max);
		if (value != 0)
			if (value < 0)
				value = -Math.log(-value);
			else
				value = Math.log(value);

		// -> [0,1]
		return linearScale(min, max, value, limitToInterval);
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
		// return UUID.randomUUID().getMostSignificantBits();

		return ThreadLocalRandom.current().nextLong();

		// Random random = new Random();
		// return random.nextLong();
	}

	/**
	 * Calculates the min value for a given series of values. Ignores Double.NAN.
	 * 
	 * @param values
	 * @return
	 */
	public static double getMin(Collection<? extends Number> values) {
		if (values == null)
			return Double.NaN;

		double min = Double.POSITIVE_INFINITY - 1;

		boolean onlyNaN = true;
		for (Number n : values) {
			Double d = n.doubleValue();
			if (d == null || Double.isNaN(d))
				continue;
			else {
				min = Math.min(min, d);
				onlyNaN = false;
			}
		}

		if (onlyNaN)
			return Double.NaN;

		return min;
	}

	/**
	 * Calculates the min value for a given series of values. Ignores Double.NAN.
	 * 
	 * @param values
	 * @return
	 */
	public static double getMin(double[] values) {
		if (values == null)
			return Double.NaN;

		double min = Double.POSITIVE_INFINITY - 1;

		boolean onlyNaN = true;
		for (Double d : values)
			if (d == null || Double.isNaN(d))
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
	 * Calculates the min value for a given series of values. Ignores Double.NAN.
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
	 * Calculates the mean value for a given series of values. Ignores Double.NAN
	 * 
	 * @param values
	 * @return mean value
	 */
	public static double getMean(double values[]) {
		if (values == null)
			return Double.NaN;

		double sum = 0;
		double count = 0;
		for (Double d : values)
			if (d != null && !Double.isNaN(d)) {
				sum += d;
				count++;
			}

		return sum / count;
	}

	/**
	 * Calculates the mean value for a given series of values. Ignores Double.NAN
	 * 
	 * @param values
	 * @return
	 */
	public static double getMean(Collection<? extends Number> values) {
		if (values == null)
			return Double.NaN;

		double sum = 0;
		double count = 0;
		for (Number n : values) {
			Double d = n.doubleValue();
			if (d != null && !Double.isNaN(d)) {
				sum += d;
				count++;
			}
		}

		return sum / count;
	}

	/**
	 * Calculates the max value for a given collection. Ignores Double.NAN.
	 * 
	 * @param values
	 * @return
	 */
	public static double getMax(Collection<? extends Number> values) {
		if (values == null)
			return Double.NaN;

		double max = Double.NEGATIVE_INFINITY + 1;

		boolean onlyNaN = true;
		for (Number n : values) {
			Double d = n.doubleValue();
			if (d == null || Double.isNaN(d))
				continue;
			else {
				max = Math.max(max, d);
				onlyNaN = false;
			}
		}

		if (onlyNaN)
			return Double.NaN;

		return max;
	}

	/**
	 * Calculates the min value for a given series of values. Ignores Double.NAN.
	 * 
	 * @param values
	 * @return
	 */
	public static double getMax(double[] values) {
		if (values == null)
			return Double.NaN;

		double max = Double.NEGATIVE_INFINITY + 1;

		boolean onlyNaN = true;
		for (Double d : values)
			if (d == null || Double.isNaN(d))
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
	 * Calculates the min value for a given series of values. Ignores Double.NAN.
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
	public static double getVariance(Collection<? extends Number> values) {
		double sumOfSquares = 0;

		if (values == null)
			return sumOfSquares;

		double mean = getMean(values);

		for (Number n : values)
			sumOfSquares += Math.pow((n.doubleValue() - mean), 2);

		return sumOfSquares / (double) values.size();
	}

	/**
	 * calculates the standard deviation (SD, sDev) of a double array.
	 * 
	 * @param values
	 * @return
	 */
	public static double getStandardDeviation(double values[]) {
		double sumOfSquares = 0;

		if (values == null)
			return sumOfSquares;

		double mean = getMean(values);

		for (int i = 0; i < values.length; i++)
			sumOfSquares += Math.pow((values[i] - mean), 2);

		return Math.sqrt(sumOfSquares / (double) values.length);
	}

	/**
	 * calculates the standard deviation (SD, sDev) of a double array.
	 * 
	 * @param values
	 * @return
	 */
	public static double getStandardDeviation(Collection<? extends Number> values) {
		double sumOfSquares = 0;

		if (values == null)
			return sumOfSquares;

		double mean = getMean(values);

		for (Number n : values)
			sumOfSquares += Math.pow((n.doubleValue() - mean), 2);

		return Math.sqrt(sumOfSquares / (double) values.size());
	}

	/**
	 * sums up a series of Double values.
	 * 
	 * @param values
	 * @param ignoreNAN
	 * @return
	 */
	public static Double getSum(Collection<? extends Number> values, boolean ignoreNAN) {
		Double sum = 0.0;

		for (Number d : values)
			if (Double.isNaN(d.doubleValue()) && ignoreNAN)
				continue;
			else
				sum += d.doubleValue();

		return sum;
	}

	/**
	 * simple Double.NaN checker.
	 * 
	 * @param values
	 * @return
	 */
	public static boolean containsNaN(Collection<? extends Number> values) {
		if (values == null)
			return false;

		for (Number n : values)
			if (Double.isNaN(n.doubleValue()))
				return true;

		return false;
	}

	/**
	 * simple Double.NaN checker.
	 * 
	 * @param values
	 * @return
	 */
	public static boolean containsNaN(Number[] values) {
		if (values == null)
			return false;

		for (Number n : values)
			if (Double.isNaN(n.doubleValue()))
				return true;

		return false;
	}

	/**
	 * determines whether or not the given double array contains floating point
	 * values
	 * 
	 * @param values
	 * @return
	 */
	public static boolean hasFloatingPointValues(double[] values) {
		for (int i = 0; i < values.length; i++) {
			if (values[i] != Math.floor(values[i]))
				return true;
		}
		return false;
	}
}
