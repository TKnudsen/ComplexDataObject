package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double;

import org.apache.commons.math3.ml.distance.DistanceMeasure;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.IDistanceMeasure;

/**
 * <p>
 * Title: DoubleDistanceMeasure
 * </p>
 * 
 * <p>
 * Description: Basic class for all double[] distance measures.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public abstract class DoubleDistanceMeasure implements IDistanceMeasure<double[]>, DistanceMeasure {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2559705521219780141L;

	@Override
	public double compute(double[] a, double[] b) {
		return getDistance(a, b);
	}

	@Override
	public double applyAsDouble(double[] t, double[] u) {
		return getDistance(t, u);
	}
}
