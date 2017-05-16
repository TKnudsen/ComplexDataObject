package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double;

public class EuclideanDistanceMeasure extends DoubleDistanceMeasure {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1315973116014111192L;

	public double dist(double[] a, double[] b) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("Arrays differ in length");
		}
		double dist = 0.0;
		for (int i = 0; i < a.length; i++) {
			dist += Math.pow(a[i] - b[i], 2.0);
		}
		return Math.sqrt(dist);
	}

	@Override
	public double getDistance(double[] o1, double[] o2) {
		if (o1 == null || o2 == null)
			return Double.NaN;

		if (o1.length != o2.length)
			throw new IllegalArgumentException("Distance measure: given array have different length");

		double dist = 0.0;
		for (int i = 0; i < o1.length; i++)
			dist += Math.pow(o1[i] - o2[i], 2.0);

		return Math.sqrt(dist);
	}

	@Override
	public String getName() {
		return "Euclidean Distance Measure";
	}

	@Override
	public String getDescription() {
		return getName();
	}
}
