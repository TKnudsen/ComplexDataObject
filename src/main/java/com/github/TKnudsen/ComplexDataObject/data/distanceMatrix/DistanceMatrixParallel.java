package com.github.TKnudsen.ComplexDataObject.data.distanceMatrix;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.IntStream;

/**
 * <p>
 * Distance matrix implementation which allows the computation of pairwise
 * distances in a parallel way. Considerably faster for large matrices (large
 * greater or equals 1000 items).
 *
 * Speedup for symmetric matrices, both for parallel and non-parallel
 * computation.
 *
 * Tracks global minimum/maximum distance for O(1) access.
 * </p>
 *
 * @deprecated use DistanceMatrixBlockedParallel for better performance.
 * @version 1.02
 * @since 2017
 */
public class DistanceMatrixParallel<T> implements IDistanceMatrix<T> {

	private final List<? extends T> elements;
	private final Map<T, Integer> indices;
	private final boolean symmetric;
	private final boolean parallel;

	private final double matrix[][];

	// global minimum/maximum
	private final double globalMinDistance;
	private final double globalMaxDistance;

	public DistanceMatrixParallel(List<? extends T> elements,
			ToDoubleBiFunction<? super T, ? super T> distanceMeasure) {
		this(elements, distanceMeasure, true, true);
	}

	public DistanceMatrixParallel(List<? extends T> elements, ToDoubleBiFunction<? super T, ? super T> distanceMeasure,
			boolean symmetric, boolean parallel) {

		this.elements = elements;
		this.indices = new LinkedHashMap<T, Integer>();
		this.symmetric = symmetric;
		this.parallel = parallel;

		int n = elements.size();
		for (int i = 0; i < n; i++) {
			T element = elements.get(i);
			indices.put(element, i);
		}
		matrix = new double[n][n];

		if (parallel) {
			DoubleAccumulator minAcc = new DoubleAccumulator(Math::min, Double.POSITIVE_INFINITY);
			DoubleAccumulator maxAcc = new DoubleAccumulator(Math::max, Double.NEGATIVE_INFINITY);

			IntStream.range(0, n).parallel().forEach(i -> {
				int min = 0;
				if (symmetric)
					min = i + 1;

				IntStream.range(min, n).parallel().forEach(j -> {
					T ti = elements.get(i);
					T tj = elements.get(j);
					double d = distanceMeasure.applyAsDouble(ti, tj);
					matrix[i][j] = d;
					if (symmetric)
						matrix[j][i] = d;

					if (!Double.isNaN(d)) {
						minAcc.accumulate(d);
						maxAcc.accumulate(d);
					}

				});
			});

			double minVal = minAcc.get();
			double maxVal = maxAcc.get();

			// Handle the edge case where no values were accumulated (e.g., n <= 1).
			if (minVal == Double.POSITIVE_INFINITY)
				minVal = Double.NaN;

			if (maxVal == Double.NEGATIVE_INFINITY)
				maxVal = Double.NaN;

			this.globalMinDistance = minVal;
			this.globalMaxDistance = maxVal;
		} else {
			double minVal = Double.POSITIVE_INFINITY;
			double maxVal = Double.NEGATIVE_INFINITY;

			for (int i = 0; i < n; i++) {
				final T ti = elements.get(i);

				int minJ = 0;
				if (symmetric)
					minJ = i + 1; // keep consistent with parallel computation

				for (int j = minJ; j < n; j++) {
					final T tj = elements.get(j);
					final double d = distanceMeasure.applyAsDouble(ti, tj);

					matrix[i][j] = d;
					if (symmetric)
						matrix[j][i] = d;

					if (!Double.isNaN(d)) {
						if (d < minVal)
							minVal = d;
						if (d > maxVal)
							maxVal = d;
					}
				}
			}

			if (minVal == Double.POSITIVE_INFINITY)
				minVal = Double.NaN;
			if (maxVal == Double.NEGATIVE_INFINITY)
				maxVal = Double.NaN;

			this.globalMinDistance = minVal;
			this.globalMaxDistance = maxVal;
		}
	}

	@Override
	public double applyAsDouble(T t, T u) {
		int index0 = indices.get(t);
		int index1 = indices.get(u);
		return matrix[index0][index1];
	}

	@Override
	public double getDistance(T o1, T o2) {
		return applyAsDouble(o1, o2);
	}

	/**
	 * Returns the global minimum distance encountered during matrix computation.
	 * O(1).
	 *
	 * Note: Returns NaN if no distances were computed (e.g., elements.size() <= 1).
	 */
	public double getGlobalMinDistance() {
		return globalMinDistance;
	}

	/**
	 * Returns the global maximum distance encountered during matrix computation.
	 * O(1).
	 *
	 * Note: Returns NaN if no distances were computed (e.g., elements.size() <= 1).
	 */
	public double getGlobalMaxDistance() {
		return globalMaxDistance;
	}

	@Override
	public String getName() {
		return DistanceMatrixParallel.class.getSimpleName();
	}

	@Override
	public String getDescription() {
		return "Can be calculated in a parallel way to speedup computation time";
	}

	@Override
	public double[][] getDistanceMatrix() {
		return matrix;
	}

	@Override
	public List<? extends T> getElements() {
		return elements;
	}

	@Override
	public boolean isSymmetric() {
		return symmetric;
	}

	public boolean isParallel() {
		return parallel;
	}

}
