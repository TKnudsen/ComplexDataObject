package com.github.TKnudsen.ComplexDataObject.data.distanceMatrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.ToDoubleBiFunction;

/**
 * Computes and stores a full pairwise distance matrix for a fixed set of
 * elements.
 * <p>
 * This implementation targets workloads where individual distance evaluations
 * are relatively expensive (e.g., high-dimensional feature vectors) and where
 * repeated matrix construction should avoid per-instance thread pool setup
 * overhead.
 * </p>
 *
 * <h2>Computation model</h2>
 * <ul>
 * <li>Distances are computed once during construction and stored in a dense
 * {@code double[n][n]} matrix.</li>
 * <li>For symmetric distance measures, only the upper triangle ({@code i < j})
 * is evaluated and then mirrored.</li>
 * <li>Blocked/tiled traversal is used to improve cache locality for
 * high-dimensional data access patterns.</li>
 * <li>Parallel execution uses a shared fixed thread pool to amortize thread
 * creation costs across instances.</li>
 * </ul>
 *
 * <h2>Min/Max tracking</h2> The global minimum and maximum non-NaN distance
 * values are tracked during matrix construction and exposed via
 * {@link #getGlobalMinDistance()} and {@link #getGlobalMaxDistance()} for O(1)
 * access. If all computed distances are {@code NaN}, the corresponding global
 * value is {@code NaN}.
 *
 * <h2>Threading and life cycle</h2> Parallel computation is enabled based on the
 * constructor flag and a size threshold. Parallel work is executed on a shared
 * {@link ExecutorService}. The shared executor is intentionally not shut down
 * per instance; applications may call {@link #shutdownSharedExecutor()} once at
 * process shutdown if desired.
 *
 * <h2>Complexity</h2>
 * <ul>
 * <li>Time: O(n^2) distance evaluations (half for symmetric measures)</li>
 * <li>Memory: O(n^2) doubles</li>
 * </ul>
 *
 * @param <T> element type for which pairwise distances are computed
 */
public class DistanceMatrixBlockedParallel<T> implements IDistanceMatrix<T> {

	/** Enables parallel execution only for matrices at least this size (n). */
	private static final int PARALLEL_THRESHOLD = 100;

	/** Number of worker threads used by the shared executor. */
	private static final int CORES = Runtime.getRuntime().availableProcessors();

	/**
	 * Tile size used for blocked traversal. Chosen to improve cache locality when
	 * the distance measure accesses high-dimensional data.
	 */
	private static final int BLOCK_SIZE = 32;

	/**
	 * Reciprocal of {@link #BLOCK_SIZE} used for micro-optimizations in index
	 * computations. Note: This is used in a hot path; ensure any strength-reduction
	 * arithmetic remains correct for your intended mapping.
	 */
	private static final double BLOCK_SIZE_RECIPROCAL = 1.0 / BLOCK_SIZE;

	/**
	 * Shared pool to amortize thread creation across multiple matrix constructions.
	 * This executor is not shut down automatically per instance.
	 */
	private static final ExecutorService SHARED_EXECUTOR = Executors.newFixedThreadPool(CORES, r -> {
		Thread t = new Thread(r, "DistanceMatrix-worker");
		t.setDaemon(true);
		return t;
	});

	private final List<? extends T> elements;
	private final Object[] elementArray;
	private final Map<T, Integer> indices;
	private final boolean symmetric;
	private final boolean parallel;

	private final double[][] matrix;

	private final double globalMinDistance;
	private final double globalMaxDistance;

	/**
	 * Constructs a distance matrix for the given elements using a symmetric
	 * distance measure and enabling parallel computation (subject to internal
	 * thresholding).
	 *
	 * @param elements        the elements for which pairwise distances are computed
	 * @param distanceMeasure function computing the distance between two elements
	 * @throws NullPointerException if {@code elements} or {@code distanceMeasure}
	 *                              is {@code null}
	 */
	public DistanceMatrixBlockedParallel(List<? extends T> elements,
			ToDoubleBiFunction<? super T, ? super T> distanceMeasure) {
		this(elements, distanceMeasure, true, true);
	}

