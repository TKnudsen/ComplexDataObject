package com.github.TKnudsen.ComplexDataObject.data.distanceMatrix;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.IDistanceMeasure;

/**
 * <p>
 * Title: TDistanceMatrix
 * </p>
 * 
 * <p>
 * Description: Interface for distance matrices. Store and manage distances of
 * pairs of objects (T's)
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.02
 */
public interface IDistanceMatrix<T> extends IDistanceMeasure<T> {

	public double[][] getDistanceMatrix();

	/**
	 * allows operations like normalization with no additional effort
	 * 
	 * @return
	 */
	public double getMinDistance();

	/**
	 * allows operations like normalization with no additional effort
	 * 
	 * @return
	 */
	public double getMaxDistance();
}
