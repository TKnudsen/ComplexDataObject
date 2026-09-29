package com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.clamping;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainer;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataContainers;
import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.Parsers;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataProcessingCategory;
import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.IComplexDataObjectProcessor;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

/**
 * <p>
 * Percentile-clamps (suppresses outliers in) whichever attributes of a
 * container are registered in a {@link ClampConfigurationStore}, driven
 * entirely by that configuration rather than by any attribute list baked
 * into source code.
 * </p>
 *
 * <p>
 * Not tied to any particular container or attribute-naming scheme: a
 * container attribute is looked up in the store under its <i>canonical
 * key</i>, produced by the {@code keyNormalizer} given at construction --
 * the identity function for a container whose attribute names already match
 * the store 1:1, or something that collapses many concrete names onto one
 * shared key (e.g. stripping a generated suffix) for a container that
 * doesn't. Every attribute whose canonical key is neither registered nor
 * marked ignored is reported via {@code LOG.warning}, making configuration
 * gaps visible without stopping execution.
 * </p>
 *
 * <p>
 * An optional {@code skipAttribute} predicate, given at construction, lets a
 * caller exclude a whole category of attributes from clamping and from the
 * gap warning entirely -- e.g. a caller whose attribute names carry an
 * uncertainty marker (a standard deviation accompanying a point estimate) may
 * want those skipped outright, since clamping them against the point
 * estimate's own bounds would be meaningless. Defaults to never skipping
 * anything.
 * </p>
 *
 * <p>
 * A {@code priorityKeySet} of item primary keys is monitored during
 * clamping. If any priority item is affected by clamping, a warning is
 * logged -- a high count suggests the bounds for that attribute may need
 * recalibration.
 * </p>
 *
 * <p>
 * {@link #onBeforeClamp(ComplexDataContainer, String, StatisticsSupport)} and
 * {@link #onPriorityItemsAffected(ComplexDataContainer, String,
 * StatisticsSupport, int)} are no-op extension points a subclass can
 * override to add e.g. a visualization step -- this class itself has no
 * opinion on how (or whether) to visualize a distribution.
 * </p>
 *
 * @version 1.0
 * @since 2026
 */
public class PercentileClampingProcessor implements IComplexDataObjectProcessor {

	private static final Logger LOG = Logger.getLogger(PercentileClampingProcessor.class.getName());

	private final ClampConfigurationStore store;
	private final UnaryOperator<String> keyNormalizer;
	private final Predicate<String> skipAttribute;
	private final Set<String> priorityKeySet;

	public PercentileClampingProcessor(ClampConfigurationStore store) {
		this(store, UnaryOperator.identity(), attribute -> false, Collections.emptySet());
	}

	public PercentileClampingProcessor(ClampConfigurationStore store, Set<String> priorityKeySet) {
		this(store, UnaryOperator.identity(), attribute -> false, priorityKeySet);
	}

	public PercentileClampingProcessor(ClampConfigurationStore store, UnaryOperator<String> keyNormalizer,
			Set<String> priorityKeySet) {
		this(store, keyNormalizer, attribute -> false, priorityKeySet);
	}

	public PercentileClampingProcessor(ClampConfigurationStore store, UnaryOperator<String> keyNormalizer,
			Predicate<String> skipAttribute, Set<String> priorityKeySet) {
		if (store == null)
			throw new IllegalArgumentException("PercentileClampingProcessor: store must not be null");
		if (keyNormalizer == null)
			throw new IllegalArgumentException("PercentileClampingProcessor: keyNormalizer must not be null");

		this.store = store;
		this.keyNormalizer = keyNormalizer;
		this.skipAttribute = skipAttribute == null ? attribute -> false : skipAttribute;
		this.priorityKeySet = priorityKeySet == null ? Collections.emptySet() : priorityKeySet;
	}

	@Override
	public DataProcessingCategory getPreprocessingCategory() {
		return DataProcessingCategory.DATA_CLEANING;
	}

	@Override
	public void process(List<ComplexDataObject> data) {
		process(new ComplexDataContainer(data, "ISIN"));
	}