	/**
	 * Constructs a distance matrix for the given elements.
	 * <p>
	 * The matrix is fully computed during construction.
	 * </p>
	 *
	 * @param elements        the elements for which pairwise distances are computed
	 * @param distanceMeasure function computing the distance between two elements
	 * @param symmetric       whether the distance function is symmetric (i.e.,
	 *                        d(a,b)=d(b,a)). If {@code true}, only the upper
	 *                        triangle is computed and mirrored.
	 * @param parallel        whether parallel computation is allowed (subject to
	 *                        threshold and CPU count)
	 * @throws NullPointerException if {@code elements} or {@code distanceMeasure}
	 *                              is {@code null}
	 */
	public DistanceMatrixBlockedParallel(List<? extends T> elements,
			ToDoubleBiFunction<? super T, ? super T> distanceMeasure, boolean symmetric, boolean parallel) {

		this.elements = Objects.requireNonNull(elements, "elements");
		Objects.requireNonNull(distanceMeasure, "distanceMeasure");

		this.symmetric = symmetric;
		this.parallel = parallel;

		final int n = elements.size();

		// Snapshot to array for faster indexed access in hot loops.
		this.elementArray = elements.toArray();

		// Capacity optimization to avoid rehashing during index construction.
		this.indices = new HashMap<>((int) (n * 1.34f) + 1);
		for (int i = 0; i < n; i++) {
			@SuppressWarnings("unchecked")
			T e = (T) elementArray[i];
			indices.put(e, i);
		}

		this.matrix = new double[n][n];

		final MinMaxResult mm;
		if (parallel && n >= PARALLEL_THRESHOLD && CORES > 1) {
			mm = computeParallelBlocked(distanceMeasure, n);
		} else {
			mm = computeSequentialBlocked(distanceMeasure, n);
		}

		this.globalMinDistance = mm.min;
		this.globalMaxDistance = mm.max;
	}

	/**
	 * Computes the full distance matrix in a single thread using blocked traversal.
	 * <p>
	 * For symmetric matrices, only {@code i < j} is evaluated and mirrored into
	 * {@code [j][i]}. For non-symmetric matrices, all {@code (i,j)} pairs are
	 * evaluated.
	 * </p>
	 *
	 * @param distanceMeasure function computing the distance between two elements
	 * @param n               number of elements
	 * @return min/max result over all computed distances (excluding NaN)
	 */
	private MinMaxResult computeSequentialBlocked(ToDoubleBiFunction<? super T, ? super T> distanceMeasure, int n) {

		double minVal = Double.POSITIVE_INFINITY;
		double maxVal = Double.NEGATIVE_INFINITY;

		final Object[] elems = this.elementArray;
		final double[][] mat = this.matrix;

		if (symmetric) {
			final int numBlocks = (n + BLOCK_SIZE - 1) / BLOCK_SIZE;

			for (int iBlockIdx = 0; iBlockIdx < numBlocks; iBlockIdx++) {
				final int iBlock = iBlockIdx * BLOCK_SIZE;
				final int iEnd = Math.min(iBlock + BLOCK_SIZE, n);

				for (int jBlockIdx = iBlockIdx; jBlockIdx < numBlocks; jBlockIdx++) {
					final int jBlock = jBlockIdx * BLOCK_SIZE;
					final int jEnd = Math.min(jBlock + BLOCK_SIZE, n);
					final boolean isDiagonalBlock = (iBlockIdx == jBlockIdx);

					for (int i = iBlock; i < iEnd; i++) {
						@SuppressWarnings("unchecked")
						final T ti = (T) elems[i];

						final double[] rowI = mat[i];
						final int jStart = isDiagonalBlock ? i + 1 : jBlock;

						for (int j = jStart; j < jEnd; j++) {
							@SuppressWarnings("unchecked")
							final T tj = (T) elems[j];

							final double d = distanceMeasure.applyAsDouble(ti, tj);

							rowI[j] = d;
							mat[j][i] = d;

							final boolean valid = !Double.isNaN(d);
							minVal = valid & (d < minVal) ? d : minVal;
							maxVal = valid & (d > maxVal) ? d : maxVal;
						}
					}
				}
			}
		} else {
			final int numBlocks = (n + BLOCK_SIZE - 1) / BLOCK_SIZE;

			for (int iBlockIdx = 0; iBlockIdx < numBlocks; iBlockIdx++) {
				final int iBlock = iBlockIdx * BLOCK_SIZE;
				final int iEnd = Math.min(iBlock + BLOCK_SIZE, n);

				for (int jBlockIdx = 0; jBlockIdx < numBlocks; jBlockIdx++) {
					final int jBlock = jBlockIdx * BLOCK_SIZE;
					final int jEnd = Math.min(jBlock + BLOCK_SIZE, n);

					for (int i = iBlock; i < iEnd; i++) {
						@SuppressWarnings("unchecked")
						final T ti = (T) elems[i];

						final double[] rowI = mat[i];

						for (int j = jBlock; j < jEnd; j++) {
							@SuppressWarnings("unchecked")
							final T tj = (T) elems[j];

							final double d = distanceMeasure.applyAsDouble(ti, tj);
							rowI[j] = d;

							final boolean valid = !Double.isNaN(d);
							minVal = valid & (d < minVal) ? d : minVal;
							maxVal = valid & (d > maxVal) ? d : maxVal;
						}
					}
				}
			}
		}

		return normalizeMinMax(minVal, maxVal);
	}

