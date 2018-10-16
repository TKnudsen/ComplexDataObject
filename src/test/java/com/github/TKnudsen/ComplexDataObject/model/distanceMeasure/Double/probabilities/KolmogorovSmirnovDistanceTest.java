package com.github.TKnudsen.ComplexDataObject.model.distanceMeasure.Double.probabilities;

import java.util.Arrays;

public class KolmogorovSmirnovDistanceTest {

	public static void main(String[] args) {

		KolmogorovSmirnovDistance dm = new KolmogorovSmirnovDistance();

		for (int i = 2; i < 10; i++) {

			double[] v1 = createTriangleProbabilityDecreasing(i);
			double[] v2 = createTriangleProbabilityIncreasing(i);
			double[] v3 = createRandomProbabilities(i);

			System.out.println("KolmogorovSmirnovDistance for " + Arrays.toString(v1) + " and " + Arrays.toString(v2)
					+ " is " + dm.getDistance(v1, v2));
			System.out.println("KolmogorovSmirnovDistance for " + Arrays.toString(v2) + " and " + Arrays.toString(v1)
					+ " is " + dm.getDistance(v2, v1));

			System.out.println("KolmogorovSmirnovDistance for " + Arrays.toString(v1) + " and " + Arrays.toString(v3)
					+ " is " + dm.getDistance(v1, v3));
			System.out.println("KolmogorovSmirnovDistance for " + Arrays.toString(v3) + " and " + Arrays.toString(v1)
					+ " is " + dm.getDistance(v3, v1));

			System.out.println("KolmogorovSmirnovDistance for " + Arrays.toString(v2) + " and " + Arrays.toString(v3)
					+ " is " + dm.getDistance(v2, v3));
			System.out.println("KolmogorovSmirnovDistance for " + Arrays.toString(v3) + " and " + Arrays.toString(v2)
					+ " is " + dm.getDistance(v3, v2));
		}
	}

	private static double[] createTriangleProbabilityDecreasing(int i) {
		double valueQuant = 1;
		if (i > 1)
			valueQuant = 1.0 / (i * (i + 1) / 2.0);

		double[] v = new double[i];
		for (int dim = 0; dim < i; dim++)
			v[dim] = (i - dim) * valueQuant;

		return v;
	}

	private static double[] createTriangleProbabilityIncreasing(int i) {
		double valueQuant = 1;
		if (i > 1)
			valueQuant = 1.0 / (i * (i + 1) / 2.0);

		double[] v = new double[i];
		for (int dim = 0; dim < i; dim++)
			v[dim] = (dim + 1) * valueQuant;

		return v;
	}

	private static double[] createRandomProbabilities(int i) {
		double[] v = new double[i];
		for (int dim = 0; dim < i; dim++)
			v[dim] = Math.random() / i * 2;

		// normalize to 1.0
		double sum = 0;
		for (int dim = 0; dim < i; dim++)
			sum += v[dim];
		for (int dim = 0; dim < i; dim++)
			v[dim] = v[dim] / sum;

		return v;
	}
}
