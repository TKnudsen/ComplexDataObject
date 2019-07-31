package com.github.TKnudsen.ComplexDataObject.model.scoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import javax.swing.event.ChangeListener;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.data.entry.EntryWithComparableKey;
import com.github.TKnudsen.ComplexDataObject.data.ranking.Ranking;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.BooleanParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.AttributeScoringFunction;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.BooleanAttributeScoringFunction;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.DoubleAttributeScoringFunction;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;

public class AttributeScoringModel implements AttributeScoringChangeListener {

	private List<AttributeScoringFunction<?>> attributeWeightingFunctions = new ArrayList<>();

	private Boolean validationMode = false;

	private List<ChangeListener> changeListeners = new ArrayList<>();

	public Ranking<EntryWithComparableKey<Double, ComplexDataObject>> calculateRanking(ComplexDataContainer container) {

		Ranking<EntryWithComparableKey<Double, ComplexDataObject>> cdoRanking = new Ranking<>();

		for (ComplexDataObject cdo : container) {
			double score = 0.0;
			for (AttributeScoringFunction<?> attributeScoringFunction : attributeWeightingFunctions) {
				Double v = attributeScoringFunction.apply(cdo);

				v *= attributeScoringFunction.getWeight();

				if (v != null && !Double.isNaN(v))
					score += v;
			}

			cdoRanking.add(new EntryWithComparableKey<Double, ComplexDataObject>(score, cdo));
		}

		if (validationMode)
			for (EntryWithComparableKey<Double, ComplexDataObject> entry : cdoRanking)
				System.out.println(MathFunctions.round(entry.getKey(), 3) + ":\t" + entry.getValue().getName() + ":\t"
						+ entry.getValue().getAttribute("ISIN"));

		handleItemRankingChangeEvent(new ItemRankingChangeEvent(this, cdoRanking));

		return cdoRanking;
	}

	public void addAttributeScoringFunction(ComplexDataContainer container, String attribute) {
		this.addAttributeScoringFunction(container, attribute, null);
	}

	public void addAttributeScoringFunction(ComplexDataContainer container, String attribute,
			Function<ComplexDataObject, Double> uncertaintyFunction) {
		Objects.requireNonNull(container);
		Objects.requireNonNull(attribute);

		AttributeScoringFunction<?> f = null;

		switch (container.getType(attribute).getSimpleName()) {
		case "Boolean":
			f = new BooleanAttributeScoringFunction(container, new BooleanParser(), attribute, null, false, true, 1.0,
					uncertaintyFunction);
			break;
		case "Double":
			f = new DoubleAttributeScoringFunction(container, new DoubleParser(), attribute, null, false, true, 1.0,
					uncertaintyFunction);
			break;
		case "Integer":
			f = new DoubleAttributeScoringFunction(container, new DoubleParser(), attribute, null, false, true, 1.0,
					uncertaintyFunction);
			break;
		default:
			System.err.println(
					"AttributeScoringModel: unsupported data type: " + container.getType(attribute).getSimpleName());
			break;
		}

		addAttributeScoringFunction(f);
	}

	public void addAttributeScoringFunction(AttributeScoringFunction<?> attributeScoringFunction) {
		attributeWeightingFunctions.add(attributeScoringFunction);
		attributeScoringFunction.addAttributeScoringChangeListener(this);

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this,
				attributeScoringFunction.getAttribute(), attributeScoringFunction);
		handleAttributeScoringChangeEvent(event);
	}

	public void removeAttributeScoringFunction(String attribute) {
		AttributeScoringFunction<?> f = null;

		for (int i = 0; i < attributeWeightingFunctions.size(); i++)
			if (attributeWeightingFunctions.get(i).getAttribute().equals(attribute)) {
				f = attributeWeightingFunctions.remove(i);
				i--;
			}

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, f);
		handleAttributeScoringChangeEvent(event);
	}

	public AttributeScoringFunction<?> getAttributeScoringFunction(String attribute) {
		for (int i = 0; i < attributeWeightingFunctions.size(); i++)
			if (attributeWeightingFunctions.get(i).getAttribute().equals(attribute))
				return attributeWeightingFunctions.get(i);
		return null;
	}

	public List<AttributeScoringFunction<?>> getAttributeScoringFunctions() {
		return new CopyOnWriteArrayList<AttributeScoringFunction<?>>(attributeWeightingFunctions);
	}

	public void setAttributeScoringInformationQuantile(String attribute, Boolean quantile) {
		AttributeScoringFunction<?> f = getAttributeScoringFunction(attribute);

		if (f == null)
			throw new IllegalArgumentException("AttributeSorter: attribute does not exist");

		f.setQuantileBased(quantile);

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, f);
		handleAttributeScoringChangeEvent(event);
	}

	public void setAttributeScoringInformationHighIsGood(String attribute, Boolean highIsGood) {
		AttributeScoringFunction<?> f = getAttributeScoringFunction(attribute);

		if (f == null)
			throw new IllegalArgumentException("AttributeSorter: attribute does not exist");

		f.setHighIsGood(highIsGood);

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, f);
		handleAttributeScoringChangeEvent(event);
	}

	public void setAttributeScoringInformationWeight(String attribute, Double weight) {
		AttributeScoringFunction<?> f = getAttributeScoringFunction(attribute);

		if (f == null)
			throw new IllegalArgumentException("AttributeSorter: attribute does not exist");

		f.setWeight(weight);

		AttributeScoringChangeEvent event = new AttributeScoringChangeEvent(this, attribute, f);
		handleAttributeScoringChangeEvent(event);
	}

	public void addChangeListener(ChangeListener listener) {
		changeListeners.remove(listener);

		changeListeners.add(listener);
	}

	private final void handleAttributeScoringChangeEvent(AttributeScoringChangeEvent event) {
		for (ChangeListener listener : changeListeners)
			listener.stateChanged(event);
	}

	private final void handleItemRankingChangeEvent(ItemRankingChangeEvent event) {
		for (ChangeListener listener : changeListeners)
			listener.stateChanged(event);
	}

	public Boolean isValidationMode() {
		return validationMode;
	}

	public void setValidationMode(Boolean validationMode) {
		this.validationMode = validationMode;
	}

	@Override
	public void attributeScoringFunctionChanged(AttributeScoringChangeEvent event) {
		handleAttributeScoringChangeEvent(event);
	}
}
