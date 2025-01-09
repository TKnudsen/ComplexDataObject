package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.String;

import java.io.Serializable;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.IDistanceMeasure;

/**
 * <p>
 * Title: StringDistanceMeasure
 * </p>
 * 
 * <p>
 * Description: Basic class for String distance measures.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2024
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
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