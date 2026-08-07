package com.github.TKnudsen.ComplexDataObject.model.statistics.test;

import java.util.ArrayList;
import java.util.Collection;

import com.github.TKnudsen.ComplexDataObject.model.statistics.Entropy;

/**
 * <p>
 * Simple demo/test class that computes and prints the entropy of an example
 * probability distribution using the Entropy utility class.
 * </p>
 *
 * @version 1.01
 * @since 2016
 */
public class EntropyTest {

	public static void main(String args[]) {

		Collection<Double> distribution = new ArrayList<Double>();
		distribution.add(0.3);
		distribution.add(0.2);
		distribution.add(0.2);
		distribution.add(0.2);
		distribution.add(0.2);

		System.out.println("Entropy: " + Entropy.calculateEntropy(distribution));
	}

}
