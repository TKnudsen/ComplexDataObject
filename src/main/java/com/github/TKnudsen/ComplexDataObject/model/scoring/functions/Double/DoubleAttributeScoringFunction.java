package com.github.TKnudsen.ComplexDataObject.model.scoring.functions.Double;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.scoring.AttributeScoringFunctionChangeEvent;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.AttributeScoringFunction;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.AttributeScoringFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

public abstract class DoubleAttributeScoringFunction extends AttributeScoringFunction<Double> {

	private Double preFilterOutlierStd = 10.0;

	/**
	 * outlier cropping level at the minimum side. The decision was made to go for
	 * 2.96 instead of 1.96 to avoid too much scoring truncation
	 */
	protected Double outlierStd = 2.96;

	/**
	 * outlier cropping level at the maximum side. The decision was made to go for
	 * 2.96 instead of 1.96 to avoid too much scoring truncation
	 */
	protected Double outlierStdTop = 2.96;

	protected Double outlierPruningMinValue;
	protected Double outlierPruningMaxValue;
	protected Double outlierPruningMinValueExternal;
	protected Double outlierPruningMaxValueExternal;

	/**
	 * if true, values that exactly match the neutral value will receive no quantile
	 * normalization. Values that exactly match the outlier pruning levels will
	 * receive the defined quantileNormalizationRate. every value in between
	 * receives a ratio that is interpolated, accordingly.
	 */
	private boolean linearTransitionOfQuantileNormalizationRates = true;

	/**
	 * for serialization purposes
	 */
	protected DoubleAttributeScoringFunction() {
		super();
	}

	public DoubleAttributeScoringFunction(ComplexDataContainer container, String attribute,
			IObjectParser<Double> parser) {
		this(container, parser, attribute, null, false, true, 1.0, null);
	}

	public DoubleAttributeScoringFunction(ComplexDataContainer container, IObjectParser<Double> parser,
			String attribute, String abbreviation, boolean quantileBased, boolean highIsGood, double weight) {
		this(container, parser, attribute, abbreviation, quantileBased, highIsGood, weight, null);
	}

	public DoubleAttributeScoringFunction(ComplexDataContainer container, IObjectParser<Double> parser,
			String attribute, String abbreviation, boolean quantileBased, boolean highIsGood, double weight,
			Function<ComplexDataObject, Double> uncertaintyFunction) {
		super(container, parser, attribute, abbreviation, quantileBased, highIsGood, weight, uncertaintyFunction);

		refreshScoringFunction();
	}

	@Override
	protected void refreshScoringFunction() {
		Collection<Object> values = getContainer().getAttributeValueCollection(getAttribute());

		Collection<Double> doubleValues = new ArrayList<>();

		for (Object o : values) {
			Double value = getParser().apply(o);
			if (value != null && !Double.isNaN(value))
				doubleValues.add(value);
		}

		// raw value statistics
		initializeRawValuesStatisticsSupport(doubleValues);

		doubleValues = clampValues(doubleValues, 10.0);
		initializeStdOutlierTreatment(doubleValues);

		truncatedValueRateBottom = 0;
		truncatedValueRateTop = 0;
		if (outlierPruningMinValue != null && outlierPruningMaxValue != null) {
			Collection<Double> afterO = new ArrayList<>();
			for (Double d : doubleValues) {
				double truncated = truncateOutlier(d);
				if (truncated != d) {
					if (d < outlierPruningMinValue)
						truncatedValueRateBottom++;
					if (d > outlierPruningMaxValue)
						truncatedValueRateTop++;
				}
				afterO.add(truncated);
			}
			doubleValues = afterO;
		}

		truncatedValueRateBottom /= doubleValues.size();
		truncatedValueRateTop /= doubleValues.size();

		// outlier-pruned value statistics
		initializeStatisticsSupport(doubleValues);

		initializeNormalizationFunctions();

		double scoreAverageWithoutMissingValues = AttributeScoringFunctions
				.calculateAverageScoreWithoutMissingValues(this, false);

		// determine internal missing value score
		if (Double.isNaN(getScoreForMissingObjectsExternal()))
			scoreForMissingObjects = scoreAverageWithoutMissingValues * 0.5;
		else
			scoreForMissingObjects = getScoreForMissingObjectsExternal();
	}

	private Collection<Double> clampValues(Collection<Double> doubleValues, double std) {
		Collection<Double> clampedValues = new ArrayList<>();
		for (Double d : doubleValues)
			if (d != null && !Double.isNaN(d))
				clampedValues.add(d);

		StatisticsSupport statisticsSupport = new StatisticsSupport(clampedValues);
		double mean = statisticsSupport.getMean();
		double standardDeviation = statisticsSupport.getStandardDeviation();

		if (mean - std * standardDeviation > statisticsSupport.getMin()
				|| mean + std * standardDeviation < statisticsSupport.getMax()) {

			Collection<Double> clamped = new ArrayList<>();

			for (Double d : clampedValues) {
				d = Math.max(d, mean - preFilterOutlierStd * standardDeviation);
				d = Math.min(d, mean + preFilterOutlierStd * standardDeviation);
				clamped.add(d);
			}

			clampedValues = clamped;
		}

		return clampedValues;
	}

