package com.github.TKnudsen.ComplexDataObject.model.scoring.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.numerification.DoubleNumerificationInputDialogFunction;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.numerification.NumerificationInputDialogFunction;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.BooleanParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.DoubleParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.IObjectParser;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.Parsers;
import com.github.TKnudsen.ComplexDataObject.model.scoring.functions.Double.DoubleAttributeBipolarScoringFunction;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

/**
 * <p>
 * Static utility class providing helper operations for
 * AttributeScoringFunction instances, including alphabetical sorting,
 * average/impact score calculations, categorical grouping, correlation
 * computation between two functions, and factory methods for creating
 * scoring and uncertainty functions for a given attribute.
 * </p>
 *
 * @deprecated Use
 *             {@code com.github.TKnudsen.scoring.model.scoring.AttributeScoringFunctions}
 *             instead.
 */
@Deprecated
public class AttributeScoringFunctions {

	public static List<AttributeScoringFunction<?>> sortAlphabetically(
			List<AttributeScoringFunction<?>> scoringFunctions) {
		List<AttributeScoringFunction<?>> functions = new ArrayList<>(scoringFunctions);

		// sort alphabetically
		Collections.sort(functions, new Comparator<Function<? super ComplexDataObject, Double>>() {
			@Override
			public int compare(Function<? super ComplexDataObject, Double> o1,
					Function<? super ComplexDataObject, Double> o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});

		return functions;
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

			double value = (absoluteValues) ? Math.abs(function.apply(cdo)) : function.apply(cdo);
			scores.add(value);
		}

		if (scores.isEmpty())
			return 0.0;

		StatisticsSupport statistics = new StatisticsSupport(scores);

		double score = statistics.getMean();
		if (Double.isNaN(score))
			System.err.println(
					"AttributeScoringFunctions.calculateAverageScoreWithoutMissingValues: NaN value detected for the average score!");
		return score;
	}

	/**
	 * useful for categorical attribute types which are (to be) with are parsed with
	 * a NumerificationInputDialogFunction.
	 * 
	 * @param attributeScoringFunction
	 * @return
	 */
	public static SortedMap<Object, List<ComplexDataObject>> getAttributeCategories(
			AttributeScoringFunction<Double> attributeScoringFunction) {

		if (attributeScoringFunction.getParser() == null
				|| !(attributeScoringFunction.getParser() instanceof NumerificationInputDialogFunction
						|| attributeScoringFunction.getContainer().getType(attributeScoringFunction.getAttribute())
								.equals(String.class)))
			throw new IllegalArgumentException(
					"AttributeScoringFunctions.getAttributeCategories requires categorical input data that is mapped to Double using a NumerificationInputDialogFunction.");

		String attribute = attributeScoringFunction.getAttribute();

		SortedMap<Object, List<ComplexDataObject>> map = new TreeMap<>();
		for (ComplexDataObject cdo : attributeScoringFunction.getContainer()) {
			Object object = cdo.getAttribute(attribute);

			if (object == null)
				continue;

			if (!map.containsKey(object))
				map.put(cdo.getAttribute(attribute), new ArrayList<>());

			map.get(cdo.getAttribute(attribute)).add(cdo);
		}

		return map;
	}

	public static AttributeScoringFunction<?> create(ComplexDataContainer container, String attribute,
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
			f = new DoubleAttributeBipolarScoringFunction(container, new DoubleParser(true), attribute, null, false,
					true, 1.0, uncertaintyFunction);
			break;
		case "Integer":
		case "Long":
			f = new DoubleAttributeBipolarScoringFunction(container, new DoubleParser(true), attribute, null, false,
					true, 1.0, uncertaintyFunction);
			break;
		case "String":
			f = new DoubleAttributeBipolarScoringFunction(container,
					new DoubleNumerificationInputDialogFunction(true, 10000), attribute, null, false, true, 1.0,
					uncertaintyFunction);
			break;

		default:
			throw new IllegalArgumentException(
					"AttributeScoringModel: unsupported data type: " + container.getType(attribute).getSimpleName());
		}

