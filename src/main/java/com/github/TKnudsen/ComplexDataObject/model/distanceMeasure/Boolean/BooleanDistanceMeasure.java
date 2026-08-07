package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Boolean;

import java.io.Serializable;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.IDistanceMeasure;

/**
 * <p>
 * Basic class for boolean[] distance measures.
 * </p>
 *
 * @version 1.01
 * @since 2017
 */
public abstract class BooleanDistanceMeasure implements IDistanceMeasure<Boolean[]>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4443250297148475131L;

	public double applyAsDouble(Boolean[] t, Boolean[] u) {
		return getDistance(t, u);
	}
}
