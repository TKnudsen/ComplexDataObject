package com.github.TKnudsen.ComplexDataObject.data.color;

/**
 * Hue/saturation/brightness histograms of an image, aggregated into n bins
 * each.
 *
 * @version 1.0
 * @since 2024
 */
public class ColorHistogram {

	private double[] hues;
	private double[] saturations;
	private double[] brightnesses;

	public ColorHistogram(double[] hues, double[] saturations, double[] brightnesses) {
		super();
		this.hues = hues;
		this.brightnesses = brightnesses;
		this.saturations = saturations;
	}

	/**
	 * hues aggregated to n bins.
	 *
	 * @return
	 */
	public double[] getHues() {
		return hues;
	}

	/**
	 * brightness value distributions aggregated to n bins.
	 *
	 * @return
	 */
	public double[] getBrightnesses() {
		return brightnesses;
	}

	/**
	 * saturation value distributions aggregated to n bins
	 *
	 * @return
	 */
	public double[] getSaturations() {
		return saturations;
	}
}
