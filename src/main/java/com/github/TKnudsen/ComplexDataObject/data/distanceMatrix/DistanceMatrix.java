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
	private List<T> objects;
	private IDistanceMeasure<T> distanceMeasure;

	// storage, indexing
	private double[][] distanceMatrix = null;
	private Map<T, Integer> objectIndex;

	// statistics
	private double min;
	private double max;

	public DistanceMatrix(List<T> objects, IDistanceMeasure<T> distanceMeasure) {
		if (distanceMeasure == null)
			throw new IllegalArgumentException("DistanceMatrix: given distance measures was null");

		this.objects = objects;
		this.distanceMeasure = distanceMeasure;

		initialize();
	}

	private void initialize() {
		distanceMatrix = new double[objects.size()][objects.size()];

		for (int x = 0; x < distanceMatrix.length; x++)
			for (int y = 0; y < distanceMatrix[x].length; y++)
				distanceMatrix[x][y] = Double.NaN;

		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;

		// create index
		objectIndex = new HashMap<>();
		for (int i = 0; i < distanceMatrix.length; i++)
			objectIndex.put(objects.get(i), i);

		// create distance matrix
		for (int i = 0; i < objects.size() - 1; i++)
			for (int j = i; j < objects.size(); j++) {
				double d1 = distanceMeasure.getDistance(objects.get(i), objects.get(j));
				double d2 = distanceMeasure.getDistance(objects.get(j), objects.get(i));

				distanceMatrix[i][j] = d1;
				distanceMatrix[j][i] = d2;

				min = Math.min(min, d1);
				min = Math.min(min, d2);

				max = Math.min(max, d1);
				max = Math.min(max, d2);
			}
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
	public double getDistance(T o1, T o2) {
		Integer index1 = objectIndex.get(o1);
		Integer index2 = objectIndex.get(o2);

		if (index1 == null || index2 == null)
			return distanceMeasure.getDistance(o1, o2);

		return distanceMatrix[index1][index2];
	}

	@Override
	public double applyAsDouble(T t, T u) {
		return getDistance(t, u);
	}

	@Override
	public double[][] getDistanceMatrix() {
		return distanceMatrix;
	}
}
