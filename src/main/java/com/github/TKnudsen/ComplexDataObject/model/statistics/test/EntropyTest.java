package com.github.TKnudsen.ComplexDataObject.model.statistics.test;

import java.util.ArrayList;
import java.util.Collection;

import com.github.TKnudsen.ComplexDataObject.model.statistics.Entropy;
import com.github.TKnudsen.ComplexDataObject.model.statistics.SimpsonsIndex;

/**
 * <p>
 * Copyright: Copyright (c) 2016-2019
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.01
 */
public class EntropyTest {

	public static void main(String args[]) {

		// you should use integers. iff the index is use for double values this
		// is how the index can be addressed.

		Collection<Double> distribution = new ArrayList<Double>();
		distribution.add(0.3);
		distribution.add(0.2);
		distribution.add(0.2);
		distribution.add(0.2);
		distribution.add(0.1);

		System.out.println("Entropy: " + Entropy.calculateEntropy(distribution));
	}

}