	/**
	 * Computes the full distance matrix in parallel using blocked traversal and
	 * static work assignment.
	 * <p>
	 * Work is partitioned by block index. Each worker computes a disjoint set of
	 * blocks and maintains local min/max values that are merged after all tasks
	 * complete.
	 * </p>
	 *
	 * @param distanceMeasure function computing the distance between two elements
	 * @param n               number of elements
	 * @return min/max result over all computed distances (excluding NaN)
	 * @throws RuntimeException if task execution fails
	 */
	private MinMaxResult computeParallelBlocked(ToDoubleBiFunction<? super T, ? super T> distanceMeasure, int n) {

		final int numBlocks = (n + BLOCK_SIZE - 1) / BLOCK_SIZE;
		final int numThreads = Math.min(CORES, numBlocks);

		try {
			@SuppressWarnings("unchecked")
			Future<MinMaxResult>[] futures = new Future[numThreads];

			for (int t = 0; t < numThreads; t++) {
				final int threadId = t;
				futures[t] = SHARED_EXECUTOR
						.submit(() -> computeThreadBlocked(distanceMeasure, n, numBlocks, threadId, numThreads));
			}

			double globalMin = Double.POSITIVE_INFINITY;
			double globalMax = Double.NEGATIVE_INFINITY;

			for (int t = 0; t < numThreads; t++) {
				MinMaxResult r = futures[t].get();
				globalMin = r.min < globalMin ? r.min : globalMin;
				globalMax = r.max > globalMax ? r.max : globalMax;
			}

			return normalizeMinMax(globalMin, globalMax);

		} catch (Exception e) {
			throw new RuntimeException("Parallel computation failed", e);
		}
	}

	/**
	 * Computes a subset of blocks assigned to the given worker.
	 * <p>
	 * For symmetric matrices, each worker is assigned a subset of i-blocks and
	 * processes all {@code jBlock >= iBlock} blocks, mirroring computed values into
	 * the lower triangle. For non-symmetric matrices, each worker is assigned a
	 * subset of all block pairs.
	 * </p>
	 *
	 * @param distanceMeasure function computing the distance between two elements
	 * @param n               number of elements
	 * @param numBlocks       number of blocks along a matrix dimension
	 * @param threadId        worker id in {@code [0, numThreads)}
	 * @param numThreads      number of workers
	 * @return local min/max for this worker (excluding NaN)
	 */
	private MinMaxResult computeThreadBlocked(ToDoubleBiFunction<? super T, ? super T> distanceMeasure, int n,
			int numBlocks, int threadId, int numThreads) {

		double localMin = Double.POSITIVE_INFINITY;
		double localMax = Double.NEGATIVE_INFINITY;

		final Object[] elems = this.elementArray;
		final double[][] mat = this.matrix;

		if (symmetric) {
			for (int blockIdx = threadId; blockIdx < numBlocks; blockIdx += numThreads) {
				final int iBlock = blockIdx * BLOCK_SIZE;
				final int iEnd = Math.min(iBlock + BLOCK_SIZE, n);

				for (int jBlockIdx = blockIdx; jBlockIdx < numBlocks; jBlockIdx++) {
					final int jBlock = jBlockIdx * BLOCK_SIZE;
					final int jEnd = Math.min(jBlock + BLOCK_SIZE, n);
					final boolean isDiagonalBlock = (blockIdx == jBlockIdx);

					for (int i = iBlock; i < iEnd; i++) {
						@SuppressWarnings("unchecked")
						final T ti = (T) elems[i];

						final double[] rowI = mat[i];
						final int jStart = isDiagonalBlock ? i + 1 : jBlock;

						for (int j = jStart; j < jEnd; j++) {
							@SuppressWarnings("unchecked")
							final T tj = (T) elems[j];

							final double d = distanceMeasure.applyAsDouble(ti, tj);

							rowI[j] = d;
							mat[j][i] = d;

							final boolean valid = !Double.isNaN(d);
							localMin = valid & (d < localMin) ? d : localMin;
							localMax = valid & (d > localMax) ? d : localMax;
						}
					}
				}
			}
		} else {
			final int totalBlocks = numBlocks * numBlocks;

			for (int blockIdx = threadId; blockIdx < totalBlocks; blockIdx += numThreads) {
				final int iBlockNum = blockIdx / numBlocks;
				final int jBlockNum = blockIdx - iBlockNum * numBlocks; // faster than %

				final int iBlock = iBlockNum * BLOCK_SIZE;
				final int jBlock = jBlockNum * BLOCK_SIZE;

				final int iEnd = Math.min(iBlock + BLOCK_SIZE, n);
				final int jEnd = Math.min(jBlock + BLOCK_SIZE, n);

				for (int i = iBlock; i < iEnd; i++) {
					@SuppressWarnings("unchecked")
					final T ti = (T) elems[i];

					final double[] rowI = mat[i];

					for (int j = jBlock; j < jEnd; j++) {
						@SuppressWarnings("unchecked")
						final T tj = (T) elems[j];

						final double d = distanceMeasure.applyAsDouble(ti, tj);
						rowI[j] = d;

						final boolean valid = !Double.isNaN(d);
						localMin = valid & (d < localMin) ? d : localMin;
						localMax = valid & (d > localMax) ? d : localMax;
					}
				}
			}
		}

		return new MinMaxResult(localMin, localMax);
	}

