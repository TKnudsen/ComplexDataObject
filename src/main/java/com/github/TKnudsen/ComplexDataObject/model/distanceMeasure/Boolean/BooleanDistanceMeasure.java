package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Boolean;

import java.io.Serializable;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.IDistanceMeasure;

/**
 * <p>
 * Title: BooleanDistanceMeasure
 * </p>
 * 
 * <p>
 * Description: Basic class for boolean[] distance measures.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public abstract class BooleanDistanceMeasure implements IDistanceMeasure<boolean[]>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4443250297148475131L;

	public double applyAsDouble(boolean[] t, boolean[] u) {
		return getDistance(t, u);
	}
}
