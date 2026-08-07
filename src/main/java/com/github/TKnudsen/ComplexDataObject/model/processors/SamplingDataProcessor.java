package com.github.TKnudsen.ComplexDataObject.model.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.model.processors.complexDataObject.DataProcessingCategory;

/**
 * Applies random sampling to reduce a collection to a target size while
 * PRESERVING THE ORIGINAL ORDER of elements.
 *
 * <p>
 * <b>Order Preservation:</b> Sampled elements maintain their relative order
 * from the input list. For example, if the input is [A, B, C, D, E] and we
 * sample 3 elements, the result might be [A, C, E] but never [E, A, C].
 * </p>
 *
 * <p>
 * <b>Adaptive Algorithm:</b> Automatically selects the optimal sampling
 * strategy based on data size and sampling ratio:
 * <ul>
 * <li><b>Small datasets (&lt; 10,000):</b> Random index selection (simple)</li>
 * <li><b>High sampling ratio (&gt; 50%):</b> Select indices to remove (fewer
 * operations)</li>
 * <li><b>Low sampling ratio (&lt; 50%):</b> Select indices to keep (memory
 * efficient)</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Performance:</b> O(n) for index selection + O(n) for list modification =
 * O(n) total
 * </p>
 *
 * <p>
 * <b>Thread Safety:</b> Each call to {@link #process(List)} creates a new
 * Random instance with the configured seed, ensuring deterministic behavior.
 * </p>
 *
 * <p>
 * <b>Mutation:</b> This processor MODIFIES the input list in-place.
 * </p>
 *
 * @version 2.0 (optimized)
 * @since 2020
 */
public class SamplingDataProcessor<T> implements IDataProcessor<T> {

	// ==================== CONSTANTS ====================

	/**
	 * Default seed for reproducible random sampling.
	 */
	private static final int DEFAULT_SEED = 42;

	/**
	 * Threshold below which we use simple algorithm (small dataset optimization).
	 */
	private static final int SMALL_DATASET_THRESHOLD = 10_000;

	/**
	 * Sampling ratio threshold for choosing between "select to keep" vs "select to
	 * remove". If (targetSize / dataSize) > this threshold, select indices to
	 * remove. Otherwise, select indices to keep.
	 */
	private static final double HIGH_SAMPLING_RATIO_THRESHOLD = 0.5;

	/**
	 * Enable detailed logging of algorithm selection and performance.
	 */
	private static final boolean ENABLE_LOGGING = true;

	// ==================== FIELDS ====================

	private final int targetSize;
	private final int seed;
	private final SamplingStrategy forcedStrategy;

	// ==================== ENUMS ====================

	/**
	 * Available order-preserving sampling strategies.
	 */
	public enum SamplingStrategy {
		/**
		 * Automatically select best strategy based on data characteristics.
		 */
		AUTO,

		/**
		 * Random index selection with simple removal. Best for small datasets. O(n)
		 * time, O(k) space where k = targetSize.
		 */
		SIMPLE,

		/**
		 * Select indices to keep, extract those elements. Best for low sampling ratio
		 * (&lt;50%). O(n) time, O(k) space where k = targetSize.
		 */
		SELECT_TO_KEEP,

		/**
		 * Select indices to remove, remove in reverse order. Best for high sampling
		 * ratio (&gt;50%). O(n) time, O(n-k) space where k = targetSize.
		 */
		SELECT_TO_REMOVE
	}

	// ==================== CONSTRUCTORS ====================

	/**
	 * Creates a sampling processor with default seed and automatic strategy
	 * selection.
	 * 
	 * @param targetSize Desired number of elements after sampling
	 * @throws IllegalArgumentException if targetSize is negative
	 */
	public SamplingDataProcessor(int targetSize) {
		this(targetSize, DEFAULT_SEED, SamplingStrategy.AUTO);
	}

	/**
	 * Creates a sampling processor with custom seed and automatic strategy
	 * selection.
	 * 
	 * @param targetSize Desired number of elements after sampling
	 * @param seed       Random seed for reproducible sampling
	 * @throws IllegalArgumentException if targetSize is negative
	 */
	public SamplingDataProcessor(int targetSize, int seed) {
		this(targetSize, seed, SamplingStrategy.AUTO);
	}

	/**
	 * Creates a sampling processor with forced strategy (for testing/benchmarking).
	 * 
	 * @param targetSize     Desired number of elements after sampling
	 * @param seed           Random seed for reproducible sampling
	 * @param forcedStrategy Strategy to use (or AUTO for automatic selection)
	 * @throws IllegalArgumentException if targetSize is negative
	 */
	public SamplingDataProcessor(int targetSize, int seed, SamplingStrategy forcedStrategy) {
		if (targetSize < 0) {
			throw new IllegalArgumentException("Target size cannot be negative: " + targetSize);
		}
		this.targetSize = targetSize;
		this.seed = seed;
		this.forcedStrategy = Objects.requireNonNull(forcedStrategy, "Strategy cannot be null");
	}

