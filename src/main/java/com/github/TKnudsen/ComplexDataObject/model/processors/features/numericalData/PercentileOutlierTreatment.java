package com.github.TKnudsen.ComplexDataObject.model.processors.features.numericalData;

import java.util.List;
import java.util.Map.Entry;

import com.github.TKnudsen.ComplexDataObject.data.entry.EntryWithComparableKey;
import com.github.TKnudsen.ComplexDataObject.model.tools.StatisticsSupport;

/**
 * <p>
 * Removes the
 * </p>
 *
 * @version 1.01
 * @since 2017
 */
public class PercentileOutlierTreatment extends AbstractOutlierTreatment {

	private double outlierpercentile;

	public PercentileOutlierTreatment() {
		this(5.0);
	}

	/**
	 * percentage in x.0: example: 5% = 5.0.
	 * 
	 * @param outlierpercentile
	 */
	public PercentileOutlierTreatment(double outlierpercentile) {
		this.outlierpercentile = outlierpercentile;
	}

	@Override
	protected Entry<Double, Double> calculateBounds(List<Double> values) {
		StatisticsSupport statisticsSupport = new StatisticsSupport(values);
		return new EntryWithComparableKey<>(statisticsSupport.getPercentile(outlierpercentile), statisticsSupport.getPercentile(100.0 - outlierpercentile));
	}

}
