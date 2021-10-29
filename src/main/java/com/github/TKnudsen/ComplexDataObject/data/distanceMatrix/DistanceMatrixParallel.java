package com.github.TKnudsen.ComplexDataObject.data.distanceMatrix;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.IntStream;

/**
 * Distance matrix implementation which allows the computation of pairwise
 * distances in a parallel way. Considerably faster for large matrices (large
 * greater or equals 1000 items).
 */
public class DistanceMatrixParallel<T> implements IDistanceMatrix<T> {

	private final List<? extends T> elements;
	private final Map<T, Integer> indices;
	private final boolean symmetric;
	private final boolean parallel;

	private final double matrix[][];

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
			IntStream.range(0, n).parallel().forEach(i -> {
				int min = 0;
				if (symmetric) {
					min = i + 1;
				}
				IntStream.range(min, n).parallel().forEach(j -> {
					T ti = elements.get(i);
					T tj = elements.get(j);
					double d = distanceMeasure.applyAsDouble(ti, tj);
					matrix[i][j] = d;
					if (symmetric) {
						matrix[j][i] = d;
					}
				});
			});
		} else {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					T ti = elements.get(i);
					T tj = elements.get(j);
					double d = distanceMeasure.applyAsDouble(ti, tj);
					matrix[i][j] = d;
				}
			}
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
