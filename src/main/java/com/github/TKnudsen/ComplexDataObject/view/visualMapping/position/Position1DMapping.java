package com.github.TKnudsen.ComplexDataObject.view.visualMapping.position;

import com.github.TKnudsen.ComplexDataObject.model.functions.scaling.LinearScalingFunction;
import com.github.TKnudsen.ComplexDataObject.model.functions.scaling.LogarithmicScalingFunction;
import com.github.TKnudsen.ComplexDataObject.model.functions.scaling.ScalingFunction;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.view.visualMapping.VisualMappingFunction;

/**
 * <p>
 * Title: Position1DMapping
 * </p>
 * 
 * <p>
 * Description: maps a numeric value domain into pixel coordinates (1D position
 * information)
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2016-2018
 * </p>
 * 
 * @author Juergen Bernard
 */
public final class Position1DMapping extends VisualMappingFunction<Number, Double> {

	private Number minValue;
	private Number maxValue;
	private Double minPixel;
	private Double maxPixel;

	private boolean logarithmicScale = false;

	private ScalingFunction scalingFunction;

	public Position1DMapping(Number minValue, Number maxValue, Double minPixel, Double maxPixel) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.minPixel = minPixel;
		this.maxPixel = maxPixel;

		initializeScalingFunction();
	}

	private void initializeScalingFunction() {
		if (!logarithmicScale)
			this.scalingFunction = new LinearScalingFunction(minValue, maxValue, false);
		else
			this.scalingFunction = new LogarithmicScalingFunction(minValue, maxValue, false);
	}

	@Override
	protected Double calculateMapping(Number value) {
		// double linearScaleOld = MathFunctions.linearScale(minValue.doubleValue(),
		// maxValue.doubleValue(),
		// value.doubleValue(), true);
		Number scale = scalingFunction.apply(value);
		return minPixel + (maxPixel - minPixel) * scale.doubleValue();
	}

	public Number inverseMapping(Double visual) {
		double scale = Double.NaN;
		if (!logarithmicScale)
			scale = MathFunctions.linearScale(minPixel, maxPixel, visual, false);
		else
			scale = MathFunctions.logarithmicScale(minPixel, maxPixel, visual, false);
		return minValue.doubleValue() + (maxValue.doubleValue() - minValue.doubleValue()) * scale;
	}

	public Number getMinValue() {
		return minValue;
	}

	public void setMinValue(Number minValue) {
		this.minValue = minValue;
		this.scalingFunction.setGlobalMin(minValue);

		resetMappingLookup();
	}

	public Number getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Number maxValue) {
		this.maxValue = maxValue;
		this.scalingFunction.setGlobalMax(maxValue);

		resetMappingLookup();
	}

	public boolean isLogarithmicScale() {
		return logarithmicScale;
	}

	public void setLogarithmicScale(boolean logarithmicScale) {
		this.logarithmicScale = logarithmicScale;

		initializeScalingFunction();
	}
}