		return f;
	}

	public static AttributeScoringFunction<?> create(ComplexDataContainer container, String attribute,
			Function<ComplexDataObject, Double> uncertaintyFunction, IObjectParser<Double> toDoubleParser) {
		Objects.requireNonNull(container);
		Objects.requireNonNull(attribute);
		Objects.requireNonNull(toDoubleParser);

		return new DoubleAttributeBipolarScoringFunction(container, toDoubleParser, attribute, null, false, true, 1.0,
				uncertaintyFunction);
	}

	/**
	 * uses the scores of an attribute scoring function for a data set and computes
	 * the impact. The impact sums up the absolute scores, reflecting the numerical
	 * amount of scores introduced by an ASF to a ranking system.
	 * 
	 * @param function
	 * @param items
	 * @param negativeScoresImpactIsDoubled in case negative scores are of greater
	 *                                      importance
	 * @return
	 */
	public static double calculateImpact(AttributeScoringFunction<?> function, Iterable<ComplexDataObject> items,
			boolean negativeScoresImpactIsDoubled) {
		double impact = 0.0;

		for (ComplexDataObject cdo : items) {
			Double score = function.apply(cdo);

			if (Double.isNaN(score))
				System.err.println(
						"AttributeScoringFunctions: function returned NaN for item. that should never be the case");
			else {
				if (score >= 0)
					impact += score * function.getWeight();
				else if (negativeScoresImpactIsDoubled)
					impact += Math.abs(score * function.getWeight() * 2);
				else
					impact += Math.abs(score * function.getWeight());
			}
		}

		return impact;
	}

	/**
	 * uses the scores of an attribute scoring function for a data set and computes
	 * the impact. The impact sums up the absolute scores, reflecting the numerical
	 * amount of scores introduced by an ASF to a ranking system.
	 * 
	 * @param function
	 * @param items
	 * @param negativeScoresImpactIsDoubled in case negative scores are of greater
	 *                                      importance
	 * @return
	 */
	public static double[] calculateImpactWithMissingValueImpact(AttributeScoringFunction<?> function,
			Iterable<ComplexDataObject> items, boolean negativeScoresImpactIsDoubled) {
		double impact = 0.0;
		double impactMV = 0.0;

		for (ComplexDataObject cdo : items) {
			Object o = cdo.getAttribute(function.getAttribute());
			Double value;
			if (o instanceof Number)
				value = Parsers.parseDouble(o);
			else
				value = o == null ? null : 1.0;

			Double score = function.apply(cdo);

			if (Double.isNaN(score))
				System.err.println(
						"AttributeScoringFunctions: function returned NaN for item. that should never be the case");
			else {
				if (score >= 0)
					impact += score * function.getWeight();
				else if (negativeScoresImpactIsDoubled)
					impact += Math.abs(score * function.getWeight() * 2);
				else
					impact += Math.abs(score * function.getWeight());

				if (value == null || Double.isNaN(value)) {
					if (score >= 0)
						impactMV += score * function.getWeight();
					else if (negativeScoresImpactIsDoubled)
						impactMV += Math.abs(score * function.getWeight() * 2);
					else
						impactMV += Math.abs(score * function.getWeight());
				}

			}
		}

		return new double[] { MathFunctions.round(impact, 3), MathFunctions.round(impactMV, 3) };
	}

	/**
	 * 
	 * @param dataContainer        data container where the uncertainty attribute is
	 *                             in. Not (necessarily) the container where the ASF
	 *                             attribute is in.
	 * @param uncertaintyAttribute typically the attribute plus an uncertainty
	 *                             identifier such as attributeX[u]
	 * @return
	 */
	public static Function<ComplexDataObject, Double> createUncertaintyFunction(ComplexDataContainer dataContainer,
			String uncertaintyAttribute) {

		if (dataContainer == null)
			return null;
		if (uncertaintyAttribute == null)
			return null;
		if (!dataContainer.containsAttribute(uncertaintyAttribute)) {
			System.err.println("AttributeScoringFunctions.createUncertaintyFunction: uncertainty attribute ("
					+ uncertaintyAttribute + ") not part of the data container");
			return null;
		}

		if (dataContainer.containsAttribute(uncertaintyAttribute)) {
			Function<ComplexDataObject, Double> f = new Function<ComplexDataObject, Double>() {

				DoubleParser doubleParser = new DoubleParser(true);

				@Override
				public Double apply(ComplexDataObject cdo) {
					if (cdo == null)
						return 1.0;
					Object value = cdo.getAttribute(uncertaintyAttribute);
					if (value == null)
						value = 1.0;
					if (value instanceof Double)
						return (Double) value;
					return doubleParser.apply(value);
				}
			};

			return f;
		} else
			return null;
	}

	/**
	 * 
	 * @param container
	 * @param f1
	 * @param f2
	 * @param pearson
	 * @param spearman
	 * @param minimumSize default: 2 (leading to a perfect correlation though)
	 * @return
	 */
	public static double getCorrelation(ComplexDataContainer container, AttributeScoringFunction<?> f1,
			AttributeScoringFunction<?> f2, boolean pearson, boolean spearman, int minimumSize) {

		Collection<Double> values1 = new ArrayList<>();
		Collection<Double> values2 = new ArrayList<>();

		for (ComplexDataObject cdo : container) {
			double v1 = f1.apply(cdo);
			double v2 = f2.apply(cdo);

			if (!Double.isNaN(v1) && !Double.isNaN(v2)) {
				values1.add(v1);
				values2.add(v2);
			}
		}

		return computeCorrelation(values1, values2, pearson, spearman, minimumSize);
	}

	/**
	 * TODO nice to have: unify this with
	 * com.github.TKnudsen.statistics.Correlations once the statistics project
	 * has obtained a resolvable Maven coordinate; it is duplicated here so that
	 * ComplexDataObject, being a root project, does not depend on it.
	 */
	private static double computeCorrelation(Collection<Double> values1, Collection<Double> values2,
			boolean pearson, boolean spearman, int minimumSize) {
		if (values1.size() != values2.size())
			throw new IllegalArgumentException("AttributeScoringFunctions: collections must be equal length");

		int n = values1.size();
		if (n < Math.max(3, minimumSize))
			return Double.NaN;

		double[] a1 = values1.stream().mapToDouble(Double::doubleValue).toArray();
		double[] a2 = values2.stream().mapToDouble(Double::doubleValue).toArray();

		Double p = pearson ? new PearsonsCorrelation().correlation(a1, a2) : null;
		Double s = spearman ? new SpearmansCorrelation().correlation(a1, a2) : null;

		if (p != null && s == null)
			return p;
		if (p == null && s != null)
			return s;
		if (p == null)
			return Double.NaN;
		return (p + s) * 0.5;
	}

}
