package com.github.TKnudsen.ComplexDataObject.model.scoring.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.scoring.AttributeScoringChangeEvent;
import com.github.TKnudsen.ComplexDataObject.model.scoring.AttributeScoringChangeListener;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

public abstract class AttributeScoringFunction<T> implements Function<ComplexDataObject, Double> {

	private IObjectParser<T> parser;

	@JsonIgnore
	private ComplexDataContainer container;

	private String attribute;
	private String abbreviation;
	private boolean quantileBased;
	private boolean highIsGood;
	private double weight;

	@JsonIgnore
	private Function<ComplexDataObject, Double> uncertaintyFunction = null;

	private Double scoreForMissingObjects = null;

	@JsonIgnore
	private List<AttributeScoringChangeListener> listeners = new ArrayList<AttributeScoringChangeListener>();

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
		this.quantileBased = quantileBased;
		this.highIsGood = highIsGood;
		this.weight = weight;

		this.uncertaintyFunction = uncertaintyFunction;
	}

	public abstract double getAverageScoreWithoutMissingValues();

	protected abstract void refreshScoringFunction();

	protected abstract Double applyValue(T value);

	@Override
	public final Double apply(ComplexDataObject cdo) {

		Object o = cdo.getAttribute(attribute);

		Double missingValueValue = getScoreForMissingObjects();
		if (missingValueValue == null)
			missingValueValue = getAverageScoreWithoutMissingValues() * 0.5;

		if (o == null)
			return missingValueValue;

		T value = parser.apply(o);

		double s = applyValue(value);

		// uncertainty information. As a result the scoring function does
		// not necessarily produce at least once the score 1.0
		if (getUncertaintyFunction() != null) {
			Double u = getUncertaintyFunction().apply(cdo);

			if (u == null || Double.isNaN(u))
				u = missingValueValue;

			u = Math.max(0.0, Math.min(1.0, u));

			return s * (1 - u);
		}

		return s;
	}

	public void addAttributeScoringChangeListener(AttributeScoringChangeListener listener) {
		listeners.remove(listener);
		listeners.add(listener);
	}

	protected final void notifyListeners(AttributeScoringChangeEvent event) {
		for (AttributeScoringChangeListener listener : listeners)
			listener.attributeScoringFunctionChanged(event);
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public final void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;

		// not necessarily needed but consistent
		refreshScoringFunction();

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public boolean isHighIsGood() {
		return highIsGood;
	}

	public final void setHighIsGood(boolean highIsGood) {
		this.highIsGood = highIsGood;

		refreshScoringFunction();

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public boolean isQuantileBased() {
		return quantileBased;
	}

	public void setQuantileBased(boolean quantileBased) {
		this.quantileBased = quantileBased;

		refreshScoringFunction();

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public Double getScoreForMissingObjects() {
		return scoreForMissingObjects;
	}

	public final void setScoreForMissingObjects(double scoreForMissingObjects) {
		this.scoreForMissingObjects = scoreForMissingObjects;

		refreshScoringFunction();

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public Function<ComplexDataObject, Double> getUncertaintyFunction() {
		return uncertaintyFunction;
	}

	public final void setUncertaintyFunction(Function<ComplexDataObject, Double> uncertaintyFunction) {
		this.uncertaintyFunction = uncertaintyFunction;

		refreshScoringFunction();

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public double getWeight() {
		return weight;
	}

	public final void setWeight(double weight) {
		this.weight = weight;

		refreshScoringFunction();

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, this);

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

		refreshScoringFunction();

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, this);

		notifyListeners(event);
	}

	public static double calculateAverageScoreWithoutMissingValues(AttributeScoringFunction<?> function) {
		Collection<Double> scores = new ArrayList<>();

		for (ComplexDataObject cdo : function.getContainer()) {
			Object o = cdo.getAttribute(function.getAttribute());

			if (o == null)
				continue;

			if (o instanceof Number)
				if (Double.isNaN(((Number) o).doubleValue()))
					continue;

			scores.add(function.apply(cdo));
		}

		StatisticsSupport statistics = new StatisticsSupport(scores);
		return statistics.getMean();
	}

}
