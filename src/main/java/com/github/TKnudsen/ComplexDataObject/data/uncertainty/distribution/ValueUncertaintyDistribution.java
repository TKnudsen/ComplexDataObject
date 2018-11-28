package com.github.TKnudsen.ComplexDataObject.data.uncertainty.distribution;

import java.util.Collection;

import com.github.TKnudsen.ComplexDataObject.data.uncertainty.range.ValueUncertaintyRange;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

/**
 * <p>
 * Data model for uncertainties of numerical values characterized by a
 * statistical distribution.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2015-2018 Juergen Bernard,
 * https://github.com/TKnudsen/ComplexDataObject
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.07
 */
public class ValueUncertaintyDistribution extends ValueUncertaintyRange implements IValueUncertaintyDistribution {

	private double upperQartile;

	private double median;

	private double lowerQuartile;

	private double variance;

	private double standardDeviation;

	int size;

	@SuppressWarnings("unused")
	private ValueUncertaintyDistribution() {
		super();
	}

	public ValueUncertaintyDistribution(Collection<? extends Double> values) {
		super();

		initialize(values);
	}

	private void initialize(Collection<? extends Double> values) {
		StatisticsSupport statisticsSupport = new StatisticsSupport(values);

		this.upperQartile = statisticsSupport.getPercentile(75);
		this.median = statisticsSupport.getMedian();
		this.lowerQuartile = statisticsSupport.getPercentile(25);
		this.variance = statisticsSupport.getVariance();
		this.standardDeviation = statisticsSupport.getStandardDeviation();

		this.size = values.size();
	}

	@Override
	public String toString() {
		return "ValueUncertaintyDistribution. Amount: " + getAmount() + ", bounds: [" + getLowerBound() + ", "
				+ getUpperBound() + "]";
	}

	@Override
	public double getUpperQartile() {
		return upperQartile;
	}

	@Override
	public double getMedian() {
		return median;
	}

	@Override
	public double getLowerQuartile() {
		return lowerQuartile;
	}

	@Override
	public double getVariance() {
		return variance;
	}

	@Override
	public double getStandardDeviation() {
		return standardDeviation;
	}

	@Override
	public int size() {
		return size;
	}

}
