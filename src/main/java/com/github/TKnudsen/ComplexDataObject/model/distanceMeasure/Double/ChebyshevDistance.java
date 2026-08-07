package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double;

import org.apache.commons.math3.ml.distance.DistanceMeasure;

/**
 * <p>
 * Double-array distance measure that delegates to Apache Commons Math's
 * ChebyshevDistance, i.e. the maximum absolute difference across all
 * dimensions.
 * </p>
 *
 * @version 1.01
 * @since 2017
 */
public class ChebyshevDistance extends DoubleDistanceMeasure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6401822601454203919L;

	DistanceMeasure distanceMeasure = new org.apache.commons.math3.ml.distance.ChebyshevDistance();

	@Override
	public double getDistance(double[] o1, double[] o2) {
		return distanceMeasure.compute(o1, o2);
	}

	@Override
	public String getName() {
		return "Chebyshev Distance Measure";
	}

}
