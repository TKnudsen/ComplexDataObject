package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;

/**
 * <p>
 * Utility class for estimating and reporting the in-memory footprint of a
 * ComplexDataObject. Provides a detailed per-attribute breakdown of
 * estimated sizes (strings, arrays, collections, maps, generic objects) as
 * well as general JVM free-memory reporting.
 * </p>
 */
public class MemoryTools {

	// Object header size (typically 12-16 bytes depending on JVM and compressed
	// OOPs)
	private static final int OBJECT_HEADER_SIZE = 16;
	private static final int OBJECT_REFERENCE_SIZE = 8; // 64-bit JVM with compressed OOPs

	private static final Map<Class<?>, Integer> PRIMITIVE_SIZES = new HashMap<Class<?>, Integer>();

	static {
		PRIMITIVE_SIZES.put(boolean.class, 1);
		PRIMITIVE_SIZES.put(byte.class, 1);
		PRIMITIVE_SIZES.put(char.class, 2);
		PRIMITIVE_SIZES.put(short.class, 2);
		PRIMITIVE_SIZES.put(int.class, 4);
		PRIMITIVE_SIZES.put(float.class, 4);
		PRIMITIVE_SIZES.put(long.class, 8);
		PRIMITIVE_SIZES.put(double.class, 8);
	}

	public static void freeMemory() {
		// Get current runtime
		Runtime runtime = Runtime.getRuntime();

		// Run garbage collector
		// runtime.gc();

		// Print initial memory usage
		// System.out.println("Initial free memory: " + runtime.freeMemory());

		long memoryUsed = runtime.totalMemory() - runtime.freeMemory();

		// Convert bytes to megabytes
		double memoryUsedMb = (double) memoryUsed / (1024 * 1024);

		// Print memory usage
		System.out.printf("Used Memory: %.2f MB%n", memoryUsedMb);
	}

	/** Tracks the previous {@link #logCheckpoint(String)} reading for delta reporting. */
	private static volatile Double lastCheckpointMb;

	/**
	 * Labeled memory checkpoint for tracking usage across a multi-phase loading
	 * sequence -- unlike {@link #freeMemory()} (an anonymous, un-timestamped
	 * snapshot), this attributes each reading to a named phase and reports how
	 * much memory that phase itself added, which is what actually helps narrow
	 * down a leak rather than just observing that total usage grew somewhere
	 * during the run.
	 *
	 * <p>
	 * This class is a low-level, dependency-free utility used across many
	 * projects, so it deliberately does not persist checkpoints to a file --
	 * doing so would require a project-specific file-location convention this
	 * class has no business knowing about. A caller that wants cross-run
	 * persistence should use the returned value to write its own log entry (see
	 * {@code PostgreSQLTools.selectContainer} in stocksapi for an example).
	 *
	 * <p>
	 * Not thread-safe against concurrent callers racing on {@code
	 * lastCheckpointMb} -- intended for a single logical loading sequence (a
	 * startup path, a batch job's main phases), not for checkpoints fired from
	 * multiple threads at once.
	 *
	 * @param label a short, human-readable name for this checkpoint (e.g. the
	 *              attribute category or loading phase just completed)
	 * @return the used-memory reading at this checkpoint, in MB
	 */
	public static double logCheckpoint(String label) {
		Checkpoint checkpoint = checkpoint();

		String delta = checkpoint.deltaMb == null ? "n/a" : String.format("%+.2f MB", checkpoint.deltaMb);
		System.out.printf("Used Memory [%s]: %.2f MB (delta: %s since last checkpoint)%n", label, checkpoint.usedMb,
				delta);

		return checkpoint.usedMb;
	}

	/**
	 * Silent counterpart to {@link #logCheckpoint(String)} -- reads and tracks
	 * the same {@link #lastCheckpointMb} state (advancing it exactly as {@link
	 * #logCheckpoint(String)} does) but returns the reading instead of printing
	 * it. For a caller that wants to fold the memory reading into a line of its
	 * own (e.g. one row of a table) rather than getting a separate printed line
	 * it doesn't control.
	 *
	 * @return the used-memory reading and its delta from the previous checkpoint
	 *         (whichever caller made it, {@link #logCheckpoint(String)} or this
	 *         method)
	 */
	public static Checkpoint checkpoint() {
		Runtime runtime = Runtime.getRuntime();
		long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
		double memoryUsedMb = (double) memoryUsed / (1024 * 1024);

		Double previous = lastCheckpointMb;
		Double deltaMb = previous == null ? null : memoryUsedMb - previous;
		lastCheckpointMb = memoryUsedMb;

		return new Checkpoint(memoryUsedMb, deltaMb);
	}

