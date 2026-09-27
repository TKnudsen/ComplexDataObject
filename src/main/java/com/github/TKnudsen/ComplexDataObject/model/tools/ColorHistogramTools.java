package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.github.TKnudsen.ComplexDataObject.data.color.ColorHistogram;

/**
 * Computes hue/saturation/brightness {@link ColorHistogram}s of a
 * {@link BufferedImage}.
 *
 * @version 1.0
 * @since 2024
 */
public class ColorHistogramTools {

	public static ColorHistogram getColorHistogram(BufferedImage bufferedImage) {
		return getColorHistogram(bufferedImage, 0.0, 0.0);
	}

	/**
	 *
	 * @param bufferedImage
	 * @param lowSaturationRecutionRate   1.0 means that 0% saturated colors are
	 *                                    entirely ignored (100% saturated is
	 *                                    optimal)
	 * @param weakBrightnessReductionRate 1.0 means that 0% and 100% bright colors
	 *                                    are entirely ignored (50% brightness is
	 *                                    optimal)
	 * @return
	 */
	public static ColorHistogram getColorHistogram(BufferedImage bufferedImage, double lowSaturationRecutionRate,
			double weakBrightnessReductionRate) {

		double size = bufferedImage.getWidth() * bufferedImage.getHeight();
		int sampling = (int) Math.max(1, size / 2000000);

		double[] h = new double[256];
		double[] s = new double[256];
		double[] b = new double[256];

		for (int x = 0; x < bufferedImage.getWidth(); x += sampling)
			for (int y = 0; y < bufferedImage.getHeight(); y += sampling) {
				Color c = getColor(bufferedImage.getRGB(x, y));
				float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
				int hue = (int) (hsv[0] * 255);
				int saturation = (int) (hsv[1] * 255);
				int brightness = (int) (hsv[2] * 255);

				// consider reduction
				double reduction = 1.0;
				double sat = saturation / 255.0;
				reduction *= (1 - (lowSaturationRecutionRate * (1 - sat)));

				double bright = brightness / 255.0;
				bright -= 0.5;
				bright = Math.abs(bright) * 2;
				bright = 1 - bright; // high is good now
				reduction *= (1 - (weakBrightnessReductionRate * (1 - bright)));

				h[hue] += reduction;
				s[saturation] += reduction;
				b[brightness] += reduction;
			}

		return new ColorHistogram(h, s, b);
	}

	public static Color getColor(int TYPE_INT_RGB) {
		int alpha = (TYPE_INT_RGB >> 24) & 0xFF;
		int red = (TYPE_INT_RGB >> 16) & 0xFF;
		int green = (TYPE_INT_RGB >> 8) & 0xFF;
		int blue = (TYPE_INT_RGB) & 0xFF;
		return new Color(red, green, blue, alpha);
	}
}