	@Override
	public void process(ComplexDataContainer container) {
		for (String attribute : container.getAttributes()) {
			if (skipAttribute.test(attribute))
				continue;

			String canonicalKey = keyNormalizer.apply(attribute);

			if (store.isIgnored(canonicalKey))
				continue;

			Optional<ClampBounds> bounds = store.getBounds(canonicalKey);
			if (!bounds.isPresent()) {
				LOG.warning("PercentileClampingProcessor: unregistered attribute in container: '" + attribute
						+ "' (canonical key '" + canonicalKey + "') -- add clamp bounds or mark it ignored.");
				continue;
			}

			clampAttribute(container, bounds.get().getLowerPercentile(), bounds.get().getUpperPercentile(),
					attribute);
		}
	}

	/**
	 * Clamps the values of {@code attribute} to the [{@code minPercentile},
	 * {@code maxPercentile}] range.
	 *
	 * <p>
	 * When priority items are affected, a warning is logged with the count at the
	 * lower and upper bound separately, and the average original value at each
	 * bound -- providing enough information to judge whether re-calibration is
	 * needed.
	 *
	 * @param container     the container to modify; must not be null
	 * @param minPercentile lower bound percentile, in [0, 100]
	 * @param maxPercentile upper bound percentile, in [0, 100]
	 * @param attribute     the name of the attribute to clamp
	 */
	private void clampAttribute(ComplexDataContainer container, double minPercentile, double maxPercentile,
			String attribute) {

		if (!container.containsAttribute(attribute))
			return;

		double[] validValues = ComplexDataContainers.getAttributeValuesNumerical(container, attribute, true, true,
				false);
		if (validValues.length == 0) {
			LOG.warning("PercentileClampingProcessor.clampAttribute: no valid values for '" + attribute + "'");
			return;
		}

		StatisticsSupport stats = new StatisticsSupport(validValues);
		double lowerBound = stats.getPercentile(minPercentile);
		double upperBound = stats.getPercentile(maxPercentile);

		LOG.fine("clampAttribute: '" + attribute + "' to [p" + minPercentile + "=" + lowerBound + ", p" + maxPercentile
				+ "=" + upperBound + "]");

		onBeforeClamp(container, attribute, stats);

		// Track priority-item clamping separately for lower and upper bounds,
		// accumulating original values to compute averages for the warning message.
		int priorityLowerCount = 0;
		int priorityUpperCount = 0;
		double priorityLowerValueSum = 0.0;
		double priorityUpperValueSum = 0.0;

		for (ComplexDataObject item : container) {
			Double d = Parsers.parseDouble(item.getAttribute(attribute));
			if (d == null || d.isNaN())
				continue;

			if (d < lowerBound) {
				if (priorityKeySet.contains(Parsers.parseString(item.getAttribute("ISIN")))) {
					priorityLowerCount++;
					priorityLowerValueSum += d;
				}
				item.add(attribute, lowerBound);
			} else if (d > upperBound) {
				if (priorityKeySet.contains(Parsers.parseString(item.getAttribute("ISIN")))) {
					priorityUpperCount++;
					priorityUpperValueSum += d;
				}
				item.add(attribute, upperBound);
			}
		}

		int totalPriorityAffected = priorityLowerCount + priorityUpperCount;
		if (totalPriorityAffected > 1) {
			String nl = System.lineSeparator();
			StringBuilder warning = new StringBuilder();
			warning.append("PercentileClampingProcessor: priority item(s) affected by clamping")
					.append(" for attribute '").append(attribute).append("':").append(nl);

			if (priorityLowerCount > 0) {
				double avgOriginal = MathFunctions.round(priorityLowerValueSum / priorityLowerCount, 4);
				warning.append("  lower bound (p").append(minPercentile).append(" = ")
						.append(MathFunctions.round(lowerBound, 4)).append("): ").append(priorityLowerCount)
						.append(" item(s) clamped").append(", avg original value = ").append(avgOriginal).append(nl);
			}

			if (priorityUpperCount > 0) {
				double avgOriginal = MathFunctions.round(priorityUpperValueSum / priorityUpperCount, 4);
				warning.append("  upper bound (p").append(maxPercentile).append(" = ")
						.append(MathFunctions.round(upperBound, 4)).append("): ").append(priorityUpperCount)
						.append(" item(s) clamped").append(", avg original value = ").append(avgOriginal).append(nl);
			}

			warning.append("  total affected: ").append(totalPriorityAffected)
					.append(" - consider recalibrating the bounds for this attribute.");

			LOG.warning(warning.toString());

			onPriorityItemsAffected(container, attribute, stats, totalPriorityAffected);
		}
	}

