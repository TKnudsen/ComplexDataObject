package com.github.TKnudsen.ComplexDataObject.data.uncertainty.Double;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

import java.util.Collection;

/**
 * <p>
 * Title: ValueUncertainty
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
public class ValueUncertainty extends ComplexDataObject implements IValueUncertainty<Double> {

	private Double representant;
	private double variation;

	public ValueUncertainty(double representant) {
		this(representant, 0.0);
	}

	public ValueUncertainty(double representant, double variation) {
		this.representant = representant;
		this.variation = variation;
	}

	public ValueUncertainty(Collection<? extends Double> values) {
		initialize(values);
	}

	public ValueUncertainty() {
		super();
	}

	private void initialize(Collection<? extends Double> values) {
		StatisticsSupport statisticsSupport = new StatisticsSupport(values);

		this.representant = statisticsSupport.getMedian();
		this.variation = statisticsSupport.getVariance();
	}

	@Override
	public String toString() {
		return "ValueUncertainty. Representant: " + representant + ", " + variation;
	}

	@Override
	public Double getUncertainty() {
		return representant;
	}

	@Override
	public Double getVariation() {
		return variation;
	}

}