	/**
	 * A single memory reading paired with its delta from the previous checkpoint
	 * (whichever caller made it). {@link #deltaMb} is {@code null} for the first
	 * checkpoint of a run, since there is nothing to compare against yet.
	 */
	public static final class Checkpoint {

		public final double usedMb;
		public final Double deltaMb;

		private Checkpoint(double usedMb, Double deltaMb) {
			this.usedMb = usedMb;
			this.deltaMb = deltaMb;
		}
	}

	/**
	 * Analyzes and prints memory consumption of a ComplexDataObject. Shows detailed
	 * breakdown per attribute including type and estimated size.
	 *
	 * @param cdo the ComplexDataObject to analyze
	 */
	public static void memoryOf(ComplexDataObject cdo) {
		if (cdo == null) {
			System.out.println("ComplexDataObject is null");
			return;
		}

		System.out.println(repeatChar('=', 80));
		System.out.println("Memory Analysis for ComplexDataObject: " + cdo.getName());
		System.out.println("ID: " + cdo.getID());
		System.out.println(repeatChar('=', 80));

		// Calculate base object overhead
		long baseOverhead = calculateBaseObjectOverhead(cdo);
		System.out.println("\n--- BASE OBJECT OVERHEAD ---");
		System.out.printf("ComplexDataObject base:        %,10d bytes\n", baseOverhead);

		// Analyze each attribute
		System.out.println("\n--- ATTRIBUTES MEMORY BREAKDOWN ---");
		System.out.printf("%-40s %-25s %15s\n", "Attribute", "Type", "Est. Size (bytes)");
		System.out.println(repeatChar('-', 80));

		long totalAttributesSize = 0;
		Map<String, Long> attributeSizes = new LinkedHashMap<String, Long>();

		Set<String> attributes = cdo.keySet();
		// Sort attributes for consistent display
		List<String> sortedAttributes = new ArrayList<String>(attributes);
		Collections.sort(sortedAttributes);

		for (String attribute : sortedAttributes) {
			Object value = cdo.getAttribute(attribute);
			long size = estimateSize(value);
			attributeSizes.put(attribute, size);
			totalAttributesSize += size;

			String typeName = (value == null) ? "null" : value.getClass().getSimpleName();
			System.out.printf("%-40s %-25s %,15d\n", truncate(attribute, 40), truncate(typeName, 25), size);
		}

		// Summary
		System.out.println(repeatChar('-', 80));
		System.out.printf("%-40s %-25s %,15d\n", "TOTAL ATTRIBUTES DATA", "", totalAttributesSize);

		// Map overhead (HashMap internal structure)
		long mapOverhead = estimateMapOverhead(attributes.size());
		System.out.printf("%-40s %-25s %,15d\n", "Map overhead (HashMap)", "", mapOverhead);

		// Listeners overhead
		long listenersOverhead = estimateListenersOverhead(cdo);
		System.out.printf("%-40s %-25s %,15d\n", "Listeners overhead", "", listenersOverhead);

		long totalSize = baseOverhead + totalAttributesSize + mapOverhead + listenersOverhead;
		System.out.println(repeatChar('=', 80));
		System.out.printf("%-40s %-25s %,15d\n", "TOTAL ESTIMATED SIZE", "", totalSize);
		System.out.println(repeatChar('=', 80));

		// Additional statistics
		printStatistics(cdo, attributeSizes, totalSize);
	}

	/**
	 * Calculates base object overhead (object headers, fields).
	 */
	private static long calculateBaseObjectOverhead(ComplexDataObject cdo) {
		// ComplexDataObject header
		long size = OBJECT_HEADER_SIZE;

		// Inherited from KeyValueObject: attributes map reference
		size += OBJECT_REFERENCE_SIZE;

		// Inherited from KeyValueObject: ID field (String reference)
		size += OBJECT_REFERENCE_SIZE;

		// ComplexDataObject specific: NAME, DESCRIPTION (static, don't count)
		// listeners reference
		size += OBJECT_REFERENCE_SIZE;

		return size;
	}

