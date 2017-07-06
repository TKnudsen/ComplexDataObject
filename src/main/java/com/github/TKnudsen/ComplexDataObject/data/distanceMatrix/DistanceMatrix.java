package com.github.TKnudsen.ComplexDataObject.data.distanceMatrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.IDistanceMeasure;

/**
 * <p>
 * Title: DistanceMatrix
 * </p>
 * 
 * <p>
 * Description: Stores and manages distances of pairs of objects (T's)
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2017
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class DistanceMatrix<T> implements IDistanceMatrix<T> {

	// constructor properties
	protected List<T> objects;
	protected IDistanceMeasure<T> distanceMeasure;

	// storage, indexing
	protected double[][] distanceMatrix = null;
	protected Map<T, Integer> objectIndex;

	// statistics
	private double min;
	private double max;

	public DistanceMatrix(List<T> objects, IDistanceMeasure<T> distanceMeasure) {
		if (distanceMeasure == null)
			throw new IllegalArgumentException("DistanceMatrix: given distance measures was null");

		this.objects = objects;
		this.distanceMeasure = distanceMeasure;
	}

	protected void initializeObjectIndex() {
		// create index
		objectIndex = new HashMap<>();
		for (int i = 0; i < objects.size(); i++)
			objectIndex.put(objects.get(i), i);
	}

	protected void initializeDistanceMatrix() {
		initializeObjectIndex();

		distanceMatrix = new double[objectIndex.size()][objectIndex.size()];

		for (int x = 0; x < distanceMatrix.length; x++)
			for (int y = 0; y < distanceMatrix[x].length; y++)
				distanceMatrix[x][y] = Double.NaN;

		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;

		// create distance matrix - optimized for inheriting index-based access
		for (T t1 : objectIndex.keySet())
			for (T t2 : objectIndex.keySet()) {
				double distance = distanceMeasure.getDistance(t1, t2);

				distanceMatrix[getObjectIndex(t1)][getObjectIndex(t2)] = distance;

				min = Math.min(min, distance);
				max = Math.max(max, distance);
			}

		// // create distance matrix - would be at least as good as the upper
		// // variant. but does not generalize for inheriting classes
		// for (int i = 0; i < objectIndex.size() - 1; i++)
		// for (int j = i; j < objectIndex.size(); j++) {
		// double d1 = distanceMeasure.getDistance(objects.get(i),
		// objects.get(j));
		// double d2 = distanceMeasure.getDistance(objects.get(j),
		// objects.get(i));
		//
		// distanceMatrix[i][j] = d1;
		// distanceMatrix[j][i] = d2;
		//
		// min = Math.min(min, d1);
		// min = Math.min(min, d2);
		//
		// max = Math.max(max, d1);
		// max = Math.max(max, d2);
		// }
	}

	protected Integer getObjectIndex(T object) {
		if (objectIndex == null)
			initializeDistanceMatrix();

		return objectIndex.get(object);
	}

	@Override
	public double getDistance(T o1, T o2) {
		Integer index1 = getObjectIndex(o1);
		Integer index2 = getObjectIndex(o2);

		// better let it burn. it is a bad design if these indices would be null
		// if (index1 == null || index2 == null)
		// return distanceMeasure.getDistance(o1, o2);

		return getDistanceMatrix()[index1][index2];
	}

	@Override
	public String getName() {
		return "Distance Matrix using " + distanceMeasure.toString();
	}

	@Override
	public String getDescription() {
		return getName() + ", size: " + objects.size() + " x " + objects.size() + ".";
	}

	@Override
	public double applyAsDouble(T t, T u) {
		return getDistance(t, u);
	}

	@Override
	public double[][] getDistanceMatrix() {
		if (distanceMatrix == null)
			initializeDistanceMatrix();

		return distanceMatrix;
	}
}
