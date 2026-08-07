package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * <p>
 * Optimized statistics support that automatically switches between
 * DescriptiveStatistics (for datasets &le; threshold) and SummaryStatistics (for
 * datasets > threshold) to balance memory usage and functionality.
 *
 * Small datasets use DescriptiveStatistics which stores all values and provides
 * EXACT statistics including percentiles, median, skewness, and kurtosis.
 *
 * Large datasets use SummaryStatistics with reservoir sampling which computes
 * incrementally using O(1) memory while still providing APPROXIMATE
 * percentiles, median, skewness, and kurtosis based on a statistical sample.
 *
 * Aggregate Statistics Included (always available): - min, max, mean, geometric
 * mean, n, sum, sum of squares, standard deviation, variance
 *
 * Additional statistics (exact for small datasets, approximate for large
 * datasets): - percentiles, median, skewness, kurtosis, outlier detection
 *
 * "Rolling" capability? Yes - use setWindowSize() to maintain only the most
 * recent N values. This provides automatic sliding window statistics. Window
 * size forces descriptive mode.
 *
 * Values stored? Small datasets: all values. Large datasets: reservoir sample
 * of ~1000 values.
 * </p>
 *
 * <p>
 * Reservoir Sampling: For large datasets, maintains a statistically
 * representative random sample using reservoir sampling algorithm. This allows
 * approximate percentile and distribution calculations with constant memory
 * overhead, rather than throwing exceptions.
 * </p>
 *
 * <p>
 * Example usage:
 *
 * <pre>
 * // Small dataset - exact statistics
 * StatisticsSupport small = new StatisticsSupport(dataUnder10k);
 * System.out.println("Exact median: " + small.getMedian());
 *
 * // Large dataset - approximate statistics
 * StatisticsSupport large = new StatisticsSupport(dataOver10k);
 * System.out.println("Approximate median: " + large.getMedian()); // No exception!
 * System.out.println("Is sample? " + large.isSample()); // true
 *
 * // Rolling window for streaming data
 * StatisticsSupport rolling = new StatisticsSupport(data, 100);
 * rolling.setWindowSize(100); // Track last 100 values
 * for (double value : stream) {
 * 	rolling.addValue(value);
 * 	System.out.println("Rolling mean: " + rolling.getMean());
 * }
 * </pre>
 * </p>
 *
 * @version 2.01
 * @since 2012
 */
public class StatisticsSupport implements StatisticalSummary, Iterable<Double> {

	private static final long serialVersionUID = -4338838213221265738L;

	/**
	 * Threshold for switching between DescriptiveStatistics and SummaryStatistics.
	 * Datasets with size <= this value use DescriptiveStatistics (full features).
	 * Datasets with size > this value use SummaryStatistics (memory efficient).
	 */
	private static final int DESCRIPTIVE_STATS_THRESHOLD = 10000;

	/**
	 * Size of sample cache for large datasets to enable approximate percentile
	 * calculations
	 */
	private static final int SAMPLE_CACHE_SIZE = 5000;

	/**
	 * Default window size for DescriptiveStatistics (INFINITE_WINDOW = no limit)
	 */
	private static final int DEFAULT_WINDOW_SIZE = DescriptiveStatistics.INFINITE_WINDOW;

	/**
	 * Mode indicator: true = using DescriptiveStatistics, false = using
	 * SummaryStatistics
	 */
	private boolean useDescriptive;

	/**
	 * Used for small datasets (&le; threshold) - stores values, supports percentiles
	 */
	private DescriptiveStatistics descriptiveStats;

	/**
	 * Used for large datasets (> threshold) - O(1) memory, no percentiles
	 */
	private SummaryStatistics summaryStats;

	/**
	 * Reservoir sample cache for large datasets - enables approximate percentiles
	 * Uses reservoir sampling to maintain a representative sample
	 */
	private List<Double> sampleCache;

	/**
	 * Cached sorted sample for fast repeated percentile lookups Invalidated
	 * whenever sampleCache is modified
	 */
	private List<Double> sortedSampleCache = null;

	/**
	 * Count of total values added (for reservoir sampling)
	 */
	private long totalValuesAdded = 0;

