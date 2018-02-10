package com.github.TKnudsen.ComplexDataObject.model.functions.scaling;

import java.util.List;

import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

/**
 * <p>
 * Title: LogarithmicScalingFunction
 * </p>
 * 
 * <p>
 * Description: scales a value into the interval [0...1]. Scaling can be based
 * on a given value interval [global min max] or based on a given value
 * distribution.
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
public class LogarithmicScalingFunction extends ScalingFunction {

	public LogarithmicScalingFunction(List<Number> values) {
		super(values);
	}

	public LogarithmicScalingFunction(List<Number> values, boolean limitToBounds) {
		super(values, limitToBounds);
	}

	public LogarithmicScalingFunction(StatisticsSupport statisticsSupport) {
		super(statisticsSupport);
	}

	public LogarithmicScalingFunction(StatisticsSupport statisticsSupport, boolean limitToBounds) {
		super(statisticsSupport, limitToBounds);
	}

	public LogarithmicScalingFunction(Number globalMin, Number globalMax) {
		super(globalMin, globalMax);
	}

	public LogarithmicScalingFunction(Number globalMin, Number globalMax, boolean limitToBounds) {
		super(globalMin, globalMax, limitToBounds);
	}

	@Override
	public Number apply(Number value) {
		double scaled = MathFunctions.logarithmicScale(getGlobalMin().doubleValue(), getGlobalMax().doubleValue(),
				value.doubleValue(), isLimitToBounds());

		return new Double(scaled);
	}
}
