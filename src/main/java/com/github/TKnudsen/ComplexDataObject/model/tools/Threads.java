package com.github.TKnudsen.ComplexDataObject.model.tools;

public class Threads {

	public static void sleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Waits some milliseconds (delay). In addition, random creates some delta
	 * milliseconds.
	 * 
	 * @param delay  in seconds
	 * @param random in seconds
	 */
	public static void sleep(double delay, double random) {
		long dl = (long) (delay + Math.random() * random);
		sleep(dl);
	}

	/**
	 * Waits some seconds (delay). In addition, random creates some delta seconds.
	 * 
	 * @param delay  in seconds
	 * @param randomInSeconds in seconds
	 */
	public static void sleepSeconds(double delay, double randomInSeconds) {
		sleep(delay * 1000, randomInSeconds * 1000);
	}

}
