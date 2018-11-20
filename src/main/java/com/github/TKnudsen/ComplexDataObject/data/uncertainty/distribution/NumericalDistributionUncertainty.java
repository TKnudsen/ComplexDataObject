package com.github.TKnudsen.ComplexDataObject.data.uncertainty.distribution;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

import java.util.Collection;

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
 * @version 1.05
 */
public class NumericalDistributionUncertainty extends ComplexDataObject
		implements IValueDistributionUncertainty<Double> {

	private Double min;
	private Double median;
	private Double max;
	private Double representant;
	private double variation;

	private Collection<? extends Double> values;

	@SuppressWarnings("unused")
	private NumericalDistributionUncertainty() {
		super();
	}

	public NumericalDistributionUncertainty(Collection<? extends Double> values) {
		initialize(values);

		this.values = values;
	}

	private void initialize(Collection<? extends Double> values) {
		StatisticsSupport statisticsSupport = new StatisticsSupport(values);

		this.min = statisticsSupport.getMin();
		this.median = statisticsSupport.getMedian();
		this.max = statisticsSupport.getMax();
		this.representant = statisticsSupport.getMedian();
		this.variation = statisticsSupport.getVariance();
		
		this.values = values;
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

	public Collection<? extends Double> getValues() {
		return values;
	}

}
