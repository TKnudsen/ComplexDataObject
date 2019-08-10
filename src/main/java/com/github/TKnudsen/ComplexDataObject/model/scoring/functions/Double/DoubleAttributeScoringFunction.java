package com.github.TKnudsen.ComplexDataObject.model.scoring.functions.Double;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.scoring.AttributeScoringChangeEvent;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.AttributeScoringFunction;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

public abstract class DoubleAttributeScoringFunction extends AttributeScoringFunction<Double> {

	protected Double outlierStd = 2.96;
	protected Double minOutlierPruning;
	protected Double maxOutlierPruning;

	protected double scoreAverageWithoutMissingValues = 0.0;

	/**
	 * for serialization purposes
	 */
	protected DoubleAttributeScoringFunction() {
		super();
	}

	public DoubleAttributeScoringFunction(ComplexDataContainer container, String attribute) {
		this(container, new DoubleParser(), attribute, null, false, true, 1.0, null);
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

		if (!isQuantileBased() && outlierStd != null && !Double.isNaN(outlierStd))
			initializeOutlierTreatment(doubleValues);

		if (!isQuantileBased() && minOutlierPruning != null && maxOutlierPruning != null) {
			Collection<Double> afterO = new ArrayList<>();
			for (Double d : doubleValues)
				afterO.add(pruneOutlier(d));
			doubleValues = afterO;
		}

		initializeStatisticsSupport(doubleValues);

		initializeNormalizationFunctions();

		scoreAverageWithoutMissingValues = AttributeScoringFunction.calculateAverageScoreWithoutMissingValues(this);
	}

	/**
	 * treatment of anomalies is performed across the entire value domain with one
	 * statistics distribution instance. no differentiation between positive and
	 * negative here.
	 * 
	 * given double values must not be null or NaN!
	 */
	protected final void initializeOutlierTreatment(Collection<Double> doubleValues) {

		StatisticsSupport statisticsSupport = new StatisticsSupport(doubleValues);

		double mean = statisticsSupport.getMean();
		double standardDeviation = statisticsSupport.getStandardDeviation();
		double min = statisticsSupport.getMin();
		double max = statisticsSupport.getMax();

		minOutlierPruning = Math.max(min, mean - outlierStd * standardDeviation);
		maxOutlierPruning = Math.min(max, mean + outlierStd * standardDeviation);
	}

	/**
	 * given double values must not be null or NaN!
	 */
	protected abstract void initializeStatisticsSupport(Collection<Double> doubleValues);

	protected abstract void initializeNormalizationFunctions();

	private final double pruneOutlier(double value) {
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
		if (value == null || Double.isNaN(value)) {
			Double v = getScoreForMissingObjects();
			if (v == null)
				return scoreAverageWithoutMissingValues * 0.5;
		}

		Double v = value;

		// pruning outliers is not necessary as the normalization function will crop
		// extreme values.
		double output = normalize(v);

		if (!isHighIsGood())
			output = 1 - output;

		// decision: weight should be applied externally. Thus, the relative value
		// domain is preserved and guaranteed internally.
		return output; // * getWeight();
	}

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

		refreshScoringFunction();

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, getAttribute(), this);

		notifyListeners(event);
	}

}
