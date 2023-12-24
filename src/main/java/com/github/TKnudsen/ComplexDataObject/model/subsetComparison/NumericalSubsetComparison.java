package com.github.TKnudsen.ComplexDataObject.model.subsetComparison;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

public class NumericalSubsetComparison implements SubsetComparison<Number> {

	private boolean referenceSetBuffering = false;
	private Map<Integer, StatisticsSupport> referenceSetStatistics = new HashMap<>();

	public NumericalSubsetComparison() {
		this(false);
	}

	public NumericalSubsetComparison(boolean referenceSetBuffering) {
		this.referenceSetBuffering = referenceSetBuffering;
	}

	@Override
	public <T> Number compare(Collection<T> referenceSet, Collection<T> targetSet,
			Function<T, Number> toValueFunction) {
		Objects.requireNonNull(toValueFunction);

		if (referenceSet == null || referenceSet.isEmpty())
			return 0.0;
		if (targetSet == null || targetSet.isEmpty())
			return 0.0;

		Integer hash = 31 * referenceSet.hashCode() + toValueFunction.hashCode();

		StatisticsSupport refStatistics = null;
		if (referenceSetBuffering)
			if (referenceSetStatistics.containsKey(hash))
				refStatistics = referenceSetStatistics.get(hash);

		if (refStatistics == null) {
			Collection<Number> refCollection = new ArrayList<>();
			for (T t : referenceSet) {
				Number n = toValueFunction.apply(t);
				if (n != null && !Double.isNaN(n.doubleValue()))
					refCollection.add(n);
			}
			refStatistics = new StatisticsSupport(refCollection);
			if (referenceSetBuffering)
				referenceSetStatistics.put(hash, refStatistics);
		}

		Collection<Number> targetCollection = new ArrayList<>();
		for (T t : targetSet) {
			Number n = toValueFunction.apply(t);
			if (n != null && !Double.isNaN(n.doubleValue()))
				targetCollection.add(n);
		}
		StatisticsSupport targetStatistics = new StatisticsSupport(targetCollection);

		return targetStatistics.getMean() - refStatistics.getMean();
	}

	public boolean isReferenceSetBuffering() {
		return referenceSetBuffering;
	}

	public void setReferenceSetBuffering(boolean referenceSetBuffering) {
		this.referenceSetBuffering = referenceSetBuffering;
	}

}