	/**
	 * Normalizes min/max values to {@code NaN} if no valid distances were observed.
	 *
	 * @param minVal current minimum (or {@link Double#POSITIVE_INFINITY} if none)
	 * @param maxVal current maximum (or {@link Double#NEGATIVE_INFINITY} if none)
	 * @return normalized min/max holder
	 */
	private static MinMaxResult normalizeMinMax(double minVal, double maxVal) {
		minVal = minVal == Double.POSITIVE_INFINITY ? Double.NaN : minVal;
		maxVal = maxVal == Double.NEGATIVE_INFINITY ? Double.NaN : maxVal;
		return new MinMaxResult(minVal, maxVal);
	}

	/**
	 * Immutable holder for min/max values computed during matrix construction.
	 */
	private static final class MinMaxResult {
		final double min;
		final double max;

		MinMaxResult(double min, double max) {
			this.min = min;
			this.max = max;
		}
	}

	/**
	 * Returns the pre-computed distance between the two given elements.
	 *
	 * @param t first element (must be contained in this matrix)
	 * @param u second element (must be contained in this matrix)
	 * @return the distance value stored at {@code [index(t)][index(u)]}
	 * @throws IllegalArgumentException if either element is not contained in this
	 *                                  matrix
	 */
	@Override
	public double applyAsDouble(T t, T u) {
		Integer i = indices.get(t);
		Integer j = indices.get(u);

		if (i == null || j == null) {
			throw new IllegalArgumentException("Element not in matrix");
		}
		return matrix[i][j];
	}

	/**
	 * Convenience wrapper for {@link #applyAsDouble(Object, Object)}.
	 *
	 * @param o1 first element
	 * @param o2 second element
	 * @return distance between {@code o1} and {@code o2}
	 * @throws IllegalArgumentException if either element is not contained in this
	 *                                  matrix
	 */
	@Override
	public double getDistance(T o1, T o2) {
		return applyAsDouble(o1, o2);
	}

	/**
	 * @return the minimum non-NaN distance observed during matrix construction, or
	 *         {@code NaN} if none
	 */
	public double getGlobalMinDistance() {
		return globalMinDistance;
	}

	/**
	 * @return the maximum non-NaN distance observed during matrix construction, or
	 *         {@code NaN} if none
	 */
	public double getGlobalMaxDistance() {
		return globalMaxDistance;
	}

	/**
	 * @return a short name for this distance matrix implementation
	 */
	@Override
	public String getName() {
		return DistanceMatrixBlockedParallel.class.getSimpleName();
	}

	/**
	 * @return a human-readable description of this distance matrix implementation
	 */
	@Override
	public String getDescription() {
		return "Ultra-optimized parallel distance matrix for high-dimensional data (500+ dimensions)";
	}

	/**
	 * Returns the backing matrix. The returned array is mutable and shares state
	 * with this instance.
	 * <p>
	 * If external callers must not mutate the matrix, provide a defensive copy in a
	 * separate method.
	 * </p>
	 *
	 * @return the internal {@code double[n][n]} distance matrix
	 */
	@Override
	public double[][] getDistanceMatrix() {
		return matrix;
	}

	/**
	 * @return the elements corresponding to indices in this matrix
	 */
	@Override
	public List<? extends T> getElements() {
		return elements;
	}

	/**
	 * @return {@code true} if this matrix was computed assuming a symmetric
	 *         distance measure
	 */
	@Override
	public boolean isSymmetric() {
		return symmetric;
	}

	/**
	 * @return {@code true} if parallel computation was enabled for this instance
	 */
	public boolean isParallel() {
		return parallel;
	}

	/**
	 * Shuts down the shared executor used for parallel distance matrix computation.
	 * <p>
	 * This method is optional and should typically be called once during
	 * application shutdown. Calling it will prevent future instances from computing
	 * in parallel using the shared pool.
	 * </p>
	 */
	public static void shutdownSharedExecutor() {
		SHARED_EXECUTOR.shutdown();
	}
}