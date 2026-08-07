package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.ManhattanDistance;

/**
 * <p>
 * Double-array distance measure that delegates to Apache Commons Math's
 * ManhattanDistance, representing the Minkowski distance with exponent 1.
 * </p>
 *
 * @version 1.01
 * @since 2017
 */
public class ManhattanDistanceMeasure extends DoubleDistanceMeasure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6401822601454203919L;

	DistanceMeasure distanceMeasure = new ManhattanDistance();

	@Override
	public double getDistance(double[] o1, double[] o2) {
		return distanceMeasure.compute(o1, o2);
	}

	@Override
	public String getName() {
		return "Manhattan Distance Measure";
	}

	@Override
	public String getDescription() {
		return getName() + ": represents the Minkovski distance with exponent 1";
	}
}
