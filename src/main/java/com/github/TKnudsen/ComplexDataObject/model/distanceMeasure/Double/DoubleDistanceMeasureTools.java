package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Utility class providing a factory method to instantiate a
 * DoubleDistanceMeasure from its String name, along with a listing of the
 * distance measure names currently supported.
 * </p>
 *
 * @version 1.01
 * @since 2018
 */
public class DoubleDistanceMeasureTools {
	public static DoubleDistanceMeasure getDistFunction(String distanceFunctionName) {

		DoubleDistanceMeasure distFunction = null;

		switch (distanceFunctionName) {
		case "EuclideanDistance":
			distFunction = new EuclideanDistanceMeasure();
			break;
		case "ManhattanDistance":
			distFunction = new ManhattanDistanceMeasure();
			break;
		case "ChebyshevDistance":
			distFunction = new ChebyshevDistance();
			break;
		default:
			System.err.println(
					"DoubleDistanceMeasureTools: unknown distance function name. returning Euclidean Distance.");
			distFunction = new EuclideanDistanceMeasure();
			break;
		}

		return distFunction;

	}

	public static List<String> distanceMeasures() {
		return Arrays.asList(new String[] { "EuclideanDistance", "ChebyShevDistanceMeasure", "ManhattanDistance" });
	}
}