	/**
	 * Called once per clamped attribute, right after its bounds are computed but
	 * before the container is modified. No-op by default; a subclass can
	 * override this to add e.g. a distribution visualization for calibration
	 * purposes.
	 */
	protected void onBeforeClamp(ComplexDataContainer container, String attribute, StatisticsSupport stats) {
	}

	/**
	 * Called once per clamped attribute, only when more than one priority item
	 * was affected by clamping, right after the corresponding warning is
	 * logged. No-op by default.
	 */
	protected void onPriorityItemsAffected(ComplexDataContainer container, String attribute, StatisticsSupport stats,
			int totalPriorityAffected) {
	}

	/**
	 * Logs a fixed percentile breakdown of the given {@link StatisticsSupport}
	 * for calibration purposes. Exposed to subclasses so an overridden
	 * extension hook can reuse it.
	 */
	protected void logPercentileBreakdown(StatisticsSupport stats, String attribute, boolean extendedVersion) {
		// Build the entire breakdown as a single message so that the Logger
		// emits one header line rather than one per percentile row.
		String nl = System.lineSeparator();

		String msg;
		if (extendedVersion) {
			msg = "Percentile breakdown for '" + attribute + "':" + nl + "  min   : "
					+ MathFunctions.round(stats.getMin(), 2) + nl + "  p0.1  : "
					+ MathFunctions.round(stats.getPercentile(0.1), 2) + nl + "  p0.25  : "
					+ MathFunctions.round(stats.getPercentile(0.25), 2) + nl + "  p0.5  : "
					+ MathFunctions.round(stats.getPercentile(0.5), 2) + nl + "  p1.0  : "
					+ MathFunctions.round(stats.getPercentile(1), 2) + nl + "  p2.0  : "
					+ MathFunctions.round(stats.getPercentile(2), 2) + nl + "  p50   : "
					+ MathFunctions.round(stats.getMedian(), 2) + nl + "  mean  : "
					+ MathFunctions.round(stats.getMean(), 2) + nl + "  p98   : "
					+ MathFunctions.round(stats.getPercentile(98), 2) + nl + "  p99   : "
					+ MathFunctions.round(stats.getPercentile(99), 2) + nl + "  p99.5 : "
					+ MathFunctions.round(stats.getPercentile(99.5), 2) + nl + "  p99.75 : "
					+ MathFunctions.round(stats.getPercentile(99.75), 2) + nl + "  p99.9 : "
					+ MathFunctions.round(stats.getPercentile(99.9), 2) + nl + "  max   : "
					+ MathFunctions.round(stats.getMax(), 2);
		} else {
			msg = "Percentile breakdown for '" + attribute + "':" + nl + "  min   : "
					+ MathFunctions.round(stats.getMin(), 2) + nl + "  p1.0  : "
					+ MathFunctions.round(stats.getPercentile(1), 2) + nl + "  p2.0  : "
					+ MathFunctions.round(stats.getPercentile(2), 2) + nl + "  p50   : "
					+ MathFunctions.round(stats.getMedian(), 2) + nl + "  mean  : "
					+ MathFunctions.round(stats.getMean(), 2) + nl + "  p98   : "
					+ MathFunctions.round(stats.getPercentile(98), 2) + nl + "  p99   : "
					+ MathFunctions.round(stats.getPercentile(99), 2) + nl + "  max   : "
					+ MathFunctions.round(stats.getMax(), 2);
		}

		LOG.info(msg);
	}

	/**
	 * Unmodifiable view of the priority keys this processor was constructed with.
	 */
	public Collection<String> getPriorityKeySet() {
		return Collections.unmodifiableSet(priorityKeySet);
	}
}
