package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Title: BufferedImageTools
 * </p>
 *
 * <p>
 * Description: some helpers when applying standard operations on BufferedImages
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.02
 */
public class BufferedImageTools {

	public static double getLuminanceforPixel(BufferedImage bi, int x, int y) {
		if (bi == null)
			throw new IllegalArgumentException("BufferedImageTools.getLuminanceforPixel(...): BufferedImage was null.");

		if (x < 0 || x >= bi.getWidth() || y < 0 || y >= bi.getHeight())
			throw new IndexOutOfBoundsException("BufferedImageTools.getLuminanceforPixel(...): pixel coordinates ill-defined.");

		int color = bi.getRGB(x, y);
		int R = (color >>> 16) & 0xFF;
		int G = (color >>> 8) & 0xFF;
		int B = (color >>> 0) & 0xFF;
		double luminance = (0.2126 * R + 0.7152 * G + 0.0722 * B) / 255.0;
		return luminance;
	}

	/**
	 * Gathers the Color of a given pixel in a given BufferedImage.
	 * 
	 * @param bi
	 * @param x
	 * @param y
	 * @return
	 */
	public static Color getColor(BufferedImage bi, int x, int y) {
		int rgb = bi.getRGB(x, y);
		Color color = new Color(rgb, true);
		return color;
	}

	/**
	 * gathers the colors of all pixels from a given BufferedImage
	 * 
	 * @param bufferedImage
	 * @return
	 */
	public static List<Color> getColors(BufferedImage bufferedImage) {
		if (bufferedImage == null)
			return null;

		List<Color> colors = new ArrayList<>();
		for (int x = 0; x < bufferedImage.getWidth(); x++)
			for (int y = 0; y < bufferedImage.getHeight(); y++)
				colors.add(getColor(bufferedImage, x, y));

		return colors;
	}
}