	// ==================== MAIN PROCESSING ====================

	/**
	 * Reduces the input list to the target size using adaptive random sampling
	 * while PRESERVING the original order of elements.
	 * 
	 * <p>
	 * <b>IMPORTANT:</b> This method MODIFIES the input list in-place.
	 * </p>
	 * <p>
	 * <b>ORDER PRESERVATION:</b> Sampled elements maintain their relative order.
	 * </p>
	 * 
	 * @param data List to sample (will be modified in-place, order preserved)
	 * @throws NullPointerException if data is null
	 */
	@Override
	public void process(List<T> data) {
		Objects.requireNonNull(data, "Data list cannot be null");

		int originalSize = data.size();

		// Early exit if no sampling needed
		if (originalSize <= targetSize) {
			if (originalSize < targetSize && ENABLE_LOGGING) {
				System.out.println("SamplingDataProcessor: Data size (" + originalSize
						+ ") is smaller than target size (" + targetSize + "), no sampling performed");
			}
			return;
		}

		long startTime = System.nanoTime();

		// Select strategy
		SamplingStrategy strategy = selectStrategy(originalSize, targetSize);

		// Execute appropriate algorithm
		Random random = new Random(seed);

		switch (strategy) {
		case SIMPLE:
			simpleSample(data, random);
			break;
		case SELECT_TO_KEEP:
			selectToKeepSample(data, random);
			break;
		case SELECT_TO_REMOVE:
			selectToRemoveSample(data, random);
			break;
		default:
			throw new IllegalStateException("Unknown strategy: " + strategy);
		}

		long elapsedNanos = System.nanoTime() - startTime;

		if (ENABLE_LOGGING) {
			System.out.println(String.format(
					"SamplingDataProcessor: Sampled %,d from %,d elements (%.1f%%) using %s in %.3fms (order preserved)",
					targetSize, originalSize, (100.0 * targetSize / originalSize), strategy,
					elapsedNanos / 1_000_000.0));
		}
	}

	// ==================== STRATEGY SELECTION ====================

	/**
	 * Selects the optimal sampling strategy based on data characteristics.
	 * 
	 * @param dataSize   Current size of the dataset
	 * @param targetSize Desired size after sampling
	 * @return Selected strategy
	 */
	private SamplingStrategy selectStrategy(int dataSize, int targetSize) {
		// If strategy is forced (for testing), use it
		if (forcedStrategy != SamplingStrategy.AUTO) {
			return forcedStrategy;
		}

		// Calculate sampling ratio
		double samplingRatio = (double) targetSize / dataSize;

		// Decision tree for optimal strategy
		if (dataSize < SMALL_DATASET_THRESHOLD) {
			// Small datasets: simple algorithm is fastest due to low overhead
			return SamplingStrategy.SIMPLE;
		} else if (samplingRatio > HIGH_SAMPLING_RATIO_THRESHOLD) {
			// High sampling ratio: select which to REMOVE (fewer removals)
			// Example: keep 900K of 1M to remove 100K (fewer operations)
			return SamplingStrategy.SELECT_TO_REMOVE;
		} else {
			// Low sampling ratio: select which to KEEP (less memory)
			// Example: keep 10K of 1M to select 10K indices (less memory)
			return SamplingStrategy.SELECT_TO_KEEP;
		}
	}

	// ==================== ALGORITHM IMPLEMENTATIONS ====================

	/**
	 * Simple sampling: Randomly select targetSize indices, sort them, extract
	 * elements.
	 * 
	 * <p>
	 * <b>Complexity:</b> O(n) time, O(k) space where k = targetSize
	 * </p>
	 * <p>
	 * <b>Best for:</b> Small datasets where simplicity matters
	 * </p>
	 * <p>
	 * <b>Order:</b> Preserved (indices are sorted before extraction)
	 * </p>
	 */
	private void simpleSample(List<T> data, Random random) {
		int dataSize = data.size();

		// Generate targetSize unique random indices
		List<Integer> selectedIndices = selectRandomIndices(dataSize, targetSize, random);

		// Sort indices to maintain order
		Collections.sort(selectedIndices);

		// Extract elements at selected indices
		List<T> sampledElements = new ArrayList<>(targetSize);
		for (int index : selectedIndices)
			sampledElements.add(data.get(index));

		// Replace original list contents
		data.clear();
		data.addAll(sampledElements);
	}

