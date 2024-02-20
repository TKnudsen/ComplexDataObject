package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;

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
 * Copyright: Copyright (c) 2016-2023
 * </p>
 *
 * @author Juergen Bernard
 * @version 1.05
 */
public class BufferedImageTools {

	public static double getLuminanceforPixel(BufferedImage bi, int x, int y) {
		if (bi == null)
			throw new IllegalArgumentException("BufferedImageTools.getLuminanceforPixel(...): BufferedImage was null.");

		if (x < 0 || x >= bi.getWidth() || y < 0 || y >= bi.getHeight())
			throw new IndexOutOfBoundsException(
					"BufferedImageTools.getLuminanceforPixel(...): pixel coordinates ill-defined.");

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

	/**
	 * 
	 * @param im
	 * @param color
	 * @return
	 */
	public static Image setTransparentColor(BufferedImage im, final Color color) {
		ImageFilter filter = new RGBImageFilter() {

			// the color we are looking for... Alpha bits are set to opaque
			public int markerRGB = color.getRGB() | 0xFF000000;

			public final int filterRGB(int x, int y, int rgb) {
				if ((rgb | 0xFF000000) == markerRGB) {
					// Mark the alpha bits as zero - transparent
					return 0x00FFFFFF & rgb;
				} else {
					// nothing to do
					return rgb;
				}
			}
		};

		ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}

	public static BufferedImage toBufferedImage(Image image) {
		BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bufferedImage.createGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();

		return bufferedImage;
	}

	/**
	 * resizes a bufferedImage to the given size properties.
	 * 
	 * @param source
	 * @param targetWidth
	 * @param targetHeight
	 * @return
	 */
	public static BufferedImage resize(BufferedImage source, int targetWidth, int targetHeight) {
		Image tmp = source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
		BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = output.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return output;
	}

	/**
	 * 
	 * @param src
	 * @param w
	 * @param h
	 * @return
	 */
	public static BufferedImage resizeFast(BufferedImage src, int w, int h) {
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		int x, y;
		int ww = src.getWidth();
		int hh = src.getHeight();
		for (x = 0; x < w; x++) {
			for (y = 0; y < h; y++) {
				int col = src.getRGB(x * ww / w, y * hh / h);
				img.setRGB(x, y, col);
			}
		}
		return img;
	}

	/**
	 * rescales a bufferedImage with the given properties.
	 * 
	 * @param source
	 * @param factorX
	 * @param factorY
	 * @return
	 */
	public static BufferedImage rescale(BufferedImage source, double factorX, double factorY) {
		int w = source.getWidth();
		int h = source.getHeight();

		BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(factorX, factorY);
		AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		output = scaleOp.filter(source, output);

		return output;
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
	public static BufferedImage loadBufferedImage(File file) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(file);
		} catch (IOException e) {
			// e.printStackTrace();
			Threads.sleep(50);
			return loadBufferedImage(file, 2);
		} catch (ConcurrentModificationException e) {
			// e.printStackTrace();
			Threads.sleep(50);
			return loadBufferedImage(file, 2);
		}
		return img;
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
	private static BufferedImage loadBufferedImage(File file, int rec) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(file);
		} catch (IOException e) {
			System.err.println("BufferedImageTools.loadBufferedImage: attempt no. " + rec);
			if (rec > 10)
				e.printStackTrace();
			Threads.sleep(50);
			return loadBufferedImage(file, rec++);
		}
		return img;
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
	public static BufferedImage loadImage(String file) {
		File imageFile = new File(file);
		BufferedImage bufferedImage = null;

		if (imageFile.exists()) {
			try {
				bufferedImage = loadBufferedImage(imageFile);
			} catch (IndexOutOfBoundsException iobe) {
				System.out.println("BufferedImageTools.loadImage: exception for file " + imageFile);
				iobe.printStackTrace();
			}
		}

		return bufferedImage;
	}

	public static BufferedImage subImage(BufferedImage bufferedImage, Rectangle rectangle) {
		if (bufferedImage == null || rectangle == null)
			return null;

		BufferedImage bi = bufferedImage.getSubimage((int) rectangle.getMinX(), (int) rectangle.getMinY(),
				(int) rectangle.getWidth(), (int) rectangle.getHeight());
		return bi;
	}

	/**
	 * Sets the color of a pixel. Sensitive to alpha.
	 * 
	 * @param bufferedImage
	 * @param x
	 * @param y
	 * @param color
	 */
	public static void setColor(BufferedImage bufferedImage, int x, int y, Color color) {
		Objects.requireNonNull(bufferedImage);
		Objects.requireNonNull(color);

		if (x < 0 || y < 0)
			throw new IllegalArgumentException("x or y out of range!");
		if (x > bufferedImage.getWidth() || y > bufferedImage.getHeight())
			throw new IllegalArgumentException("x or y out of range!");

		int col = 0;
		col = (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();

		bufferedImage.setRGB(x, y, col);
	}

	/**
	 * creates a new BufferedImage from a given matrix of BufferedImages. Uses width
	 * and height from the BufferedImage at [0][0] for the definition of the target
	 * size.
	 * 
	 * @param bufferedImages
	 * @return
	 */
	public static BufferedImage gridToSingle(BufferedImage[][] bufferedImages) {
		Objects.requireNonNull(bufferedImages);

		if (bufferedImages.length == 0 || bufferedImages[0].length == 0)
			throw new IllegalArgumentException("grid size zero");

		int widthTiles = bufferedImages[0][0].getWidth();
		int heightTiles = bufferedImages[0][0].getHeight();

		int xCount = bufferedImages.length;
		int yCount = bufferedImages[0].length;

		BufferedImage bufferedImage = new BufferedImage(widthTiles * xCount, heightTiles * yCount,
				BufferedImage.TYPE_INT_ARGB);

		for (int x = 0; x < xCount; x++)
			for (int y = 0; y < yCount; y++) {
				addImage(bufferedImage, bufferedImages[x][y], 1.0f, x * widthTiles, y * heightTiles);
			}

		return bufferedImage;
	}

	private static void addImage(BufferedImage buff1, BufferedImage buff2, float opaque, int x, int y) {
		Graphics2D g2d = buff1.createGraphics();

		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opaque));
		g2d.drawImage(buff2, x, y, null);
		g2d.dispose();
	}

	public static int hashCodeFromPixelColors(BufferedImage img) {
		Objects.requireNonNull(img);

		int hash = 29;

		for (int x = 0; x < img.getWidth(); x++)
			for (int y = 0; y < img.getHeight(); y++)
				hash = 31 * hash + img.getRGB(x, y);

		return hash;
	}

	/**
	 * see
	 * https://stackoverflow.com/questions/18800717/convert-text-content-to-image
	 * 
	 * @param string
	 * @return
	 */
	public static BufferedImage stringToBufferedimage(String string) {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		Font font = new Font("Arial", Font.PLAIN, 48);
		g2d.setFont(font);
		FontMetrics fm = g2d.getFontMetrics();
		int width = fm.stringWidth(string);
		int height = fm.getHeight();
		g2d.dispose();

		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2d.setFont(font);
		fm = g2d.getFontMetrics();
		g2d.setColor(Color.WHITE);
		g2d.drawString(string, 0, fm.getAscent());
		g2d.dispose();

		return img;
	}
}
