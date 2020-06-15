package com.github.TKnudsen.ComplexDataObject.model.scoring.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.scoring.AttributeScoringFunctionChangeEvent;
import com.github.TKnudsen.ComplexDataObject.model.scoring.AttributeScoringFunctionChangeListener;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

public abstract class AttributeScoringFunction<T> implements Function<ComplexDataObject, Double> {

	private IObjectParser<T> parser;

	@JsonIgnore
	private ComplexDataContainer container;

	private String attribute;
	private String abbreviation;

	@Deprecated
	/**
	 * remove this field, use quantileNormalizationRate = 1.0 instead
	 */
	protected boolean quantileBased;

	/**
	 * rate to which the quantile normalization shall be used
	 */
	private double quantileNormalizationRate = 0.0;

	private boolean highIsGood;
	private double weight;

	protected double scoreAverageWithoutMissingValues;

	protected double truncatedValueRate;
	protected double truncatedValueRateTop;

	@JsonIgnore
	private Function<ComplexDataObject, Double> uncertaintyFunction = null;

	private UncertaintyConsideration uncertaintyConsideration = UncertaintyConsideration.Full;

	public enum UncertaintyConsideration {
		Full, Half, None
	}

	protected double scoreForMissingObjects = 0.2;
	private Double missingValueAvgScoreRatio = null;

	@JsonIgnore
	private List<AttributeScoringFunctionChangeListener> listeners = new ArrayList<AttributeScoringFunctionChangeListener>();

	/**
	 * can be used to improve calculation time by the cost of adding a state to the
	 * function that has to be maintained carefully
	 */
	@JsonIgnore
	protected Map<ComplexDataObject, Double> scoresBuffer = new HashMap<>();

	/**
	 * for serialization purposes
	 */
	protected AttributeScoringFunction() {

	}

	public AttributeScoringFunction(ComplexDataContainer container, IObjectParser<T> parser, String attribute) {
		this(container, parser, attribute, null, false, true, 1.0);
	}

	public AttributeScoringFunction(ComplexDataContainer container, IObjectParser<T> parser, String attribute,
			String abbreviation, boolean quantileBased, boolean highIsGood, double weight) {

		this(container, parser, attribute, abbreviation, quantileBased, highIsGood, weight, null);
	}

	public AttributeScoringFunction(ComplexDataContainer container, IObjectParser<T> parser, String attribute,
			String abbreviation, boolean quantileBased, boolean highIsGood, double weight,
			Function<ComplexDataObject, Double> uncertaintyFunction) {

		this.container = container;
		this.parser = parser;
		this.attribute = attribute;
		if (abbreviation != null)
			this.abbreviation = abbreviation;
		else
			this.abbreviation = StringUtils.abbreviate(attribute, 10);
		this.setQuantileBased(quantileBased);
//		this.quantileBased = quantileBased;
		this.highIsGood = highIsGood;
		this.weight = weight;

		this.uncertaintyFunction = uncertaintyFunction;
	}

	protected abstract void refreshScoringFunction();

	public Double applyValue(T value) {
		return applyDoubleValue(toDouble(value));
	}

	protected abstract Double toDouble(T t);

	public Double applyDoubleValue(Double value) {

		if (value == null || Double.isNaN(value))
			return getScoreForMissingObjects();

		Double v = value;

		// outlier treatment: no need since normalization will crop extremes
		double output = normalize(v);

		if (!isHighIsGood())
			output = invertScore(output);

		// decision: weight should be applied externally. Thus, the relative value
		// domain is preserved and guaranteed internally.
		return output; // * getWeight();
	}

	protected abstract double invertScore(double output);

