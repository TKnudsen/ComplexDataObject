package com.github.TKnudsen.ComplexDataObject.model.functions.scaling;

import java.util.List;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

/**
 * <p>
 * Title: ScalingFunction
 * </p>
 * 
 * <p>
 * Description: scales a value into the interval [0...1]. Scaling can be based
 * on a given value interval [global min max] or based on a given value
 * distribution.
 * 
 * Scaling itself will be implemented in inheriting classes.
 * 
 * Setting new bounds is posible (even if previously calculated values will be
 * obsolete then). Reason: keeping the instance alife in value-dynamic contexts.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2018
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public abstract class ScalingFunction implements Function<Number, Number> {

	private Number globalMin = Double.NaN;
	private Number globalMax = Double.NaN;

	private boolean limitToBounds = false;

	public ScalingFunction(List<Number> values) {
		this(new StatisticsSupport(values));
	}

	public ScalingFunction(List<Number> values, boolean limitToBounds) {
		this(new StatisticsSupport(values), limitToBounds);
	}

	public ScalingFunction(StatisticsSupport statisticsSupport) {
		this(statisticsSupport.getMin(), statisticsSupport.getMax());
	}

	public ScalingFunction(StatisticsSupport statisticsSupport, boolean limitToBounds) {
		this(statisticsSupport.getMin(), statisticsSupport.getMax(), limitToBounds);
	}

	public ScalingFunction(Number globalMin, Number globalMax) {
		this(globalMin, globalMax, false);
	}

	public ScalingFunction(Number globalMin, Number globalMax, boolean limitToBounds) {
		this.globalMin = globalMin;
		this.globalMax = globalMax;

		this.limitToBounds = limitToBounds;

		checkValueIntegrity();
	}

	private void checkValueIntegrity() {
		if (globalMin == null || Double.isNaN(globalMin.doubleValue()))
			throw new IllegalArgumentException("ScalingFunction: lower bound ill-defined.");

		if (globalMax == null || Double.isNaN(globalMax.doubleValue()))
			throw new IllegalArgumentException("ScalingFunction: upper bound ill-defined.");
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;

		if (!(o.getClass().isAssignableFrom(ScalingFunction.class)))
			return false;

		ScalingFunction other = (ScalingFunction) o;

		if (other.globalMin == globalMin && other.globalMax == globalMax)
			return true;

		return false;
	}

	public Number getGlobalMin() {
		return globalMin;
	}

	public void setGlobalMin(Number globalMin) {
		this.globalMin = globalMin;

		checkValueIntegrity();
	}

	public Number getGlobalMax() {
		return globalMax;
	}

	public void setGlobalMax(Number globalMax) {
		this.globalMax = globalMax;

		checkValueIntegrity();
	}

	public boolean isLimitToBounds() {
		return limitToBounds;
	}

	public void setLimitToBounds(boolean limitToBounds) {
		this.limitToBounds = limitToBounds;
	}

}