	protected void initializeStdOutlierTreatment(Collection<Double> doubleValues) {

		Collection<Double> values = new ArrayList<>();
		for (Double d : doubleValues)
			if (d != null && !Double.isNaN(d))
				values.add(d);

		StatisticsSupport statisticsSupport = new StatisticsSupport(values);

		double mean = statisticsSupport.getMean();
		double standardDeviation = statisticsSupport.getStandardDeviation();
		double min = statisticsSupport.getMin();
		double max = statisticsSupport.getMax();

		double outlierStd = (this.outlierStd != null && !Double.isNaN(this.outlierStd)) ? this.outlierStd : 10.0;
		double outlierStdTop = (this.outlierStdTop != null && !Double.isNaN(this.outlierStdTop)) ? this.outlierStdTop
				: 10.0;

		outlierPruningMinValue = (outlierPruningMinValueExternal != null) ? outlierPruningMinValueExternal
				: Math.max(min, mean - outlierStd * standardDeviation);
		outlierPruningMaxValue = (outlierPruningMaxValueExternal != null) ? outlierPruningMaxValueExternal
				: Math.min(max, mean + outlierStdTop * standardDeviation);
	}

	/**
	 * given double values must not be null or NaN!
	 */
	protected abstract void initializeStatisticsSupport(Collection<Double> doubleValues);

	protected abstract void initializeRawValuesStatisticsSupport(Collection<Double> doubleValues);

	protected abstract void initializeNormalizationFunctions();

	public final boolean isLowerOutlier(double value) {
		if (outlierPruningMinValue == null)
			throw new NullPointerException(getClass().getSimpleName() + ": minOutlierPruning must not be null");

		if (value < outlierPruningMinValue)
			return true;
		return false;
	}

	public final boolean isUpperOutlier(double value) {
		if (outlierPruningMaxValue == null)
			throw new NullPointerException(getClass().getSimpleName() + ": maxOutlierPruning must not be null");

		if (value > outlierPruningMaxValue)
			return true;
		return false;
	}

	private final double truncateOutlier(double value) {
		if (outlierPruningMinValue == null || outlierPruningMaxValue == null)
			return value;

		return Math.max(outlierPruningMinValue, Math.min(value, outlierPruningMaxValue));
	}

	@Override
	protected double normalize(double value) {
		if (!linearTransitionOfQuantileNormalizationRates)
			return super.normalize(value);

		if (getQuantileNormalizationRate() == 0)
			return normalizeLinear(value);

		double dq = normalizeQuantiles(value);
		double dl = normalizeLinear(value);

		// this is a version with reduced effect and was used as default for a while
//		double localQuantileNormalizationRate = Math.abs(normalizeLinear(value)) * getQuantileNormalizationRate();
//		return (dq * localQuantileNormalizationRate + dl * (1 - localQuantileNormalizationRate));

		// this version has a strong effect, more what is expected
		return (dq * getQuantileNormalizationRate() + dl * (1 - getQuantileNormalizationRate()));
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Attribute Scoring Function:\t" + getClass().getSimpleName() + "\n");
		sb.append("Attribute:\t" + getAttribute() + "\n");
		sb.append("MinClamp:\t" + getOutlierPruningMinValue() + "\n");
		sb.append("MaxClamp:\t" + getOutlierPruningMaxValue() + "\n");
		sb.append("Missing Value Score: " + MathFunctions.round(getScoreForMissingObjects(), 3) + "\n");

		return sb.toString().trim();
	}

	public Double getOutlierStd() {
		return outlierStd;
	}

	public void setOutlierStd(Double outlierStd) {
		this.outlierStd = outlierStd;
		this.outlierPruningMinValueExternal = null;

		this.scoresBuffer = new HashMap<>();

		refreshScoringFunction();

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, getAttribute(), this);

		notifyListeners(event);
	}

	public Double getOutlierStdTop() {
		return outlierStdTop;
	}

	public void setOutlierStdTop(Double outlierStdTop) {
		this.outlierStdTop = outlierStdTop;
		this.outlierPruningMaxValueExternal = null;

		this.scoresBuffer = new HashMap<>();

		refreshScoringFunction();

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, getAttribute(), this);

		notifyListeners(event);
	}

	public boolean isLinearTransitionOfQuantileNormalizationRates() {
		return linearTransitionOfQuantileNormalizationRates;
	}

	public void setLinearTransitionOfQuantileNormalizationRates(boolean linearTransitionOfQuantileNormalizationRates) {
		this.linearTransitionOfQuantileNormalizationRates = linearTransitionOfQuantileNormalizationRates;
	}

	public Double getOutlierPruningMinValue() {
		if (outlierPruningMinValueExternal == null)
			return outlierPruningMinValue;
		else
			return outlierPruningMinValueExternal;
	}

	/**
	 * defines the lower boundary of the input value domain. Lower values will be
	 * clamped.
	 * 
	 * @param outlierPruningMinValue
	 */
	public void setOutlierPruningMinValue(Double outlierPruningMinValue) {
		this.outlierPruningMinValueExternal = outlierPruningMinValue;
		this.outlierStd = null;

		this.scoresBuffer = new HashMap<>();

		refreshScoringFunction();

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, getAttribute(), this);

		notifyListeners(event);
	}

	public Double getOutlierPruningMaxValue() {
		if (outlierPruningMaxValueExternal == null)
			return outlierPruningMaxValue;
		else
			return outlierPruningMaxValueExternal;
	}

	/**
	 * defines the upper boundary of the input value domain. Higher values will be
	 * clamped.
	 * 
	 * @param outlierPruningMaxValue
	 */
	public void setOutlierPruningMaxValue(Double outlierPruningMaxValue) {
		this.outlierPruningMaxValueExternal = outlierPruningMaxValue;
		this.outlierStdTop = null;

		this.scoresBuffer = new HashMap<>();

		refreshScoringFunction();

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, getAttribute(), this);

		notifyListeners(event);
	}

}
