package com.github.TKnudsen.ComplexDataObject.model.transformations.normalization;

import java.util.Collection;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

/**
 * <p>
 * Title: ZScoreNormalizationFunction
 * </p>
 * 
 * <p>
 * Description: scales a value by subtracting the mean (predefined) and by
 * division through the standard deviation (predefined). Characteristics of an
 * output distribution: mean is equals 0.0 and standard deviation is equals 1.0.
 * </p>
 * 
 * <p>
 * Returns NaN if the predetermined standard deviation is zero.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2022
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.01
 */
public class ZScoreNormalizationFunction implements Function<Number, Number> {

	private Number mean = Double.NaN;
	private Number std = Double.NaN;

	/**
	 * for serialization purposes only
	 */
	protected ZScoreNormalizationFunction() {

	}

	public ZScoreNormalizationFunction(Collection<? extends Number> values) {
		this(new StatisticsSupport(values));
	}

	public ZScoreNormalizationFunction(StatisticsSupport statisticsSupport) {
		this(statisticsSupport.getMean(), statisticsSupport.getStandardDeviation());
	}

	public ZScoreNormalizationFunction(Number mean, Number std) {
		this.mean = mean;
		this.std = std;

		checkValueIntegrity();
	}

	private void checkValueIntegrity() {
		if (mean == null || Double.isNaN(mean.doubleValue()))
			throw new IllegalArgumentException(
					getClass().getSimpleName() + ": mean ill-defined (value is " + mean + ")");

		if (std == null || Double.isNaN(std.doubleValue()))
			throw new IllegalArgumentException(getClass().getSimpleName() + ": std ill-defined (value is " + std + ")");
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;

		if (!(o.getClass().isAssignableFrom(ZScoreNormalizationFunction.class)))
			return false;

		ZScoreNormalizationFunction other = (ZScoreNormalizationFunction) o;

		if (other.mean == mean && other.std == std)
			return true;

		return false;
	}

	@Override
	public Number apply(Number t) {
		if (std.doubleValue() == 0.0)
			return t.doubleValue() - mean.doubleValue();

		return (t.doubleValue() - mean.doubleValue()) / std.doubleValue();
	}

}