	@Override
	public final Double apply(ComplexDataObject cdo) {

		Double buffered = scoresBuffer.get(cdo);
		if (buffered != null)
			return buffered;

		double missingValueValue = getScoreForMissingObjects();

		// check for missing attribute (never a good sign)
		if (!cdo.keySet().contains(attribute)) {
			if (Double.isNaN(missingValueValue))
				System.err.println(this.getClass().getSimpleName() + ": NaN value inserted in the scoresBuffer!");
			// no buffering
			// scoresBuffer.put(cdo, missingValueValue);
			return missingValueValue;
		}

		Object o = cdo.getAttribute(attribute);

		// check for missing value
		if (o == null) {
			if (Double.isNaN(missingValueValue))
				System.err.println(this.getClass().getSimpleName() + ": NaN value inserted in the scoresBuffer!");
			scoresBuffer.put(cdo, missingValueValue);
			return missingValueValue;
		}

		T value = parser.apply(o);

		double s = applyValue(value);

		// uncertainty information. Considering uncertainty information may result in a
		// scoring which does not reach 1.0 for all objects
		if (uncertaintyConsideration != UncertaintyConsideration.None)
			if (getUncertaintyFunction() != null) {
				Double u = getUncertaintyFunction().apply(cdo);

				if (u == null || Double.isNaN(u))
					u = missingValueValue;

				u = Math.max(0.0, Math.min(1.0, u));

				double sFinal = s * (1 - u);
				if (uncertaintyConsideration.equals(UncertaintyConsideration.Half))
					sFinal = s * (1 - u * 0.5);

				if (Double.isNaN(sFinal))
					System.err.println(this.getClass().getSimpleName() + ": NaN value inserted in the scoresBuffer!");
				scoresBuffer.put(cdo, sFinal);
				return sFinal;
			}

		if (Double.isNaN(s))
			System.err.println(this.getClass().getSimpleName() + ": NaN value inserted in the scoresBuffer!");
		scoresBuffer.put(cdo, s);
		return s;
	}

	protected double normalize(double value) {
		if (getQuantileNormalizationRate() == 0)
			return normalizeLinear(value);

		double dq = normalizeQuantiles(value);
		double dl = normalizeLinear(value);

		return (dq * getQuantileNormalizationRate() + dl * (1 - getQuantileNormalizationRate()));
	}

	protected abstract double normalizeLinear(double value);

	protected abstract double normalizeQuantiles(double value);

	public void addAttributeScoringChangeListener(AttributeScoringFunctionChangeListener listener) {
		listeners.remove(listener);
		listeners.add(listener);
	}

	public void addAttributeScoringChangeListener(int position, AttributeScoringFunctionChangeListener listener) {
		listeners.remove(listener);
		listeners.add(position, listener);
	}

	public void removeAttributeScoringChangeListener(AttributeScoringFunctionChangeListener listener) {
		listeners.remove(listener);
	}

