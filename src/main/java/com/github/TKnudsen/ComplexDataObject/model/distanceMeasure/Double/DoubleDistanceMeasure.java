package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double;

import java.io.Serializable;

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
 * @version 1.03
 */
public abstract class DoubleDistanceMeasure implements IDistanceMeasure<double[]>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2559705521219780141L;

	@Deprecated
	public double compute(double[] a, double[] b) {
		return getDistance(a, b);
	}

	public double applyAsDouble(double[] t, double[] u) {
		return getDistance(t, u);
	}
}
