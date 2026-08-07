package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double;

/**
 * <p>
 * Computes the standard (unweighted) Euclidean distance between two double
 * arrays of equal length.
 * </p>
 *
 * @version 1.01
 * @since 2017
 */
public class EuclideanDistanceMeasure extends DoubleDistanceMeasure {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1315973116014111192L;

	@Override
	public double getDistance(double[] o1, double[] o2) {
		if (o1 == null || o2 == null)
			return Double.NaN;

		if (o1.length != o2.length)
			throw new IllegalArgumentException(getName() + ": given arrays have different length");

		double dist = 0.0;
		for (int i = 0; i < o1.length; i++)
			dist += Math.pow(o1[i] - o2[i], 2.0);

		return Math.sqrt(dist);
	}

	@Override
	public String getName() {
		return "Euclidean Distance Measure";
	}

}