	/**
	 * Estimates the memory size of an object.
	 */
	private static long estimateSize(Object obj) {
		if (obj == null) {
			return 0;
		}

		Class<?> clazz = obj.getClass();

		// Primitives (boxed)
		if (obj instanceof Boolean)
			return OBJECT_HEADER_SIZE + 1;
		if (obj instanceof Byte)
			return OBJECT_HEADER_SIZE + 1;
		if (obj instanceof Character)
			return OBJECT_HEADER_SIZE + 2;
		if (obj instanceof Short)
			return OBJECT_HEADER_SIZE + 2;
		if (obj instanceof Integer)
			return OBJECT_HEADER_SIZE + 4;
		if (obj instanceof Float)
			return OBJECT_HEADER_SIZE + 4;
		if (obj instanceof Long)
			return OBJECT_HEADER_SIZE + 8;
		if (obj instanceof Double)
			return OBJECT_HEADER_SIZE + 8;

		// String
		if (obj instanceof String) {
			return estimateStringSize((String) obj);
		}

		// Arrays
		if (clazz.isArray()) {
			return estimateArraySize(obj);
		}

		// Collections
		if (obj instanceof Collection) {
			return estimateCollectionSize((Collection<?>) obj);
		}

		if (obj instanceof Map) {
			return estimateMapSize((Map<?, ?>) obj);
		}

		// Complex objects - rough estimate
		return estimateObjectSize(obj);
	}

	/**
	 * Estimates String memory size. String has: header + char[] reference + hash
	 * field + char array
	 */
	private static long estimateStringSize(String str) {
		if (str == null || str.isEmpty()) {
			return OBJECT_HEADER_SIZE + 8; // Empty string object
		}

		// String object: header + value reference + hash int + other fields
		long stringObjectSize = OBJECT_HEADER_SIZE + OBJECT_REFERENCE_SIZE + 4;

		// char[] array: header + length field + char data
		long charArraySize = OBJECT_HEADER_SIZE + 4 + (str.length() * 2);

		return stringObjectSize + charArraySize;
	}

	/**
	 * Estimates array size.
	 */
	private static long estimateArraySize(Object array) {
		int length = Array.getLength(array);
		Class<?> componentType = array.getClass().getComponentType();

		// Array header + length field
		long size = OBJECT_HEADER_SIZE + 4;

		if (componentType.isPrimitive()) {
			int elementSize = getPrimitiveSize(componentType);
			size += (long) length * elementSize;
		} else {
			// Reference array
			size += (long) length * OBJECT_REFERENCE_SIZE;

			// Estimate referenced objects (shallow)
			for (int i = 0; i < length; i++) {
				Object element = Array.get(array, i);
				if (element != null) {
					size += estimateSize(element);
				}
			}
		}

		return size;
	}

	/**
	 * Estimates collection size.
	 */
	private static long estimateCollectionSize(Collection<?> collection) {
		// Collection object header
		long size = OBJECT_HEADER_SIZE;

		// Internal array/node structure (varies by implementation)
		if (collection instanceof ArrayList) {
			// ArrayList: header + elementData reference + size field
			size += OBJECT_REFERENCE_SIZE + 4;
			// Internal array
			int capacity = Math.max(10, collection.size()); // Default capacity is 10
			size += OBJECT_HEADER_SIZE + 4 + (capacity * OBJECT_REFERENCE_SIZE);
		} else if (collection instanceof LinkedList) {
			// Each node: header + item reference + next + prev
			size += collection.size() * (OBJECT_HEADER_SIZE + 3 * OBJECT_REFERENCE_SIZE);
		} else {
			// Generic estimate
			size += OBJECT_REFERENCE_SIZE + (collection.size() * OBJECT_REFERENCE_SIZE);
		}

		// Count elements (shallow)
		for (Object item : collection) {
			size += estimateSize(item);
		}

		return size;
	}

	/**
	 * Estimates Map size.
	 */
	private static long estimateMapSize(Map<?, ?> map) {
		// Map object header
		long size = OBJECT_HEADER_SIZE;

		// Internal structure
		size += estimateMapOverhead(map.size());

		// Entries
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			// Entry object
			size += OBJECT_HEADER_SIZE + 3 * OBJECT_REFERENCE_SIZE; // header + key + value + next

			// Key and value
			size += estimateSize(entry.getKey());
			size += estimateSize(entry.getValue());
		}

