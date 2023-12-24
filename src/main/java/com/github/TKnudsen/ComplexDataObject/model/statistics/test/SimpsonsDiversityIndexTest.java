package com.github.TKnudsen.ComplexDataObject.model.statistics.test;

import com.github.TKnudsen.ComplexDataObject.model.statistics.SimpsonsIndex;

/**
 * <p>
 * Title: SimpsonsDiversityIndexTest
 * </p>
 *
 * <p>
 * Description:
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2016-2023
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.02
 */
public class SimpsonsDiversityIndexTest {

	public static void main(String args[]) {
		test3();
	}

	private static void test1() {
		int[] distribution = new int[] { 1, 2, 1, 1, 1 };
		System.out
				.println("Simpsons Diversity Index: " + SimpsonsIndex.calculateSimpsonsIndexOfDiversity(distribution));
	}

	private static void test2() {
		// you should use integers. iff the index is use for double values this
		// is how the index can be addressed.
		double[] probability = new double[5];
		probability[0] = 0.3;
		probability[1] = 0.2;
		probability[2] = 0.2;
		probability[3] = 0.2;
		probability[4] = 0.1;

		int[] distribution = SimpsonsIndex.transformToIntDistribution(probability);

		System.out
				.println("Simpsons Diversity Index: " + SimpsonsIndex.calculateSimpsonsIndexOfDiversity(distribution));
	}

	private static void test3() {
		// you should use integers. iff the index is use for double values this
		// is how the index can be addressed.
		double[] probability = new double[5];
		probability[0] = 0.0;
		probability[1] = 0.0;
		probability[2] = 0.0;
		probability[3] = 0.0;
		probability[4] = 100.0;

		int[] distribution = SimpsonsIndex.transformToIntDistribution(probability);

		System.out
				.println("Simpsons Diversity Index: " + SimpsonsIndex.calculateSimpsonsIndexOfDiversity(distribution));
	}

}
