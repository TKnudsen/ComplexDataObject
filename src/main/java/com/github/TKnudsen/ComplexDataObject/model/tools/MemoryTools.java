package com.github.TKnudsen.ComplexDataObject.model.tools;

public class MemoryTools {

	public static void freeMemory() {
		// Get current runtime
		Runtime runtime = Runtime.getRuntime();

		// Run garbage collector
		//runtime.gc();

		// Print initial memory usage
		// System.out.println("Initial free memory: " + runtime.freeMemory());

		long memoryUsed = runtime.totalMemory() - runtime.freeMemory();

		// Convert bytes to megabytes
		double memoryUsedMb = (double) memoryUsed / (1024 * 1024);

		// Print memory usage
		System.out.printf("Used Memory: %.2f MB%n", memoryUsedMb);
	}
}
