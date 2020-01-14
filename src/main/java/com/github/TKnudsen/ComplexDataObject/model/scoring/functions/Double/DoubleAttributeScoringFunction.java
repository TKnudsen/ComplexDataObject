package com.github.TKnudsen.ComplexDataObject.model.scoring.functions.Double;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.scoring.AttributeScoringFunctionChangeEvent;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.AttributeScoringFunction;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

public abstract class DoubleAttributeScoringFunction extends AttributeScoringFunction<Double> {

	private Double preFilterOutlierStd = 10.0;

	protected Double outlierStd = 1.96;
	protected Double outlierStdTop = 1.96;
	protected Double minOutlierPruning;
	protected Double maxOutlierPruning;

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
		Map<Long, Object> attributeValues = getContainer().getAttributeValues(getAttribute());

		Collection<Object> values = attributeValues.values();

		Collection<Double> doubleValues = new ArrayList<>();

		for (Object o : values) {
			Double value = getParser().apply(o);
			if (value != null && !Double.isNaN(value))
				doubleValues.add(value);
		}

		if (!isQuantileBased() && outlierStd != null && !Double.isNaN(outlierStd) && outlierStdTop != null
				&& !Double.isNaN(outlierStdTop))
			initializeOutlierTreatment(doubleValues);

		truncatedValueRate = 0;
		truncatedValueRateTop = 0;
		if (!isQuantileBased() && minOutlierPruning != null && maxOutlierPruning != null) {
			Collection<Double> afterO = new ArrayList<>();
			for (Double d : doubleValues) {
				double truncated = truncateOutlier(d);
				if (truncated != d) {
					truncatedValueRate++;
					if (d > maxOutlierPruning)
						truncatedValueRateTop++;
				}
				afterO.add(truncated);
			}
			doubleValues = afterO;
		}

		truncatedValueRate /= doubleValues.size();
		truncatedValueRateTop /= doubleValues.size();

		initializeStatisticsSupport(doubleValues);

		initializeNormalizationFunctions();

		scoreAverageWithoutMissingValues = calculateAverageScore();

		if (Double.isNaN(scoreAverageWithoutMissingValues))
			System.err.println(
					this.getClass().getSimpleName() + ": NaN value detected for the scoreAverageWithoutMissingValues!");

		Double missingValueAvgScoreRatio = getMissingValueAvgScoreRatio();
		if (missingValueAvgScoreRatio == null || Double.isNaN(missingValueAvgScoreRatio))
			scoreForMissingObjects = scoreAverageWithoutMissingValues * 0.5;
		else
			scoreForMissingObjects = scoreAverageWithoutMissingValues * missingValueAvgScoreRatio;
	}

	protected double calculateAverageScore() {
		double score = AttributeScoringFunction.calculateAverageScoreWithoutMissingValues(this, false);

		if (Double.isNaN(score))
			System.err.println(this.getClass().getSimpleName() + ": NaN value detected for the average score!");

		return score;
	}

	/**
	 * treatment of anomalies is performed across the entire value domain with one
	 * statistics distribution instance. no differentiation between positive and
	 * negative here.
	 * 
	 * given double values must not be null or NaN!
	 */
	protected final void initializeOutlierTreatment(Collection<Double> doubleValues) {

		// two-step process. first: remove crappy values from distribution.
		StatisticsSupport statisticsSupport = new StatisticsSupport(doubleValues);
		double mean = statisticsSupport.getMean();
		double standardDeviation = statisticsSupport.getStandardDeviation();

		if (mean - 10.0 * standardDeviation > statisticsSupport.getMin()
				|| mean + 10.0 * standardDeviation < statisticsSupport.getMax()) {

			Collection<Double> clampedValues = new ArrayList<>();

			for (Double d : doubleValues)
				if (mean - preFilterOutlierStd * standardDeviation < d
						&& mean + preFilterOutlierStd * standardDeviation > d)
					clampedValues.add(d);

			statisticsSupport = new StatisticsSupport(clampedValues);
		}

		// two-step process. second: calculate statistics with remaining values and
		// define min and max crop level

		mean = statisticsSupport.getMean();
		standardDeviation = statisticsSupport.getStandardDeviation();
		double min = statisticsSupport.getMin();
		double max = statisticsSupport.getMax();

		minOutlierPruning = Math.max(min, mean - outlierStd * standardDeviation);
//		maxOutlierPruning = Math.min(max, mean + outlierStd * standardDeviation);
		maxOutlierPruning = Math.min(max, mean + outlierStdTop * standardDeviation);
	}

	/**
	 * given double values must not be null or NaN!
	 */
	protected abstract void initializeStatisticsSupport(Collection<Double> doubleValues);

	protected abstract void initializeNormalizationFunctions();

	private final double truncateOutlier(double value) {
		if (minOutlierPruning == null || maxOutlierPruning == null)
			return value;

		return Math.max(minOutlierPruning, Math.min(value, maxOutlierPruning));
	}

	/**
	 * given double values must not be null or NaN!
	 */
	protected abstract double normalize(double value);

	@Override
	public Double applyValue(Double value) {

		if (value == null || Double.isNaN(value))
			return getScoreForMissingObjects();

		Double v = value;

		// pruning outliers: non need since normalization will crop extremes
		double output = normalize(v);

		if (!isHighIsGood())
			output = invertScore(output);

		// decision: weight should be applied externally. Thus, the relative value
		// domain is preserved and guaranteed internally.
		return output; // * getWeight();
	}

	protected abstract double invertScore(double output);

	@Override
	public double getAverageScoreWithoutMissingValues() {
		return scoreAverageWithoutMissingValues;
	}

	public abstract StatisticsSupport getStatisticsSupport();

	public double getOutlierStd() {
		return outlierStd;
	}

	public void setOutlierStd(double outlierStd) {
		this.outlierStd = outlierStd;
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
		this.scoresBuffer = new HashMap<>();

		refreshScoringFunction();

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, getAttribute(), this);

		notifyListeners(event);
	}

}
