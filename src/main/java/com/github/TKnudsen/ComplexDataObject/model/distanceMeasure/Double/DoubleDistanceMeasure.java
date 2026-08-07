package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double;

import java.io.Serializable;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.IDistanceMeasure;

/**
 * <p>
 * Basic class for all double[] distance measures.
 * </p>
 *
 * @version 1.05
 * @since 2017
 */
public abstract class DoubleDistanceMeasure implements IDistanceMeasure<double[]>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2559705521219780141L;

	public double applyAsDouble(double[] t, double[] u) {
		return getDistance(t, u);
	}

	public double dist(double[] a, double[] b) {
		return getDistance(a, b);
	}

	@Override
	public String getDescription() {
		return getName();
	}
}
