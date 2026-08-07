package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.String;

import java.io.Serializable;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.IDistanceMeasure;

/**
 * <p>
 * Basic class for String distance measures.
 * </p>
 *
 * @version 1.01
 * @since 2024
 */
public abstract class StringDistanceMeasure implements IDistanceMeasure<String>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public double applyAsDouble(String t, String u) {
		return getDistance(t, u);
	}
}