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
 * Copyright: Copyright (c) 2016-2017
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.01
 */
public class SimpsonsDiversityIndexTest {

	public static void main(String args[]) {

		double[] probability = new double[5];
		probability[0] = 0.3;
		probability[1] = 0.2;
		probability[2] = 0.2;
		probability[3] = 0.2;
		probability[4] = 0.0;

		int[] distribution = SimpsonsIndex.transformToIntDistribution(probability);

		System.out.println("Simpsons Diversity Index: " + SimpsonsIndex.calculateSimpsonsIndexOfDiversity(distribution));
	}

}
