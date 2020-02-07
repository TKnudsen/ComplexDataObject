package com.github.TKnudsen.ComplexDataObject.data.distanceMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

/**
 * <p>
 * Statistics support for distance matrices. Can either be used to calculate
 * distance statistics in a static way, or be wrapped around a distance matrix
 * instance.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2020
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class DistanceMatrixStatistics<T> implements IDistanceMatrix<T> {

	// the wrapped distance matrix
	private final IDistanceMatrix<T> distanceMatrix;

	// statistics
	private double minDistance;
	private double maxDistance;

	// elements associated to statistics
	private List<T> closestElements = new ArrayList<>(2);
	private List<T> farestElements = new ArrayList<>(2);

	public DistanceMatrixStatistics(IDistanceMatrix<T> distanceMatrix) {
		this.distanceMatrix = distanceMatrix;

		updateDistanceStatistics();
	}

	private void updateDistanceStatistics() {
		minDistance = Double.MAX_VALUE - 1;
		maxDistance = Double.MIN_VALUE + 1;

		for (int i = 0; i < getElements().size(); i++)
			for (int j = 0; j < getElements().size(); j++)
				if (i == j) // assumption: only distances between Ts are relevant
					continue;
				else if (isSymmetric() && j < i)
					continue;
				else {
					T o1 = getElements().get(i);
					T o2 = getElements().get(j);
					double distance = getDistance(o1, o2);

					if (minDistance > distance) {
						minDistance = distance;

						closestElements.clear();
						closestElements.add(o1);
						closestElements.add(o2);
					}
					if (maxDistance < distance) {
						maxDistance = distance;
						farestElements.clear();
						farestElements.add(o1);
						farestElements.add(o2);
					}
				}
	}

	@Override
	public double getDistance(T o1, T o2) {
		return distanceMatrix.getDistance(o1, o2);
	}

	@Override
	public double applyAsDouble(T t, T u) {
		return distanceMatrix.applyAsDouble(t, u);
	}

	@Override
	public String getName() {
		return distanceMatrix.getName();
	}

	@Override
	public String getDescription() {
		return distanceMatrix.getDescription();
	}

	@Override
	public double[][] getDistanceMatrix() {
		return distanceMatrix.getDistanceMatrix();
	}

	@Override
	public List<T> getElements() {
		return distanceMatrix.getElements();
	}

	@Override
	public boolean isSymmetric() {
		return distanceMatrix.isSymmetric();
	}

	public double getMinDistance() {
		return minDistance;
	}

	public double getMaxDistance() {
		return maxDistance;
	}

	public List<T> getClosestElements() {
		return Collections.unmodifiableList(closestElements);
	}

	public List<T> getFarestElements() {
		return Collections.unmodifiableList(farestElements);
	}

	/**
	 * 
	 * @param distanceMatrix
	 * @param excludeMainDiagonal
	 * @param skipTransponedValues for every pair of instances only use one value
	 *                             (assumes a symmetric distance measure)
	 * @return
	 */
	public static List<Double> getPairwiseDistances(IDistanceMatrix<?> distanceMatrix, boolean excludeMainDiagonal,
			boolean skipTransponedValues) {
		List<Double> distances = new ArrayList<>(
				distanceMatrix.getElements().size() * distanceMatrix.getElements().size());

		double[][] dm = distanceMatrix.getDistanceMatrix();
		for (int i = 0; i < distanceMatrix.getElements().size(); i++)
			for (int j = 0; j < distanceMatrix.getElements().size(); j++)
				if (excludeMainDiagonal && i == j)
					continue;
				else if (skipTransponedValues && j < i)
					continue;
				else
					distances.add(dm[i][j]);

		return distances;
	}

	/**
	 * 
	 * @param distanceMatrix
	 * @param excludeMainDiagonal
	 * @param skipTransponedValues for every pair of instances only use one value
	 *                             (assumes a symmetric distance measure)
	 * @return
	 */
	public static <X> StatisticsSupport getPairwiseDistanceStatistics(IDistanceMatrix<X> distanceMatrix,
			boolean excludeMainDiagonal, boolean skipTransponedValues) {
		List<Double> distances = getPairwiseDistances(distanceMatrix, excludeMainDiagonal, skipTransponedValues);

		return new StatisticsSupport(distances);
	}

}
