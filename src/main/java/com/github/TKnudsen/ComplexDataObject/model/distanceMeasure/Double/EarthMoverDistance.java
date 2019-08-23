package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double;

import org.apache.commons.math3.ml.distance.EarthMoversDistance;

public class EarthMoverDistance extends DoubleDistanceMeasure {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9139238493346028987L;

	EarthMoversDistance emd = new EarthMoversDistance();

	@Override
	public double getDistance(double[] o1, double[] o2) {
		if (o1 == null || o2 == null)
			throw new NullPointerException(getName() + ": given array was null");

		if (o1.length != o2.length)
			throw new IllegalArgumentException(getName() + ": given arrays have different length");

		return emd.compute(o1, o2);
	}

	@Override
	public String getName() {
		return "Earth Mover Distance";
	}

}
