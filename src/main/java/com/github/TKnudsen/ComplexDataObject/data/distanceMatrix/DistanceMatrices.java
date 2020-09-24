package com.github.TKnudsen.ComplexDataObject.data.distanceMatrix;

import java.util.List;
import java.util.function.ToDoubleBiFunction;

public class DistanceMatrices {

	/**
	 * pairwise distances between elements calculated with a distance measure
	 * represented by a biFunction. Assumes that the pairwise distances are not
	 * symmetric.
	 * 
	 * @param <T>             The element type
	 * @param elements        The elements
	 * @param distanceMeasure The distance measure
	 * @return pairwise distances represented as a matrix of doubles
	 */
	public static <T> double[][] distanceMatrix(List<? extends T> elements,
			ToDoubleBiFunction<? super T, ? super T> distanceMeasure) {

		return distanceMatrix(elements, distanceMeasure, false);
	}

	/**
	 * pairwise distances between elements calculated with a distance measure
	 * represented by a biFunction.
	 * 
	 * @param <T>              The element type
	 * @param elements         The elements
	 * @param distanceMeasure  The distance measure
	 * @param symmetricMeasure determine whether apply(a,b) and apply(b,a) will
	 *                         always lead to the same result. if yes this will
	 *                         speedup the process by factor two.
	 * @return pairwise distances represented as a matrix of doubles
	 */
	public static <T> double[][] distanceMatrix(List<? extends T> elements,
			ToDoubleBiFunction<? super T, ? super T> distanceMeasure, boolean symmetricMeasure) {

		DistanceMatrixParallel<T> distanceMatrix = new DistanceMatrixParallel<T>(elements, distanceMeasure,
				symmetricMeasure, true);

		return distanceMatrix.getDistanceMatrix();
	}

	/**
	 * avoid instantiation
	 */
	private DistanceMatrices() {
	}
}