	protected final void notifyListeners(AttributeScoringFunctionChangeEvent event) {
		for (AttributeScoringFunctionChangeListener listener : listeners)
			listener.attributeScoringFunctionChanged(event);
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public final void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public boolean isHighIsGood() {
		return highIsGood;
	}

	public final void setHighIsGood(boolean highIsGood) {
		this.highIsGood = highIsGood;
		this.scoresBuffer = new HashMap<>();

		refreshScoringFunction();

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	@Deprecated
	/**
	 * replaced by a continuous way to integrate quantile normalization
	 * 
	 * @return
	 */
	public boolean isQuantileBased() {
		return (this.quantileNormalizationRate == 1.0);
	}

	@Deprecated
	/**
	 * replaced by a continuous way to integrate quantile normalization
	 * 
	 * @param quantileBased
	 */
	public void setQuantileBased(boolean quantileBased) {
//		this.quantileBased = quantileBased;
		if (quantileBased)
			this.quantileNormalizationRate = 1.0;
		else
			this.quantileNormalizationRate = 0.0;

		this.scoresBuffer = new HashMap<>();

		refreshScoringFunction();

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public Double getMissingValueAvgScoreRatio() {
		return missingValueAvgScoreRatio;
	}

	public void setMissingValueAvgScoreRatio(Double missingValueAvgScoreRatio) {
		this.missingValueAvgScoreRatio = missingValueAvgScoreRatio;

		this.scoresBuffer = new HashMap<>();

		refreshScoringFunction();

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public double getScoreForMissingObjects() {
		return scoreForMissingObjects;
	}

	public double getAverageScoreWithoutMissingValues() {
		return scoreAverageWithoutMissingValues;
	}

	public Function<ComplexDataObject, Double> getUncertaintyFunction() {
		return uncertaintyFunction;
	}

	public double getUncertainty(ComplexDataObject cdo) {
		double missingValueValue = getScoreForMissingObjects();

		Double u = missingValueValue;

		if (getUncertaintyFunction() != null) {
			u = getUncertaintyFunction().apply(cdo);

			if (u == null || Double.isNaN(u))
				u = missingValueValue;

			u = Math.max(0.0, Math.min(1.0, u));
		}
		return u;
	}

	public final void setUncertaintyFunction(Function<ComplexDataObject, Double> uncertaintyFunction) {
		this.uncertaintyFunction = uncertaintyFunction;
		this.scoresBuffer.clear();

		refreshScoringFunction();

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public UncertaintyConsideration getUncertaintyConsideration() {
		return uncertaintyConsideration;
	}

	public void setUncertaintyConsideration(UncertaintyConsideration uncertaintyConsideration) {
		this.uncertaintyConsideration = uncertaintyConsideration;
		this.scoresBuffer.clear();

		refreshScoringFunction();

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public double getWeight() {
		return weight;
	}

	public final void setWeight(double weight) {
		this.weight = weight;
		this.scoresBuffer.clear();

		refreshScoringFunction();

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public String getAttribute() {
		return attribute;
	}

	public IObjectParser<T> getParser() {
		return parser;
	}

	public ComplexDataContainer getContainer() {
		return container;
	}

	/**
	 * sets the data context for the attribute scoring function. Triggers
	 * re-initialization of the function
	 * 
	 * @param container
	 */
	public void setContainer(ComplexDataContainer container) {
		this.container = container;
		this.scoresBuffer.clear();

		refreshScoringFunction();

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	/**
	 * 
	 * @param function
	 * @param absoluteValues scores may be negative but may be needed in an absolute
	 *                       way
	 * @return
	 */
	public static double calculateAverageScoreWithoutMissingValues(AttributeScoringFunction<?> function,
			boolean absoluteValues) {
		Collection<Double> scores = new ArrayList<>();

		for (ComplexDataObject cdo : function.getContainer()) {
			Object o = cdo.getAttribute(function.getAttribute());

			if (o == null)
				continue;

			if (o instanceof Number)
				if (Double.isNaN(((Number) o).doubleValue()))
					continue;

			if (absoluteValues)
				scores.add(Math.abs(function.apply(cdo)));
			else
				scores.add(function.apply(cdo));
		}

		if (scores.isEmpty())
			return 0.0;

		StatisticsSupport statistics = new StatisticsSupport(scores);
		return statistics.getMean();
	}

	@Override
	public String toString() {
		return getAttribute();
	}

	public double getTruncatedValueRate() {
		return truncatedValueRate;
	}

	public double getTruncatedValueRateTop() {
		return truncatedValueRateTop;
	}

	public void setTruncatedValueRateTop(double truncatedValueRateTop) {
		this.truncatedValueRateTop = truncatedValueRateTop;
	}

	public double getQuantileNormalizationRate() {
		return quantileNormalizationRate;
	}

	public void setQuantileNormalizationRate(double quantileNormalizationRate) {
		this.quantileNormalizationRate = Math.max(0.0, Math.min(1.0, quantileNormalizationRate));

//		if (this.quantileNormalizationRate == 1)
//			quantileBased = true;
//		else
//			quantileBased = false;

		this.scoresBuffer = new HashMap<>();

		refreshScoringFunction();

		AttributeScoringFunctionChangeEvent event = new AttributeScoringFunctionChangeEvent(this, attribute, this);

		notifyListeners(event);
	}
}