	/**
	 * Select-to-keep: Choose which indices to keep, extract those elements.
	 * 
	 * <p>
	 * <b>Complexity:</b> O(n) time, O(k) space where k = targetSize
	 * </p>
	 * <p>
	 * <b>Best for:</b> Large datasets with low sampling ratio (&lt;50%)
	 * </p>
	 * <p>
	 * <b>Order:</b> Preserved (indices are sorted before extraction)
	 * </p>
	 * 
	 * <p>
	 * More memory efficient than select-to-remove when keeping few elements.
	 * </p>
	 */
	private void selectToKeepSample(List<T> data, Random random) {
		int dataSize = data.size();

		// Generate targetSize unique random indices
		List<Integer> indicesToKeep = selectRandomIndices(dataSize, targetSize, random);

		// Sort indices to maintain order
		Collections.sort(indicesToKeep);

		// Extract elements at selected indices
		List<T> sampledElements = new ArrayList<>(targetSize);
		for (int index : indicesToKeep)
			sampledElements.add(data.get(index));

		// Replace original list contents
		data.clear();
		data.addAll(sampledElements);
	}

	/**
	 * Select-to-remove: Choose which indices to remove, remove them in reverse
	 * order.
	 * 
	 * <p>
	 * <b>Complexity:</b> O(n) time, O(n-k) space where k = targetSize
	 * </p>
	 * <p>
	 * <b>Best for:</b> Large datasets with high sampling ratio (&gt;50%)
	 * </p>
	 * <p>
	 * <b>Order:</b> Preserved (remaining elements maintain their order)
	 * </p>
	 * 
	 * <p>
	 * More efficient than select-to-keep when keeping most elements, as we perform
	 * fewer operations (remove minority rather than rebuild majority).
	 * </p>
	 */
	private void selectToRemoveSample(List<T> data, Random random) {
		int dataSize = data.size();
		int removeCount = dataSize - targetSize;

		// Generate removeCount unique random indices to remove
		List<Integer> indicesToRemove = selectRandomIndices(dataSize, removeCount, random);

		// Sort in DESCENDING order to remove from end first
		// This is critical for ArrayList performance (avoid shifting)
		Collections.sort(indicesToRemove, Collections.reverseOrder());

		// Remove elements at selected indices (from end to start)
		for (int index : indicesToRemove)
			data.remove(index);

	}

	// ==================== HELPER METHODS ====================

	/**
	 * Select 'count' unique random indices from range [0, maxIndex).
	 * 
	 * <p>
	 * Uses Floyd's algorithm for efficient random sampling without replacement.
	 * </p>
	 * 
	 * <p>
	 * <b>Complexity:</b> O(count) time and space
	 * </p>
	 * 
	 * @param maxIndex Upper bound (exclusive)
	 * @param count    Number of indices to select
	 * @param random   Random generator
	 * @return List of unique random indices (unsorted)
	 */
	private List<Integer> selectRandomIndices(int maxIndex, int count, Random random) {
		// Floyd's algorithm for random sampling
		// Generates exactly 'count' unique indices in O(count) time

		Set<Integer> selectedSet = new HashSet<>(count);

		for (int i = maxIndex - count; i < maxIndex; i++) {
			int randomIndex = random.nextInt(i + 1);

			// If we already selected this index, use i instead
			if (!selectedSet.add(randomIndex))
				selectedSet.add(i);

		}

		return new ArrayList<>(selectedSet);
	}

	// ==================== IDATA PROCESSOR ====================

	@Override
	public DataProcessingCategory getPreprocessingCategory() {
		return DataProcessingCategory.DATA_REDUCTION;
	}

	// ==================== GETTERS ====================

	/**
	 * Get the target size for sampling.
	 */
	public int getTargetSize() {
		return targetSize;
	}

	/**
	 * Get the random seed used for sampling.
	 */
	public int getSeed() {
		return seed;
	}

	/**
	 * Get the forced strategy (or AUTO if using adaptive selection).
	 */
	public SamplingStrategy getForcedStrategy() {
		return forcedStrategy;
	}

	/**
	 * Get a descriptive name for this processor.
	 */
	@Override
	public String toString() {
		return "SamplingDataProcessor[targetSize=" + targetSize + ", seed=" + seed + ", strategy=" + forcedStrategy
				+ ", orderPreserving=true]";
	}

	// ==================== STATIC UTILITY ====================

	/**
	 * Estimate which strategy would be chosen for given data characteristics.
	 * 
	 * @param dataSize   Size of dataset
	 * @param targetSize Target sample size
	 * @return Strategy that would be selected
	 */
	public static SamplingStrategy estimateStrategy(int dataSize, int targetSize) {
		if (dataSize <= targetSize)
			return null; // No sampling needed

		double samplingRatio = (double) targetSize / dataSize;

		if (dataSize < SMALL_DATASET_THRESHOLD) {
			return SamplingStrategy.SIMPLE;
		} else if (samplingRatio > HIGH_SAMPLING_RATIO_THRESHOLD) {
			return SamplingStrategy.SELECT_TO_REMOVE;
		} else {
			return SamplingStrategy.SELECT_TO_KEEP;
		}
	}
}