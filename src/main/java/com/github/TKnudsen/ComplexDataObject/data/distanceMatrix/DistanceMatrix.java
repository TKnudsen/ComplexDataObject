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
 * @version 1.05
 */
public class DistanceMatrix<T> implements IDistanceMatrix<T> {

	// constructor properties
	private List<? extends T> elements;
	protected IDistanceMeasure<? super T> distanceMeasure;

	// storage, indexing
	protected double[][] distanceMatrix = null;
	protected Map<T, Integer> objectIndex;

	private boolean reportWhenDistanceIsMissing = false;

	public DistanceMatrix(List<? extends T> elements, IDistanceMeasure<? super T> distanceMeasure) {
		if (distanceMeasure == null)
			throw new IllegalArgumentException("DistanceMatrix: given distance measures was null");

		this.elements = elements;
		this.distanceMeasure = distanceMeasure;

		initializeDistanceMatrix();
	}

	protected void initializeObjectIndex() {
		// create index
		objectIndex = new HashMap<>();
		for (int i = 0; i < elements.size(); i++)
			objectIndex.put(elements.get(i), i);
	}

	protected void initializeDistanceMatrix() {
		initializeObjectIndex();

		distanceMatrix = new double[objectIndex.size()][objectIndex.size()];

		for (int x = 0; x < distanceMatrix.length; x++)
			for (int y = 0; y < distanceMatrix[x].length; y++)
				distanceMatrix[x][y] = Double.NaN;

		// create distance matrix - optimized for inheriting index-based access
		for (T t1 : objectIndex.keySet())
			for (T t2 : objectIndex.keySet()) {
				double distance = distanceMeasure.getDistance(t1, t2);

				distanceMatrix[getObjectIndex(t1)][getObjectIndex(t2)] = distance;
			}
	}

	protected Integer getObjectIndex(T object) {
		// if (objectIndex == null)
		// initializeDistanceMatrix();

		return objectIndex.get(object);
	}

	@Override
	public double getDistance(T o1, T o2) {
		Integer index1 = getObjectIndex(o1);
		Integer index2 = getObjectIndex(o2);

		if (index1 == null || index2 == null) {
			if (reportWhenDistanceIsMissing)
				System.out.println(
						"DistanceMatrix.getDistance: item not in the DM index, thus no lookup possible. Calculating distance..");
			return distanceMeasure.getDistance(o1, o2);
		}

		return getDistanceMatrix()[index1][index2];
	}

	@Override
	public String getName() {
		return "Distance Matrix using " + distanceMeasure.toString();
	}

	@Override
	public String getDescription() {
		return getName() + ", size: " + elements.size() + " x " + elements.size() + ".";
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

	public int size() {
		return elements.size();
	}

	@Override
	public boolean isSymmetric() {
		return false;
	}

	@Override
	public List<? extends T> getElements() {
		return elements;
	}

	public boolean isReportWhenDistanceIsMissing() {
		return reportWhenDistanceIsMissing;
	}

	public void setReportWhenDistanceIsMissing(boolean reportWhenDistanceIsMissing) {
		this.reportWhenDistanceIsMissing = reportWhenDistanceIsMissing;
	}

}
