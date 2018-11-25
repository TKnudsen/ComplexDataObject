package com.github.TKnudsen.ComplexDataObject.data.uncertainty.distribution;

import com.github.TKnudsen.ComplexDataObject.data.uncertainty.range.IValueUncertaintyRange;

/**
 * <p>
 * * Representation of a distribution of uncertainties for a single value. It is
 * the statistical summary or accumulation of several possible uncertainties. It
 * can roughly be imagined as describing the probabilities of the possible
 * deviations of the uncertain value to the actual value: For a given uncertain
 * value <code>v</code>, the <code>(lower,mean,upper)</code> elements of an
 * (absolute) distribution indicate that the actual value will never be smaller
 * than <code>(v+lower)</code> and never be greater than <code>(v+upper)</code>,
 * and "on average", the actual value will be <code>(v+mean)</code>
 * </p>
 * 
 * <p>
 * Copyright: (c) 2015-2018 Juergen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.05
 */
public interface IValueUncertaintyDistribution extends IValueUncertaintyRange {

	double getUpperQartile();

	default double getMean() {
		return getAmount();
	}

	double getMedian();

	double getLowerQuartie();

	double getVariance();

	double getStandardDeviation();
	
	int size();

}