		return size;
	}

	/**
	 * Estimates HashMap internal overhead.
	 */
	private static long estimateMapOverhead(int size) {
		// HashMap: header + table reference + size + threshold + loadFactor + modCount
		long overhead = OBJECT_HEADER_SIZE + OBJECT_REFERENCE_SIZE + 4 + 4 + 4 + 4;

		// Internal table (array of Entry references)
		int capacity = nextPowerOfTwo((int) Math.ceil(size / 0.75));
		overhead += OBJECT_HEADER_SIZE + 4 + (capacity * OBJECT_REFERENCE_SIZE);

		return overhead;
	}

	/**
	 * Estimates listeners overhead for ComplexDataObject.
	 */
	private static long estimateListenersOverhead(ComplexDataObject cdo) {
		List<?> listeners = cdo.getListeners();
		if (listeners == null || listeners.isEmpty()) {
			// Empty CopyOnWriteArrayList: header + array reference
			return OBJECT_HEADER_SIZE + OBJECT_REFERENCE_SIZE + OBJECT_HEADER_SIZE + 4;
		}

		// CopyOnWriteArrayList overhead + array
		return estimateCollectionSize(listeners);
	}

	/**
	 * Generic object size estimation.
	 */
	private static long estimateObjectSize(Object obj) {
		long size = OBJECT_HEADER_SIZE;

		// Count fields (rough estimate)
		Class<?> clazz = obj.getClass();
		while (clazz != null && clazz != Object.class) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
					continue; // Skip static fields
				}

				Class<?> fieldType = field.getType();
				if (fieldType.isPrimitive()) {
					size += getPrimitiveSize(fieldType);
				} else {
					size += OBJECT_REFERENCE_SIZE;
				}
			}
			clazz = clazz.getSuperclass();
		}

		return size;
	}

	/**
	 * Gets size of primitive type.
	 */
	private static int getPrimitiveSize(Class<?> primitiveType) {
		Integer size = PRIMITIVE_SIZES.get(primitiveType);
		return size != null ? size : 0;
	}

	/**
	 * Finds next power of two (for HashMap capacity).
	 */
	private static int nextPowerOfTwo(int n) {
		n--;
		n |= n >> 1;
		n |= n >> 2;
		n |= n >> 4;
		n |= n >> 8;
		n |= n >> 16;
		return n + 1;
	}

	/**
	 * Truncates string for display.
	 */
	private static String truncate(String str, int maxLength) {
		if (str == null)
			return "null";
		if (str.length() <= maxLength)
			return str;
		return str.substring(0, maxLength - 3) + "...";
	}

	/**
	 * Repeats a character n times (Java 8 compatible).
	 */
	private static String repeatChar(char c, int count) {
		StringBuilder sb = new StringBuilder(count);
		for (int i = 0; i < count; i++) {
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Prints additional statistics.
	 */
	private static void printStatistics(ComplexDataObject cdo, Map<String, Long> attributeSizes, long totalSize) {
		System.out.println("\n--- STATISTICS ---");
		System.out.printf("Total attributes:              %,10d\n", cdo.keySet().size());

		if (!attributeSizes.isEmpty()) {
			// Find largest attribute (Java 8 compatible)
			Map.Entry<String, Long> largest = null;
			for (Map.Entry<String, Long> entry : attributeSizes.entrySet()) {
				if (largest == null || entry.getValue() > largest.getValue()) {
					largest = entry;
				}
			}

			if (largest != null) {
				System.out.printf("Largest attribute:             %-30s (%,d bytes)\n", truncate(largest.getKey(), 30),
						largest.getValue());
			}

			// Average size (Java 8 compatible)
			long sum = 0;
			for (Long value : attributeSizes.values()) {
				sum += value;
			}
			double avgSize = attributeSizes.isEmpty() ? 0.0 : (double) sum / attributeSizes.size();
			System.out.printf("Average attribute size:        %,10.2f bytes\n", avgSize);
		}

		System.out.printf("Estimated total memory:        %,10d bytes (%.2f KB, %.2f MB)\n", totalSize,
				totalSize / 1024.0, totalSize / (1024.0 * 1024.0));
	}

	/**
	 * Example usage and testing.
	 */
	public static void main(String[] args) {
		// Create test object
		ComplexDataObject cdo = new ComplexDataObject("TestObject", "A test object for memory analysis");

		// Add various types of attributes
		cdo.add("StringAttribute", "This is a test string with some content");
		cdo.add("IntegerAttribute", 42);
		cdo.add("DoubleAttribute", 3.14159);
		cdo.add("BooleanAttribute", true);
		cdo.add("LongAttribute", 9876543210L);
		cdo.add("NullAttribute", null);

		List<String> stringList = new ArrayList<String>();
		stringList.add("Item1");
		stringList.add("Item2");
		stringList.add("Item3");
		cdo.add("ListAttribute", stringList);

		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("Key1", 100);
		map.put("Key2", 200);
		cdo.add("MapAttribute", map);

		double[] doubleArray = { 1.1, 2.2, 3.3, 4.4, 5.5 };
		cdo.add("DoubleArrayAttribute", doubleArray);

		// Analyze memory
		memoryOf(cdo);
	}
}
