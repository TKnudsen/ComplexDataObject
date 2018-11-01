package com.github.TKnudsen.ComplexDataObject.data.uncertainty.distribution;

import java.util.Collection;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

/**
 * <p>
 * Title: NumericalDistributionUncertainty
 * </p>
 * 
 * <p>
 * Description: data model for uncertainties of numerical values.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2015-2018
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class NumericalDistributionUncertainty extends ComplexDataObject
		implements IValueDistributionUncertainty<Double> {

	private Double min;
	private Double median;
	private Double max;
	private Double representant;
	private double variation;

	public NumericalDistributionUncertainty(Collection<? extends Double> values) {
		initialize(values);
	}

	public NumericalDistributionUncertainty() {
		super();
	}

	private void initialize(Collection<? extends Double> values) {
		StatisticsSupport statisticsSupport = new StatisticsSupport(values);

		this.min = statisticsSupport.getMin();
		this.median = statisticsSupport.getMedian();
		this.max = statisticsSupport.getMax();
		this.representant = statisticsSupport.getMedian();
		this.variation = statisticsSupport.getVariance();
	}

	@Override
	public String toString() {
		return "NumericalUncertainty. min, max, representant, variation: " + min + ", " + max + ", " + representant
				+ ", " + variation;
	}

	@Override
	public Double getUncertainty() {
		return representant;
	}

	@Override
	public Double getUncertaintyMinimum() {
		return min;
	}

	@Override
	public Double getUncertaintyMaximum() {
		return max;
	}

	@Override
	public Double getVariation() {
		return variation;
	}

	@Override
	public Double getUncertaintyMedian() {
		return median;
	}

}