	/**
	 * Cached median (for descriptive mode only)
	 */
	private double median = Double.NaN;

	/**
	 * Cached unique count
	 */
	private int uniqueObservations = -1;

	/**
	 * Window size for rolling statistics (only applicable in descriptive mode)
	 */
	private int windowSize = DEFAULT_WINDOW_SIZE;

	/**
	 * If true, forces the use of DescriptiveStatistics regardless of dataset size
	 */
	private boolean forceDescriptiveMode = false;

	/**
	 * NANs are removed!
	 * 
	 * @param vector
	 */
	public StatisticsSupport(Number[] vector) {
		this(vector, DEFAULT_WINDOW_SIZE, false);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param vector
	 * @param forceDescriptiveMode if true, always use DescriptiveStatistics (exact
	 *                             statistics) regardless of dataset size
	 */
	public StatisticsSupport(Number[] vector, boolean forceDescriptiveMode) {
		this(vector, DEFAULT_WINDOW_SIZE, forceDescriptiveMode);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param vector
	 * @param windowSize window size for rolling statistics
	 *                   (DescriptiveStatistics.INFINITE_WINDOW for no limit)
	 */
	public StatisticsSupport(Number[] vector, int windowSize) {
		this(vector, windowSize, false);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param vector
	 * @param windowSize           window size for rolling statistics
	 *                             (DescriptiveStatistics.INFINITE_WINDOW for no
	 *                             limit)
	 * @param forceDescriptiveMode if true, always use DescriptiveStatistics (exact
	 *                             statistics) regardless of dataset size
	 */
	public StatisticsSupport(Number[] vector, int windowSize, boolean forceDescriptiveMode) {
		this.windowSize = windowSize;
		this.forceDescriptiveMode = forceDescriptiveMode;
		// Don't pre-count - initialize optimistically for descriptive mode
		initializeForSize(Math.min(vector.length, DESCRIPTIVE_STATS_THRESHOLD), windowSize);
		for (Number n : vector)
			if (n != null && !Double.isNaN(n.doubleValue()))
				addValue(n.doubleValue());

	}

	/**
	 * NANs are removed!
	 * 
	 * @param vector
	 */
	public StatisticsSupport(double[] vector) {
		this(vector, DEFAULT_WINDOW_SIZE, false);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param vector
	 * @param forceDescriptiveMode if true, always use DescriptiveStatistics (exact
	 *                             statistics) regardless of dataset size
	 */
	public StatisticsSupport(double[] vector, boolean forceDescriptiveMode) {
		this(vector, DEFAULT_WINDOW_SIZE, forceDescriptiveMode);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param vector
	 * @param windowSize window size for rolling statistics
	 *                   (DescriptiveStatistics.INFINITE_WINDOW for no limit)
	 */
	public StatisticsSupport(double[] vector, int windowSize) {
		this(vector, windowSize, false);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param vector
	 * @param windowSize           window size for rolling statistics
	 *                             (DescriptiveStatistics.INFINITE_WINDOW for no
	 *                             limit)
	 * @param forceDescriptiveMode if true, always use DescriptiveStatistics (exact
	 *                             statistics) regardless of dataset size
	 */
	public StatisticsSupport(double[] vector, int windowSize, boolean forceDescriptiveMode) {
		this.windowSize = windowSize;
		this.forceDescriptiveMode = forceDescriptiveMode;
		// Don't pre-count - initialize optimistically for descriptive mode
		initializeForSize(Math.min(vector.length, DESCRIPTIVE_STATS_THRESHOLD), windowSize);
		for (double d : vector)
			if (!Double.isNaN(d))
				addValue(d);

	}

	/**
	 * NANs are removed!
	 * 
	 * @param values
	 */
	public StatisticsSupport(Collection<? extends Number> values) {
		this(values, DEFAULT_WINDOW_SIZE, false);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param values
	 * @param forceDescriptiveMode if true, always use DescriptiveStatistics (exact
	 *                             statistics) regardless of dataset size
	 */
	public StatisticsSupport(Collection<? extends Number> values, boolean forceDescriptiveMode) {
		this(values, DEFAULT_WINDOW_SIZE, forceDescriptiveMode);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param values
	 * @param windowSize window size for rolling statistics
	 *                   (DescriptiveStatistics.INFINITE_WINDOW for no limit)
	 */
	public StatisticsSupport(Collection<? extends Number> values, int windowSize) {
		this(values, windowSize, false);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param values
	 * @param windowSize           window size for rolling statistics
	 *                             (DescriptiveStatistics.INFINITE_WINDOW for no
	 *                             limit)
	 * @param forceDescriptiveMode if true, always use DescriptiveStatistics (exact
	 *                             statistics) regardless of dataset size
	 */
	public StatisticsSupport(Collection<? extends Number> values, int windowSize, boolean forceDescriptiveMode) {
		this.windowSize = windowSize;
		this.forceDescriptiveMode = forceDescriptiveMode;
		// Don't pre-count - initialize optimistically for descriptive mode
		initializeForSize(Math.min(values.size(), DESCRIPTIVE_STATS_THRESHOLD), windowSize);
		for (Number n : values)
			if (n != null) {
				double d = n.doubleValue();
				if (!Double.isNaN(d))
					addValue(d);
			}
	}

	/**
	 * NANs are removed!
	 * 
	 * @param values
	 */
	public StatisticsSupport(List<Double> values) {
		this(values, DEFAULT_WINDOW_SIZE, false);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param values
	 * @param forceDescriptiveMode if true, always use DescriptiveStatistics (exact
	 *                             statistics) regardless of dataset size
	 */
	public StatisticsSupport(List<Double> values, boolean forceDescriptiveMode) {
		this(values, DEFAULT_WINDOW_SIZE, forceDescriptiveMode);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param values
	 * @param windowSize window size for rolling statistics
	 *                   (DescriptiveStatistics.INFINITE_WINDOW for no limit)
	 */
	public StatisticsSupport(List<Double> values, int windowSize) {
		this(values, windowSize, false);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param values
	 * @param windowSize           window size for rolling statistics
	 *                             (DescriptiveStatistics.INFINITE_WINDOW for no
	 *                             limit)
	 * @param forceDescriptiveMode if true, always use DescriptiveStatistics (exact
	 *                             statistics) regardless of dataset size
	 */
	public StatisticsSupport(List<Double> values, int windowSize, boolean forceDescriptiveMode) {
		this.windowSize = windowSize;
		this.forceDescriptiveMode = forceDescriptiveMode;

		// Don't pre-count - initialize optimistically for descriptive mode
		initializeForSize(Math.min(values.size(), DESCRIPTIVE_STATS_THRESHOLD), windowSize);
		for (Double d : values)
			if (d != null && !Double.isNaN(d))
				addValue(d);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param values
	 */
	public StatisticsSupport(Set<Double> values) {
		this(values, DEFAULT_WINDOW_SIZE, false);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param values
	 * @param forceDescriptiveMode if true, always use DescriptiveStatistics (exact
	 *                             statistics) regardless of dataset size
	 */
	public StatisticsSupport(Set<Double> values, boolean forceDescriptiveMode) {
		this(values, DEFAULT_WINDOW_SIZE, forceDescriptiveMode);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param values
	 * @param windowSize window size for rolling statistics
	 *                   (DescriptiveStatistics.INFINITE_WINDOW for no limit)
	 */
	public StatisticsSupport(Set<Double> values, int windowSize) {
		this(values, windowSize, false);
	}

	/**
	 * NANs are removed!
	 * 
	 * @param values
	 * @param windowSize           window size for rolling statistics
	 *                             (DescriptiveStatistics.INFINITE_WINDOW for no
	 *                             limit)
	 * @param forceDescriptiveMode if true, always use DescriptiveStatistics (exact
	 *                             statistics) regardless of dataset size
	 */
	public StatisticsSupport(Set<Double> values, int windowSize, boolean forceDescriptiveMode) {
		this.windowSize = windowSize;
		this.forceDescriptiveMode = forceDescriptiveMode;

		// Don't pre-count - initialize optimistically for descriptive mode
		initializeForSize(Math.min(values.size(), DESCRIPTIVE_STATS_THRESHOLD), windowSize);
		for (double d : values)
			if (!Double.isNaN(d))
				addValue(d);
	}

	/**
	 * Initialize the appropriate statistics implementation based on expected size
	 * 
	 * @param expectedSize expected number of values
	 * @param windowSize   window size for DescriptiveStatistics (or
	 *                     INFINITE_WINDOW)
	 */
	private void initializeForSize(int expectedSize, int windowSize) {
		// If forceDescriptiveMode is true, always use descriptive statistics
		if (forceDescriptiveMode) {
			useDescriptive = true;
			descriptiveStats = new DescriptiveStatistics();
			if (windowSize != DescriptiveStatistics.INFINITE_WINDOW && windowSize > 0) {
				descriptiveStats.setWindowSize(windowSize);
			}
			summaryStats = null;
			sampleCache = null;
			return;
		}

		// If window size is set and reasonable, always use descriptive mode
		// to support rolling window functionality
		if (windowSize != DescriptiveStatistics.INFINITE_WINDOW && windowSize > 0
				&& windowSize <= DESCRIPTIVE_STATS_THRESHOLD) {
			useDescriptive = true;
			descriptiveStats = new DescriptiveStatistics(windowSize);
			summaryStats = null;
			sampleCache = null;
		} else if (expectedSize <= DESCRIPTIVE_STATS_THRESHOLD) {
			// For small datasets without specific window, use descriptive
			useDescriptive = true;
			descriptiveStats = new DescriptiveStatistics();
			if (windowSize != DescriptiveStatistics.INFINITE_WINDOW && windowSize > 0) {
				descriptiveStats.setWindowSize(windowSize);
			}
			summaryStats = null;
			sampleCache = null;
		} else {
			// For large datasets, use summary statistics with reservoir sampling
			useDescriptive = false;
			descriptiveStats = null;
			summaryStats = new SummaryStatistics();
			sampleCache = new ArrayList<>(SAMPLE_CACHE_SIZE);
			totalValuesAdded = 0;

			if (windowSize != DescriptiveStatistics.INFINITE_WINDOW) {
				System.out.println("Note: Window size specified (" + windowSize + ") but dataset is large (n="
						+ expectedSize + "). Using reservoir sampling for approximate percentiles instead.");
			}
		}
	}

	/**
	 * Add a single value using reservoir sampling for large datasets
	 */
	public void addValue(double v) {
		if (useDescriptive) {
			// For descriptive mode, let the parent class handle everything
			// Cache invalidation is implicit through DescriptiveStatistics
			descriptiveStats.addValue(v);

			// Invalidate our wrapper caches
			median = Double.NaN;
			uniqueObservations = -1;

			// Only switch to summary mode if NOT forced and window is INFINITE and
			// threshold exceeded
			if (!forceDescriptiveMode && windowSize == DescriptiveStatistics.INFINITE_WINDOW
					&& getN() > DESCRIPTIVE_STATS_THRESHOLD)
				switchToSummaryMode();

		} else {
			// For summary mode, use reservoir sampling
			summaryStats.addValue(v);
			totalValuesAdded++;

			// Reservoir sampling: maintain a random sample of size SAMPLE_CACHE_SIZE
			if (sampleCache.size() < SAMPLE_CACHE_SIZE) {
				// Still filling the reservoir
				sampleCache.add(v);
			} else {
				// Reservoir is full - randomly replace with decreasing probability
				long j = ThreadLocalRandom.current().nextLong(totalValuesAdded); // 0..totalValuesAdded-1
				if (j < SAMPLE_CACHE_SIZE)
					sampleCache.set((int) j, v);
			}

			// Invalidate caches for summary mode
			sortedSampleCache = null;
			median = Double.NaN;
			uniqueObservations = -1;
		}
	}

	/**
	 * Switch from descriptive to summary mode when threshold is exceeded
	 */
	private void switchToSummaryMode() {
		if (!useDescriptive)
			return;

		// Don't switch if forced to stay in descriptive mode
		if (forceDescriptiveMode) {
			return;
		}

		// Warn if window size was set
		if (windowSize != DescriptiveStatistics.INFINITE_WINDOW) {
			System.out.println("Warning: Switching to summary mode. Window size functionality (" + windowSize
					+ ") will no longer be available. Using reservoir sampling for approximate percentiles.");
		}

		// Get all values before switching
		double[] values = descriptiveStats.getValues();

		// Initialize summary statistics and reservoir sampling
		summaryStats = new SummaryStatistics();
		sampleCache = new ArrayList<>(SAMPLE_CACHE_SIZE);
		totalValuesAdded = 0;

		// Transfer values to summary stats and build initial sample
		for (double v : values) {
			summaryStats.addValue(v);
			totalValuesAdded++;

			// Build reservoir sample
			if (sampleCache.size() < SAMPLE_CACHE_SIZE) {
				sampleCache.add(v);
			} else {
				long j = ThreadLocalRandom.current().nextLong(totalValuesAdded); // 0..totalValuesAdded-1
				if (j < SAMPLE_CACHE_SIZE)
					sampleCache.set((int) j, v);
			}
		}

		// Clear descriptive stats to free memory
		descriptiveStats.clear();
		descriptiveStats = null;
		useDescriptive = false;

		System.out.println("StatisticsSupport: Switched to memory-efficient mode for large dataset (n=" + getN()
				+ "). Using reservoir sampling (sample size=" + SAMPLE_CACHE_SIZE + ") for approximate percentiles.");
	}

	/**
	 * Adds a list of values efficiently
	 * 
	 * @param values
	 */
	public void addAll(List<Double> values) {
		// Batch add without resetting cache multiple times
		for (Double d : values) {
			if (d != null && !Double.isNaN(d)) {
				if (useDescriptive) {
					descriptiveStats.addValue(d);
				} else {
					summaryStats.addValue(d);
					totalValuesAdded++;

					// Reservoir sampling
					if (sampleCache.size() < SAMPLE_CACHE_SIZE) {
						sampleCache.add(d);
					} else {
						long j = ThreadLocalRandom.current().nextLong(totalValuesAdded); // 0..totalValuesAdded-1
						if (j < SAMPLE_CACHE_SIZE) {
							sampleCache.set((int) j, d);
						}
					}
				}
			}
		}

		// Single cache invalidation after all additions
		invalidateCache();

		// Only switch modes if NOT forced and window is INFINITE and threshold exceeded
		if (useDescriptive && !forceDescriptiveMode && windowSize == DescriptiveStatistics.INFINITE_WINDOW
				&& getN() > DESCRIPTIVE_STATS_THRESHOLD) {
			switchToSummaryMode();
		}
	}

	/**
	 * Invalidate cached computations
	 */
	private void invalidateCache() {
		median = Double.NaN;
		uniqueObservations = -1;
		sortedSampleCache = null; // Invalidate sorted sample when data changes
	}

	/**
	 * Get median. For large datasets, returns an approximate median based on
	 * reservoir sample.
	 * 
	 * @return median value (exact for small datasets, approximate for large
	 *         datasets)
	 */
	public double getMedian() {
		if (useDescriptive) {
			if (Double.isNaN(median))
				median = descriptiveStats.getPercentile(50);
			return median;
		} else {
			// For large datasets, use approximate median from sample
			return getApproximatePercentile(50.0);
		}
	}

	@Override
	public double getMean() {
		return useDescriptive ? descriptiveStats.getMean() : summaryStats.getMean();
	}

	/**
	 * Get count of values
	 */
	public int getCount() {
		return (int) getN();
	}

	@Override
	public double getMax() {
		return useDescriptive ? descriptiveStats.getMax() : summaryStats.getMax();
	}

	@Override
	public double getMin() {
		return useDescriptive ? descriptiveStats.getMin() : summaryStats.getMin();
	}

	/**
	 * Get percentile. For large datasets, returns an approximate percentile based
	 * on reservoir sample. Must be between 0 and 100
	 * 
	 * @param percent percentile to compute (0-100)
	 * @return percentile value (exact for small datasets, approximate for large
	 *         datasets)
	 */
	public double getPercentile(int percent) {
		return getPercentile((double) percent);
	}

	/**
	 * Get percentile. For large datasets, returns an approximate percentile based
	 * on reservoir sample. Must be between 0 and 100
	 * 
	 * @param percent percentile to compute (0-100)
	 * @return percentile value (exact for small datasets, approximate for large
	 *         datasets)
	 */
	public double getPercentile(double percent) {
		if (percent <= 0)
			return getMin();
		if (percent >= 100)
			return getMax();

		if (useDescriptive)
			return descriptiveStats.getPercentile(percent);
		else
			return getApproximatePercentile(percent);

	}

	/**
	 * Calculate approximate percentile from reservoir sample for large datasets
	 * 
	 * @param percent percentile to compute (0-100)
	 * @return approximate percentile value
	 */
	/**
	 * Calculate approximate percentile from reservoir sample for large datasets.
	 * Uses cached sorted sample for performance when multiple percentiles are
	 * queried.
	 * 
	 * @param percent percentile to compute (0-100)
	 * @return approximate percentile value
	 */
	private double getApproximatePercentile(double percent) {
		if (sampleCache == null || sampleCache.isEmpty())
			return Double.NaN;

		// Use cached sorted sample if available, otherwise create and cache it
		if (sortedSampleCache == null) {
			sortedSampleCache = new ArrayList<>(sampleCache);
			java.util.Collections.sort(sortedSampleCache);
		}

		// Calculate percentile position
		double pos = (percent / 100.0) * (sortedSampleCache.size() - 1);
		int lowerIndex = (int) Math.floor(pos);
		int upperIndex = (int) Math.ceil(pos);

		if (lowerIndex == upperIndex) {
			return sortedSampleCache.get(lowerIndex);
		}

		// Linear interpolation
		double lowerValue = sortedSampleCache.get(lowerIndex);
		double upperValue = sortedSampleCache.get(upperIndex);
		double fraction = pos - lowerIndex;

		return lowerValue + fraction * (upperValue - lowerValue);
	}

	@Override
	public double getStandardDeviation() {
		return useDescriptive ? descriptiveStats.getStandardDeviation() : summaryStats.getStandardDeviation();
	}

	@Override
	public double getVariance() {
		return useDescriptive ? descriptiveStats.getVariance() : summaryStats.getVariance();
	}

	/**
	 * Get geometric mean
	 */
	public double getGeometricMean() {
		return useDescriptive ? descriptiveStats.getGeometricMean() : summaryStats.getGeometricMean();
	}

	/**
	 * Get skewness. For large datasets, returns an approximate value based on
	 * reservoir sample.
	 * 
	 * @return skewness (exact for small datasets, approximate for large datasets)
	 */
	public double getSkewness() {
		if (useDescriptive) {
			return descriptiveStats.getSkewness();
		} else {
			// Approximate skewness from sample
			return calculateSampleSkewness();
		}
	}

	/**
	 * Get kurtosis. For large datasets, returns an approximate value based on
	 * reservoir sample.
	 * 
	 * @return kurtosis (exact for small datasets, approximate for large datasets)
	 */
	public double getKurtosis() {
		if (useDescriptive) {
			return descriptiveStats.getKurtosis();
		} else {
			// Approximate kurtosis from sample
			return calculateSampleKurtosis();
		}
	}

	/**
	 * Calculate approximate skewness from reservoir sample
	 */
	private double calculateSampleSkewness() {
		if (sampleCache == null || sampleCache.size() < 3)
			return Double.NaN;

		// Calculate mean
		double mean = 0.0;
		for (double v : sampleCache)
			mean += v;

		mean /= sampleCache.size();

		// Calculate moments
		double m2 = 0.0, m3 = 0.0;
		for (double v : sampleCache) {
			double diff = v - mean;
			m2 += diff * diff;
			m3 += diff * diff * diff;
		}

		m2 /= sampleCache.size();
		m3 /= sampleCache.size();

		if (m2 == 0)
			return 0.0;

		return m3 / Math.pow(m2, 1.5);
	}

	/**
	 * Calculate approximate kurtosis from reservoir sample
	 */
	private double calculateSampleKurtosis() {
		if (sampleCache == null || sampleCache.size() < 4)
			return Double.NaN;

		// Calculate mean
		double mean = 0.0;
		for (double v : sampleCache)
			mean += v;

		mean /= sampleCache.size();

		// Calculate moments
		double m2 = 0.0, m4 = 0.0;
		for (double v : sampleCache) {
			double diff = v - mean;
			double diff2 = diff * diff;
			m2 += diff2;
			m4 += diff2 * diff2;
		}

		m2 /= sampleCache.size();
		m4 /= sampleCache.size();

		if (m2 == 0)
			return 0.0;

		return (m4 / (m2 * m2)) - 3.0;
	}

	/**
	 * Number of different values. For large datasets, returns approximate count
	 * based on reservoir sample.
	 * 
	 * @return unique observation count (exact for small datasets, approximate for
	 *         large datasets)
	 */
	public int getCountUniqueObservations() {
		if (uniqueObservations == -1) {
			if (useDescriptive) {
				Set<Double> uniqueValues = new HashSet<>();
				for (double d : descriptiveStats.getValues()) {
					uniqueValues.add(d);
				}
				uniqueObservations = uniqueValues.size();
			} else {
				// For large data sets, estimate from sample
				if (sampleCache != null && !sampleCache.isEmpty()) {
					Set<Double> uniqueInSample = new HashSet<>(sampleCache);
					// Extrapolate: unique_total ~ unique_sample * (total_count / sample_size)
					// This is a rough approximation
					double ratio = (double) getN() / sampleCache.size();
					uniqueObservations = (int) Math.min(getN(), uniqueInSample.size() * ratio);
				} else
					return -1; // Unknown
			}
		}
		return uniqueObservations;
	}

	/**
	 * Get values array. For small data sets, returns all values. For large data sets,
	 * returns the reservoir sample (representative subset).
	 * 
	 * @return array of values (all values for small data sets, sample for large
	 *         data sets)
	 */
	public double[] getValues() {
		if (useDescriptive) {
			return descriptiveStats.getValues();
		} else {
			// Return reservoir sample for large data sets
			if (sampleCache != null)
				return sampleCache.stream().mapToDouble(Double::doubleValue).toArray();
			else
				return new double[0];

		}
	}

	/**
	 * Check if the current values represent a sample (for large datasets) or
	 * complete data
	 * 
	 * @return true if getValues() returns a sample, false if it returns all values
	 */
	public boolean isSample() {
		return !useDescriptive;
	}

	/**
	 * Get outliers based on quantile. For large datasets, operates on reservoir
	 * sample (approximate).
	 * 
	 * @param quantile the threshold percentile (e.g., 5.0 for 5th percentile)
	 * @return array of outlier values
	 */
	public double[] getOutliers(double quantile) {
		double dNotOutmin = getPercentile(quantile);
		double dNotOutmax = getPercentile(100.0 - quantile);

		List<Double> outliers = new ArrayList<>();

		if (useDescriptive) {
			for (double d : descriptiveStats.getValues())
				if (d < dNotOutmin || d > dNotOutmax)
					outliers.add(d);

		} else {
			// For large datasets, find outliers in sample
			if (sampleCache != null)
				for (double d : sampleCache)
					if (d < dNotOutmin || d > dNotOutmax)
						outliers.add(d);

		}

		return outliers.stream().mapToDouble(Double::doubleValue).toArray();
	}

	/**
	 * Predicts if a given variable is discrete by examining the ratio
	 * #uniques/#elements. For large datasets, uses approximation based on reservoir
	 * sample.
	 * 
	 * @param ratio the threshold ratio (0.01 means 1%)
	 * @return true if the ratio is smaller than the given parameter
	 */
	public boolean isLikelyDiscrete(double ratio) {
		int uniqueCount = getCountUniqueObservations();
		if (uniqueCount < 0) {
			// Unable to determine
			return false;
		}

		double ratioObserved = (double) uniqueCount / getN();
		return (ratioObserved < ratio);
	}

	@Override
	public Iterator<Double> iterator() {
		if (useDescriptive)
			return DataConversion.doubleToList(descriptiveStats.getValues()).iterator();
		else if (sampleCache != null)
			return new ArrayList<>(sampleCache).iterator();
		else
			return java.util.Collections.emptyIterator();

	}

	/**
	 * Calculates the Entropy for the value distribution. Should only be applied for
	 * positive values. Negative values will be inverted. For large datasets,
	 * calculates from reservoir sample (approximate).
	 * 
	 * @return entropy value
	 */
	public double getEntropy() {
		if (getN() == 0)
			return 0;

		double entropy = 0.0;
		for (Iterator<Double> iter = iterator(); iter.hasNext();) {
			Double d = iter.next();
			if (d > 0)
				entropy -= (d * Math.log(d));
			else if (d < 0)
				entropy -= (Math.abs(d) * Math.log(Math.abs(d)));

		}

		entropy /= Math.log(2.0);
		return entropy;
	}

	@Override
	public long getN() {
		return useDescriptive ? descriptiveStats.getN() : summaryStats.getN();
	}

	@Override
	public double getSum() {
		return useDescriptive ? descriptiveStats.getSum() : summaryStats.getSum();
	}

	/**
	 * Get sum of squares
	 */
	public double getSumsq() {
		return useDescriptive ? descriptiveStats.getSumsq() : summaryStats.getSumsq();
	}

	/**
	 * Clear all statistics
	 */
	public void clear() {
		invalidateCache();
		if (useDescriptive) {
			descriptiveStats.clear();
		} else {
			summaryStats.clear();
			if (sampleCache != null)
				sampleCache.clear();

			totalValuesAdded = 0;
		}
	}

	/**
	 * Get the current window size (only applicable in descriptive mode)
	 * 
	 * @return window size, or INFINITE_WINDOW if no limit
	 */
	public int getWindowSize() {
		if (useDescriptive) {
			return descriptiveStats.getWindowSize();
		}
		return DescriptiveStatistics.INFINITE_WINDOW;
	}

	/**
	 * Set the window size for rolling statistics (only applicable in descriptive
	 * mode). This allows you to maintain a sliding window of the most recent N
	 * values.
	 * 
	 * @param windowSize the number of most recent values to maintain, or
	 *                   INFINITE_WINDOW for no limit
	 */
	public void setWindowSize(int windowSize) {
		if (!useDescriptive) {
			throw new UnsupportedOperationException("Window size cannot be set in summary mode (large datasets). "
					+ "Current size: " + getN() + ". Window functionality requires descriptive mode.");
		}
		this.windowSize = windowSize;
		descriptiveStats.setWindowSize(windowSize);
		invalidateCache();
	}

	/**
	 * Check if currently using descriptive statistics mode (small dataset)
	 */
	public boolean isUsingDescriptiveMode() {
		return useDescriptive;
	}

	/**
	 * Check if descriptive mode is forced regardless of dataset size
	 * 
	 * @return true if descriptive mode is forced
	 */
	public boolean isForceDescriptiveMode() {
		return forceDescriptiveMode;
	}

	/**
	 * Get the threshold for switching between modes
	 */
	public static int getThreshold() {
		return DESCRIPTIVE_STATS_THRESHOLD;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("StatisticsSupport [mode=").append(useDescriptive ? "Descriptive (exact)" : "Summary (approximate)");
		if (forceDescriptiveMode) {
			sb.append(" - FORCED");
		}
		sb.append("]\n");
		sb.append("n: ").append(getN()).append("\n");
		sb.append("min: ").append(getMin()).append("\n");
		sb.append("max: ").append(getMax()).append("\n");
		sb.append("mean: ").append(getMean()).append("\n");
		sb.append("std dev: ").append(getStandardDeviation()).append("\n");

		try {
			sb.append("median: ").append(getMedian());
			if (!useDescriptive) {
				sb.append(" (approx)");
			}
			sb.append("\n");

			if (useDescriptive) {
				sb.append("skewness: ").append(getSkewness()).append("\n");
				sb.append("kurtosis: ").append(getKurtosis()).append("\n");
			} else {
				sb.append("skewness: ").append(getSkewness()).append(" (approx from sample)\n");
				sb.append("kurtosis: ").append(getKurtosis()).append(" (approx from sample)\n");
				sb.append("sample size: ").append(sampleCache != null ? sampleCache.size() : 0).append("\n");
			}
		} catch (Exception e) {
			// Skip if not available
		}

		return sb.toString();
	}
}
