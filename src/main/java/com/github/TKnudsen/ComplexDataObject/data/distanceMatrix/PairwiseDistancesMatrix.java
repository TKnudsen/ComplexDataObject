package com.github.TKnudsen.ComplexDataObject.data.distanceMatrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Distance matrix implementation that accepts a matrix of pairwise distances
 * from an external source.
 * 
 * Assumes that the given pairwise distances are symmetric.
 * </p>
 * 
 * *
 * <p>
 * Copyright: (c) 2016-2017 Juergen Bernard, https://github.com/TKnudsen/DMandML
 * </p>
 * 
 * @author Christian Ritter, Juergen Bernard
 * @version 1.04
 */
public class PairwiseDistancesMatrix<T> implements IDistanceMatrix<T> {

	private double[][] distanceMatrix;
	private Map<T, Integer> objectMapping;
	private List<T> elements;

	public PairwiseDistancesMatrix(List<T> objects, double[][] pairwiseDistances) {
		this.distanceMatrix = pairwiseDistances;
		this.elements = objects;

		initializeObjectMapping();
	}

	private void initializeObjectMapping() {
		objectMapping = new HashMap<>();
		int i = 0;
		for (T t : getElements()) {
			objectMapping.put(t, i++);
		}
	}

	@Override
	public double getDistance(T o1, T o2) {
		Integer i = objectMapping.get(o1);
		Integer j = objectMapping.get(o2);
		if (i == null || j == null) {
			return Double.NaN;
		}
		return getDistanceMatrix()[i][j];
	}

	@Override
	public boolean isSymmetric() {
		return true;
	}

	@Override
	public double applyAsDouble(T t, T u) {
		return getDistance(t, u);
	}

	@Override
	public String getName() {
		return "PairwiseDistancesMatrix";
	}

	@Override
	public String getDescription() {
		return "IDistanceMatrix wrapper for pairwise distances already represented as 2D double array";
	}

	@Override
	public double[][] getDistanceMatrix() {
		return distanceMatrix;
	}

	@Override
	public List<T> getElements() {
		return elements;
	}

}
