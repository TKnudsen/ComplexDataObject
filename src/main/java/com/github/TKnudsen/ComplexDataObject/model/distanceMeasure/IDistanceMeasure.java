package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure;

import java.io.Serializable;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.IDObject;
import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;

/**
 * <p>
 * Title: IDistanceMeasure
 * </p>
 * 
 * <p>
 * Description: Generic interface for all data object distance measures.
 * Similarity is rather complex, so defining an abstract concept is quite
 * challenging.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2013-2016
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public interface IDistanceMeasure<T extends IDObject> extends Serializable, ISelfDescription {

	public abstract String toString();

	public double getDistance(T o1, T o2);
}
