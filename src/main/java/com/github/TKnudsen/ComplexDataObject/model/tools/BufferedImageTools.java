package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.awt.image.BufferedImage;

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
 * @version 1.00
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
}
