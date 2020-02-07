package com.github.TKnudsen.ComplexDataObject.data.distanceMatrix;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.IDistanceMeasure;

/**
 * <p>
 * Distance matrix interface. Stores distances of pairs of objects (T's).
 * 
 * The interface was shrinked in its functionality down to the mandatory parts.
 * Four statistical nice-to-have functions have been externalized to the new
 * class DistanceMatrixStatistics.
 * 
 * With the method isSymmetric() a speedup paramter was introduced.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017-2020
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.06
 */
public interface IDistanceMatrix<T> extends IDistanceMeasure<T> {

	public double[][] getDistanceMatrix();

	public List<T> getElements();

	public boolean isSymmetric();

}
