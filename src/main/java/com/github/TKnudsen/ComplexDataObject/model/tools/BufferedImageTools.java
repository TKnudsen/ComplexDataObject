package com.github.TKnudsen.ComplexDataObject.model.tools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.awt.image.RasterFormatException;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

/**
 * Static helpers for common {@link BufferedImage} operations.
 *
 * <p>
 * Covers pixel access, transparency, geometry (resize, rescale, rotate, flip),
 * load and save (with format inference and JPEG alpha handling), copy, crop,
 * composition, color conversion, text rendering, and image identity checks.
 * </p>
 *
 * @version 2.0 revised and extended in March 2026
 * @since 2016
 */
public final class BufferedImageTools {

	private static final Logger LOG = Logger.getLogger(BufferedImageTools.class.getName());

	private static final int MAX_IMAGE_LOAD_ATTEMPTS = 10;
	private static final long RETRY_DELAY_MS = 50L;

	/** ImageIO canonical format name for PNG. Used as the fallback format. */
	private static final String FORMAT_PNG = "png";

	/**
	 * ImageIO canonical format name for JPEG. Both {@code "jpeg"} and {@code "jpg"}
	 * are accepted by ImageIO, but {@code "jpeg"} is used internally as the
	 * normalized form.
	 */
	private static final String FORMAT_JPEG = "jpeg";

	private BufferedImageTools() {
	}

	// ==================== PIXEL HELPERS ====================

	/**
	 * Returns the luminance of the pixel at (x, y) in [0.0, 1.0].
	 *
	 * @throws NullPointerException      if {@code bi} is null
	 * @throws IndexOutOfBoundsException if the coordinates are outside the image
	 */
	public static double getLuminanceForPixel(BufferedImage bi, int x, int y) {
		Objects.requireNonNull(bi, "BufferedImage must not be null");

		if (x < 0 || x >= bi.getWidth() || y < 0 || y >= bi.getHeight())
			throw new IndexOutOfBoundsException(
					"BufferedImageTools.getLuminanceForPixel: coordinates (" + x + ", " + y + ") out of bounds.");

		int color = bi.getRGB(x, y);
		int r = (color >>> 16) & 0xFF;
		int g = (color >>> 8) & 0xFF;
		int b = color & 0xFF;

		return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0;
	}

	/**
	 * Returns the {@link Color} of the pixel at (x, y), including alpha.
	 *
	 * @throws NullPointerException      if {@code bi} is null
	 * @throws IndexOutOfBoundsException if the coordinates are outside the image
	 */
	public static Color getColor(BufferedImage bi, int x, int y) {
		Objects.requireNonNull(bi, "BufferedImage must not be null");

		if (x < 0 || x >= bi.getWidth() || y < 0 || y >= bi.getHeight())
			throw new IndexOutOfBoundsException(
					"BufferedImageTools.getColor: coordinates (" + x + ", " + y + ") out of bounds.");

		return new Color(bi.getRGB(x, y), true);
	}

	/**
	 * Returns all pixel colors of the given image in column-major order.
	 *
	 * @throws NullPointerException if {@code bufferedImage} is null
	 */
	public static List<Color> getColors(BufferedImage bufferedImage) {
		Objects.requireNonNull(bufferedImage, "bufferedImage must not be null");

		List<Color> colors = new ArrayList<>(bufferedImage.getWidth() * bufferedImage.getHeight());
		for (int x = 0; x < bufferedImage.getWidth(); x++)
			for (int y = 0; y < bufferedImage.getHeight(); y++)
				colors.add(getColor(bufferedImage, x, y));

		return colors;
	}

	public static List<Color> getDistinctColors(BufferedImage image) {
		Objects.requireNonNull(image, "image must not be null");

		Set<Color> colors = new LinkedHashSet<>();
		for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++)
				colors.add(getColor(image, x, y));

		return new ArrayList<>(colors);
	}

	/**
	 * Sets the color of the pixel at (x, y), respecting alpha.
	 *
	 * @throws NullPointerException      if {@code bufferedImage} or {@code color}
	 *                                   is null
	 * @throws IndexOutOfBoundsException if the coordinates are outside the image
	 */
	public static void setColor(BufferedImage bufferedImage, int x, int y, Color color) {
		Objects.requireNonNull(bufferedImage, "bufferedImage must not be null");
		Objects.requireNonNull(color, "color must not be null");

		if (x < 0 || x >= bufferedImage.getWidth() || y < 0 || y >= bufferedImage.getHeight())
			throw new IndexOutOfBoundsException("BufferedImageTools.setColor: coordinates (" + x + ", " + y
					+ ") out of bounds for image " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight());

		int col = (color.getAlpha() << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
		bufferedImage.setRGB(x, y, col);
	}

	public static void replaceColor(BufferedImage image, Color remove, Color replace) {
		Objects.requireNonNull(image, "image must not be null");
		Objects.requireNonNull(remove, "remove must not be null");
		Objects.requireNonNull(replace, "replace must not be null");

		for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++)
				if (getColor(image, x, y).equals(remove))
					setColor(image, x, y, replace);
	}

	// ==================== TRANSPARENCY ====================

	/**
	 * Returns an image where all pixels matching {@code color} are made
	 * transparent.
	 *
	 * @throws NullPointerException if {@code im} or {@code color} is null
	 */
	public static Image setTransparentColor(BufferedImage im, final Color color) {
		Objects.requireNonNull(im, "im must not be null");
		Objects.requireNonNull(color, "color must not be null");

		final int markerRGB = color.getRGB() | 0xFF000000;

		ImageFilter filter = new RGBImageFilter() {
			@Override
			public int filterRGB(int x, int y, int rgb) {
				return (rgb | 0xFF000000) == markerRGB ? (0x00FFFFFF & rgb) : rgb;
			}
		};

		ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}

	/**
	 * Converts an {@link Image} to an ARGB {@link BufferedImage}.
	 *
	 * @throws NullPointerException     if {@code image} is null
	 * @throws IllegalArgumentException if image dimensions are not yet available
	 */
	public static BufferedImage toBufferedImage(Image image) {
		Objects.requireNonNull(image, "image must not be null");

		int width = image.getWidth(null);
		int height = image.getHeight(null);
		if (width < 0 || height < 0)
			throw new IllegalArgumentException("BufferedImageTools.toBufferedImage: image dimensions not available"
					+ " (image may not be fully loaded).");

		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bufferedImage.createGraphics();
		try {
			g2.drawImage(image, 0, 0, null);
		} finally {
			g2.dispose();
		}
		return bufferedImage;
	}

	// ============== RESIZE / RESCALE / ROTATE / FLIP / BLEND ===============

	/**
	 * Resizes the image to the given dimensions using smooth scaling.
	 *
	 * @throws NullPointerException     if {@code source} is null
	 * @throws IllegalArgumentException if target dimensions are not positive
	 */
	public static BufferedImage resize(BufferedImage source, int targetWidth, int targetHeight) {
		return resize(source, targetWidth, targetHeight, Image.SCALE_SMOOTH);
	}

	/**
	 * Resizes the image to the given dimensions using smooth scaling.
	 *
	 * @param source
	 * @param targetHeight
	 * @param hints        flags to indicate the type of algorithm to use for image
	 *                     re-sampling. Explicit options: Image.SCALE_SMOOTH |
	 *                     Image.SCALE_AREA_AVERAGING
	 * @throws NullPointerException     if {@code source} is null
	 * @throws IllegalArgumentException if target dimensions are not positive
	 */
	public static BufferedImage resize(BufferedImage source, int targetWidth, int targetHeight, int hints) {
		Objects.requireNonNull(source, "source must not be null");
		if (targetWidth <= 0 || targetHeight <= 0)
			throw new IllegalArgumentException("BufferedImageTools.resize: target dimensions must be positive, got "
					+ targetWidth + "x" + targetHeight);

		Image tmp = source.getScaledInstance(targetWidth, targetHeight, hints);
		BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = output.createGraphics();
		try {
			g2d.drawImage(tmp, 0, 0, null);
		} finally {
			g2d.dispose();
		}
		return output;
	}

	/**
	 * Resizes the image using nearest-neighbour sampling. Faster than
	 * {@link #resize} but lower quality. Alpha is preserved.
	 *
	 * @throws NullPointerException     if {@code src} is null
	 * @throws IllegalArgumentException if target dimensions are not positive
	 */
	public static BufferedImage resizeFast(BufferedImage src, int w, int h) {
		Objects.requireNonNull(src, "src must not be null");
		if (w <= 0 || h <= 0)
			throw new IllegalArgumentException(
					"BufferedImageTools.resizeFast: target dimensions must be positive, got " + w + "x" + h);

		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int ww = src.getWidth();
		int hh = src.getHeight();

		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				img.setRGB(x, y, src.getRGB(x * ww / w, y * hh / h));

		return img;
	}

	/**
	 * Re-scales the image by the given factors using bilinear interpolation. Output
	 * dimensions are derived from the source dimensions and the scale factors.
	 *
	 * @throws NullPointerException     if {@code source} is null
	 * @throws IllegalArgumentException if scale factors are not finite and positive
	 */
	public static BufferedImage rescale(BufferedImage source, double factorX, double factorY) {
		Objects.requireNonNull(source, "source must not be null");
		if (!Double.isFinite(factorX) || !Double.isFinite(factorY) || factorX <= 0.0 || factorY <= 0.0)
			throw new IllegalArgumentException(
					"BufferedImageTools.rescale: scale factors must be finite and positive, got " + factorX + ", "
							+ factorY);

		AffineTransform at = AffineTransform.getScaleInstance(factorX, factorY);
		AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		return scaleOp.filter(source, null);
	}

	/**
	 * Rotates the image clockwise by the given angle in degrees around its center.
	 * The output canvas is sized to contain the full rotated image.
	 *
	 * @throws NullPointerException if {@code source} is null
	 */
	public static BufferedImage rotate(BufferedImage source, double degrees) {
		Objects.requireNonNull(source, "source must not be null");

		double radians = Math.toRadians(degrees);
		double sin = Math.abs(Math.sin(radians));
		double cos = Math.abs(Math.cos(radians));

		int w = source.getWidth();
		int h = source.getHeight();
		int newW = Math.max(1, (int) Math.round(w * cos + h * sin));
		int newH = Math.max(1, (int) Math.round(w * sin + h * cos));

		BufferedImage result = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = result.createGraphics();
		try {
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.translate((newW - w) / 2.0, (newH - h) / 2.0);
			g2d.rotate(radians, w / 2.0, h / 2.0);
			g2d.drawImage(source, 0, 0, null);
		} finally {
			g2d.dispose();
		}
		return result;
	}

	/**
	 * Flips the image horizontally (left-right mirror).
	 *
	 * @throws NullPointerException if {@code source} is null
	 */
	public static BufferedImage flipHorizontal(BufferedImage source) {
		Objects.requireNonNull(source, "source must not be null");

		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-source.getWidth(), 0);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		return op.filter(source, null);
	}

	/**
	 * Flips the image vertically (top-bottom mirror).
	 *
	 * @throws NullPointerException if {@code source} is null
	 */
	public static BufferedImage flipVertical(BufferedImage source) {
		Objects.requireNonNull(source, "source must not be null");

		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -source.getHeight());
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		return op.filter(source, null);
	}

	/**
	 * Blends/merges two images with equal weight.
	 *
	 * @param image1 first image; must not be null
	 * @param image2 second image; must not be null
	 * @return blended image
	 */
	public static BufferedImage blend(BufferedImage image1, BufferedImage image2) {
		return blend(image1, image2, 0.5);
	}

	/**
	 * Blends/merges two images pixel-wise into a new ARGB image.
	 *
	 * <p>
	 * The output size is the maximum width and maximum height of both inputs. Where
	 * both images have a pixel, each RGBA channel is combined by weighted
	 * averaging. Where only one image has a pixel, that pixel is copied unchanged.
	 * </p>
	 *
	 * @param image1         first image; must not be null
	 * @param image2         second image; must not be null
	 * @param weightOfImage2 blend weight in [0, 1], 0.5 means equal weight
	 * @return blended image
	 * @throws NullPointerException     if an input image is null
	 * @throws IllegalArgumentException if {@code weightOfImage2} is not in [0, 1]
	 */
	public static BufferedImage blend(BufferedImage image1, BufferedImage image2, double weightOfImage2) {
		Objects.requireNonNull(image1, "image1 must not be null");
		Objects.requireNonNull(image2, "image2 must not be null");

		if (!Double.isFinite(weightOfImage2) || weightOfImage2 < 0.0 || weightOfImage2 > 1.0)
			throw new IllegalArgumentException(
					"BufferedImageTools.blend: weightOfImage2 must be in [0, 1], got " + weightOfImage2);

		double weightOfImage1 = 1.0 - weightOfImage2;

		int width = Math.max(image1.getWidth(), image2.getWidth());
		int height = Math.max(image1.getHeight(), image2.getHeight());

		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				boolean in1 = x < image1.getWidth() && y < image1.getHeight();
				boolean in2 = x < image2.getWidth() && y < image2.getHeight();

				if (in1 && in2) {
					Color c1 = getColor(image1, x, y);
					Color c2 = getColor(image2, x, y);

					int r = (int) Math.round(c1.getRed() * weightOfImage1 + c2.getRed() * weightOfImage2);
					int g = (int) Math.round(c1.getGreen() * weightOfImage1 + c2.getGreen() * weightOfImage2);
					int b = (int) Math.round(c1.getBlue() * weightOfImage1 + c2.getBlue() * weightOfImage2);
					int a = (int) Math.round(c1.getAlpha() * weightOfImage1 + c2.getAlpha() * weightOfImage2);

					setColor(result, x, y, new Color(r, g, b, a));
				} else if (in1) {
					setColor(result, x, y, getColor(image1, x, y));
				} else if (in2) {
					setColor(result, x, y, getColor(image2, x, y));
				}
			}
		}

		return result;
	}

	// ==================== LOAD ====================

	/**
	 * @deprecated Use {@link #loadBufferedImage(String)} instead.
	 */
	@Deprecated
	public static BufferedImage loadImage(String filePath) {
		return loadBufferedImage(filePath);
	}

	public static BufferedImage loadBufferedImage(String filePath) {
		if (filePath == null || filePath.trim().isEmpty()) {
			LOG.warning("BufferedImageTools.loadBufferedImage: filePath is null or blank.");
			return null;
		}
		return loadBufferedImage(new File(filePath));
	}

	/**
	 * Loads a {@link BufferedImage} from the given file, retrying up to
	 * {@value #MAX_IMAGE_LOAD_ATTEMPTS} times on {@link IOException} with a
	 * {@value #RETRY_DELAY_MS} ms delay between attempts.
	 *
	 * @param file the image file to load
	 * @return the loaded image, or null if unreadable or not a recognized format
	 */
	public static BufferedImage loadBufferedImage(File file) {
		if (file == null) {
			LOG.warning("BufferedImageTools.loadBufferedImage: file is null.");
			return null;
		}

		for (int attempt = 1; attempt <= MAX_IMAGE_LOAD_ATTEMPTS; attempt++) {
			try {
				BufferedImage img = ImageIO.read(file);
				if (img == null)
					LOG.warning("BufferedImageTools.loadBufferedImage: ImageIO returned null"
							+ " (unrecognised format?) for " + file);
				return img;
			} catch (IOException | ConcurrentModificationException e) {
				Level level = (attempt < MAX_IMAGE_LOAD_ATTEMPTS) ? Level.FINE : Level.WARNING;
				LOG.log(level, "BufferedImageTools.loadBufferedImage: attempt " + attempt + " of "
						+ MAX_IMAGE_LOAD_ATTEMPTS + " failed for " + file, e);

				if (attempt < MAX_IMAGE_LOAD_ATTEMPTS)
					try {
						Thread.sleep(RETRY_DELAY_MS);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						LOG.log(Level.FINE, "BufferedImageTools.loadBufferedImage: sleep interrupted for " + file, ie);
						return null;
					}
			}
		}

		LOG.warning("BufferedImageTools.loadBufferedImage: all " + MAX_IMAGE_LOAD_ATTEMPTS + " attempts failed for "
				+ file + ". Returning null.");
		return null;
	}

	/**
	 * Loads all images from the given directory. Files that cannot be read or have
	 * unrecognized formats are skipped with a warning.
	 *
	 * <p>
	 * The {@code formatFilter} is matched against the file extension
	 * case-insensitively. Leading dots are stripped, so both {@code "png"} and
	 * {@code ".png"} are accepted. A blank filter is treated as null. Pass null to
	 * attempt loading all files.
	 * </p>
	 *
	 * @param directory    directory to scan; must not be null
	 * @param recursive    if true, sub-directories are scanned recursively
	 * @param formatFilter file extension to include, e.g. {@code "png"}; null
	 *                     accepts all files
	 * @return list of successfully loaded images, never null
	 */
	public static List<BufferedImage> loadImages(File directory, boolean recursive, String formatFilter) {
		Objects.requireNonNull(directory, "directory must not be null");
		return loadImagesInternal(directory, recursive, normalizeFormatFilter(formatFilter));
	}

	/**
	 * Loads all images from the given directory path.
	 *
	 * @param directoryPath directory path; null or blank returns an empty list
	 * @param recursive     if true, sub-directories are scanned recursively
	 * @param formatFilter  file extension to include, e.g. {@code "png"}; null
	 *                      accepts all files
	 * @return list of successfully loaded images, never null
	 */
	public static List<BufferedImage> loadImages(String directoryPath, boolean recursive, String formatFilter) {
		List<BufferedImage> images = new ArrayList<>();

		if (directoryPath == null || directoryPath.trim().isEmpty()) {
			LOG.warning("BufferedImageTools.loadImages: directoryPath is null or blank.");
			return images;
		}

		return loadImages(new File(directoryPath), recursive, formatFilter);
	}

	/**
	 * Downloads an image from the given URL.
	 *
	 * @param imageUrl the image URL
	 * @return the downloaded image, or null on failure or unrecognized format
	 */
	public static BufferedImage downloadImage(String imageUrl) {
		if (imageUrl == null || imageUrl.trim().isEmpty()) {
			LOG.warning("BufferedImageTools.downloadImage: imageUrl is null or blank.");
			return null;
		}

		try {
			BufferedImage image = ImageIO.read(URI.create(imageUrl).toURL());
			if (image == null)
				LOG.warning("BufferedImageTools.downloadImage: ImageIO returned null for " + imageUrl);
			return image;
		} catch (IOException e) {
			LOG.log(Level.WARNING, "BufferedImageTools.downloadImage: failed to download " + imageUrl, e);
			return null;
		}
	}

	public static Rectangle getRectangle(BufferedImage image) {
		Objects.requireNonNull(image, "image must not be null");
		return new Rectangle(image.getWidth(), image.getHeight());
	}

	// ==================== SAVE ====================

	/**
	 * Saves a {@link BufferedImage} to the given file using the specified format.
	 *
	 * <p>
	 * JPEG does not support transparency. If the format is JPEG and the image has
	 * an alpha channel, it is composited onto a white background before writing.
	 * </p>
	 *
	 * @param image  image to save; must not be null
	 * @param file   target file; must not be null
	 * @param format ImageIO format string, e.g. {@code "png"} or {@code "jpeg"};
	 *               must not be null or blank
	 * @return true if saved successfully, false on failure or if no writer exists
	 */
	public static boolean saveImage(BufferedImage image, File file, String format) {
		Objects.requireNonNull(image, "image must not be null");
		Objects.requireNonNull(file, "file must not be null");
		Objects.requireNonNull(format, "format must not be null");

		String normalizedFormat = format.trim().toLowerCase(Locale.ROOT);
		if (normalizedFormat.isEmpty())
			throw new IllegalArgumentException("BufferedImageTools.saveImage: format must not be blank.");

		try {
			FileTools.createParentDirectory(file);

			BufferedImage toWrite = isJpegFormat(normalizedFormat) ? stripAlpha(image) : image;
			boolean written = ImageIO.write(toWrite, normalizedFormat, file);
			if (!written)
				LOG.warning("BufferedImageTools.saveImage: no ImageIO writer found for format \"" + normalizedFormat
						+ "\".");
			return written;
		} catch (IOException e) {
			LOG.log(Level.WARNING, "BufferedImageTools.saveImage: failed to save " + file, e);
			return false;
		}
	}

	/**
	 * Saves a {@link BufferedImage} to the given file path using the specified
	 * format.
	 *
	 * @param image    image to save; must not be null
	 * @param filePath target file path; must not be null or blank
	 * @param format   ImageIO format string; must not be null or blank
	 * @return true if saved successfully
	 */
	public static boolean saveImage(BufferedImage image, String filePath, String format) {
		Objects.requireNonNull(filePath, "filePath must not be null");
		if (filePath.trim().isEmpty())
			throw new IllegalArgumentException("BufferedImageTools.saveImage: filePath must not be blank.");
		return saveImage(image, new File(filePath), format);
	}

	/**
	 * Saves a {@link BufferedImage} to the given file, inferring the format from
	 * the file extension. Falls back to PNG if the extension is un-recognised.
	 *
	 * @param image image to save; must not be null
	 * @param file  target file; must not be null
	 * @return true if saved successfully
	 */
	public static boolean saveImage(BufferedImage image, File file) {
		Objects.requireNonNull(image, "image must not be null");
		Objects.requireNonNull(file, "file must not be null");
		return saveImage(image, file, formatFromFile(file));
	}

	/**
	 * Saves a {@link BufferedImage} to the given file path, inferring the format
	 * from the file extension.
	 *
	 * @param image    image to save; must not be null
	 * @param filePath target file path; must not be null or blank
	 * @return true if saved successfully
	 */
	public static boolean saveImage(BufferedImage image, String filePath) {
		Objects.requireNonNull(image, "image must not be null");
		Objects.requireNonNull(filePath, "filePath must not be null");
		if (filePath.trim().isEmpty())
			throw new IllegalArgumentException("BufferedImageTools.saveImage: filePath must not be blank.");
		return saveImage(image, new File(filePath));
	}

	/**
	 * Converts an {@link Image} to a {@link BufferedImage} and saves it using the
	 * specified format.
	 *
	 * @param image  image to save; must not be null and must have available
	 *               dimensions
	 * @param file   target file; must not be null
	 * @param format ImageIO format string; must not be null or blank
	 * @return true if saved successfully
	 * @throws IllegalArgumentException if image dimensions are not available
	 */
	public static boolean saveImage(Image image, File file, String format) {
		Objects.requireNonNull(image, "image must not be null");
		Objects.requireNonNull(file, "file must not be null");
		Objects.requireNonNull(format, "format must not be null");
		return saveImage(toBufferedImage(image), file, format);
	}

	/**
	 * Converts an {@link Image} to a {@link BufferedImage} and saves it using the
	 * specified format.
	 *
	 * @param image    image to save; must not be null and must have available
	 *                 dimensions
	 * @param filePath target file path; must not be null or blank
	 * @param format   ImageIO format string; must not be null or blank
	 * @return true if saved successfully
	 * @throws IllegalArgumentException if image dimensions are not available
	 */
	public static boolean saveImage(Image image, String filePath, String format) {
		Objects.requireNonNull(image, "image must not be null");
		Objects.requireNonNull(filePath, "filePath must not be null");
		if (filePath.trim().isEmpty())
			throw new IllegalArgumentException("BufferedImageTools.saveImage: filePath must not be blank.");
		return saveImage(toBufferedImage(image), new File(filePath), format);
	}

	/**
	 * Converts an {@link Image} to a {@link BufferedImage} and saves it, inferring
	 * the format from the file extension.
	 *
	 * @param image image to save; must not be null and must have available
	 *              dimensions
	 * @param file  target file; must not be null
	 * @return true if saved successfully
	 * @throws IllegalArgumentException if image dimensions are not available
	 */
	public static boolean saveImage(Image image, File file) {
		Objects.requireNonNull(image, "image must not be null");
		Objects.requireNonNull(file, "file must not be null");
		return saveImage(toBufferedImage(image), file);
	}

	/**
	 * Converts an {@link Image} to a {@link BufferedImage} and saves it, inferring
	 * the format from the file extension.
	 *
	 * @param image    image to save; must not be null and must have available
	 *                 dimensions
	 * @param filePath target file path; must not be null or blank
	 * @return true if saved successfully
	 * @throws IllegalArgumentException if image dimensions are not available
	 */
	public static boolean saveImage(Image image, String filePath) {
		Objects.requireNonNull(image, "image must not be null");
		Objects.requireNonNull(filePath, "filePath must not be null");
		if (filePath.trim().isEmpty())
			throw new IllegalArgumentException("BufferedImageTools.saveImage: filePath must not be blank.");
		return saveImage(toBufferedImage(image), new File(filePath));
	}

	/**
	 * Saves a {@link BufferedImage} as PNG to the given file.
	 *
	 * @param image image to save; must not be null
	 * @param file  target file; must not be null
	 * @return true if saved successfully
	 */
	public static boolean savePNG(BufferedImage image, File file) {
		return saveImage(image, file, FORMAT_PNG);
	}

	/**
	 * Saves a {@link BufferedImage} as PNG to the given file path.
	 *
	 * @param image    image to save; must not be null
	 * @param filePath target file path; must not be null or blank
	 * @return true if saved successfully
	 */
	public static boolean savePNG(BufferedImage image, String filePath) {
		return saveImage(image, filePath, FORMAT_PNG);
	}

	/**
	 * Saves a {@link BufferedImage} as JPEG to the given file.
	 *
	 * <p>
	 * Alpha channels are not supported by JPEG. If the image has transparency, it
	 * is composited onto a white background before writing.
	 * </p>
	 *
	 * @param image image to save; must not be null
	 * @param file  target file; must not be null
	 * @return true if saved successfully
	 */
	public static boolean saveJPEG(BufferedImage image, File file) {
		return saveImage(image, file, FORMAT_JPEG);
	}

	/**
	 * Saves a {@link BufferedImage} as JPEG to the given file path.
	 *
	 * <p>
	 * Alpha channels are not supported by JPEG. If the image has transparency, it
	 * is composited onto a white background before writing.
	 * </p>
	 *
	 * @param image    image to save; must not be null
	 * @param filePath target file path; must not be null or blank
	 * @return true if saved successfully
	 */
	public static boolean saveJPEG(BufferedImage image, String filePath) {
		return saveImage(image, filePath, FORMAT_JPEG);
	}

	/**
	 * Writes pre-encoded image bytes directly to the given file without re-encoding
	 * through ImageIO. Use when bytes were obtained from an external source and are
	 * already in the target format.
	 * 
	 * Parent directories are created on demand.
	 *
	 * @param imageBytes encoded image bytes; must not be null
	 * @param file       target file; must not be null
	 * @return true if written successfully
	 */
	public static boolean saveImage(byte[] imageBytes, File file) {
		Objects.requireNonNull(imageBytes, "imageBytes must not be null");
		Objects.requireNonNull(file, "file must not be null");

		try {
			FileTools.writeBytes(file, imageBytes);
			return true;
		} catch (UncheckedIOException e) {
			LOG.log(Level.WARNING, "BufferedImageTools.saveImage: failed to write bytes to " + file, e);
			return false;
		}
	}

	/**
	 * Writes pre-encoded image bytes directly to the given file path.
	 *
	 * @param imageBytes encoded image bytes; must not be null
	 * @param filePath   target file path; must not be null or blank
	 * @return true if written successfully
	 */
	public static boolean saveImage(byte[] imageBytes, String filePath) {
		Objects.requireNonNull(filePath, "filePath must not be null");
		if (filePath.trim().isEmpty())
			throw new IllegalArgumentException("BufferedImageTools.saveImage: filePath must not be blank.");
		return saveImage(imageBytes, new File(filePath));
	}

	// ==================== COPY / CROP / COMPOSITION ====================

	/**
	 * Returns a deep copy of the given image with ARGB type.
	 *
	 * @throws NullPointerException if {@code source} is null
	 */
	public static BufferedImage copy(BufferedImage source) {
		Objects.requireNonNull(source, "source must not be null");

		BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = copy.createGraphics();
		try {
			g2d.drawImage(source, 0, 0, null);
		} finally {
			g2d.dispose();
		}
		return copy;
	}

	/**
	 * Returns a sub-image view defined by the given rectangle.
	 *
	 * <p>
	 * The returned image shares raster data with the source. Changes to one may
	 * affect the other. Use {@link #crop} for an independent deep copy.
	 * </p>
	 *
	 * @return the sub-image, or null if either argument is null
	 * @throws RasterFormatException if the rectangle is outside the image bounds
	 */
	public static BufferedImage subImage(BufferedImage bufferedImage, Rectangle rectangle) {
		if (bufferedImage == null || rectangle == null)
			return null;

		return bufferedImage.getSubimage((int) rectangle.getMinX(), (int) rectangle.getMinY(),
				(int) rectangle.getWidth(), (int) rectangle.getHeight());
	}

	/**
	 * Crops the image to the given bounds and returns a deep copy.
	 *
	 * @throws NullPointerException     if {@code source} is null
	 * @throws IllegalArgumentException if the region is outside the image bounds
	 */
	public static BufferedImage crop(BufferedImage source, int x, int y, int width, int height) {
		Objects.requireNonNull(source, "source must not be null");

		if (x < 0 || y < 0 || width <= 0 || height <= 0 || x + width > source.getWidth()
				|| y + height > source.getHeight())
			throw new IllegalArgumentException("BufferedImageTools.crop: region (" + x + ", " + y + ", " + width + "x"
					+ height + ") is outside image bounds " + source.getWidth() + "x" + source.getHeight());

		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = result.createGraphics();
		try {
			g2d.drawImage(source, 0, 0, width, height, x, y, x + width, y + height, null);
		} finally {
			g2d.dispose();
		}
		return result;
	}

	/**
	 * Crops the image to the given rectangle and returns a deep copy.
	 *
	 * @throws NullPointerException if {@code source} or {@code rectangle} is null
	 */
	public static BufferedImage crop(BufferedImage source, Rectangle rectangle) {
		Objects.requireNonNull(source, "source must not be null");
		Objects.requireNonNull(rectangle, "rectangle must not be null");
		return crop(source, rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}

	public static BufferedImage maskOutside(BufferedImage image, Shape area, Color fillColor) {
		Objects.requireNonNull(image, "image must not be null");
		Objects.requireNonNull(area, "area must not be null");
		Objects.requireNonNull(fillColor, "fillColor must not be null");

		BufferedImage copy = copy(image);
		for (int x = 0; x < copy.getWidth(); x++)
			for (int y = 0; y < copy.getHeight(); y++)
				if (!area.contains(x, y))
					setColor(copy, x, y, fillColor);

		return copy;
	}

	/**
	 * Draws {@code overlay} onto {@code base} at the given position with the given
	 * opacity. Modifies {@code base} in place and returns it for convenience.
	 *
	 * @throws NullPointerException     if {@code base} or {@code overlay} is null
	 * @throws IllegalArgumentException if opacity is not in [0.0, 1.0]
	 */
	public static BufferedImage overlay(BufferedImage base, BufferedImage overlay, int x, int y, float opacity) {
		Objects.requireNonNull(base, "base must not be null");
		Objects.requireNonNull(overlay, "overlay must not be null");

		if (!Float.isFinite(opacity) || opacity < 0.0f || opacity > 1.0f)
			throw new IllegalArgumentException("BufferedImageTools.overlay: opacity must be in [0, 1], got " + opacity);

		Graphics2D g2d = base.createGraphics();
		try {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			g2d.drawImage(overlay, x, y, null);
		} finally {
			g2d.dispose();
		}
		return base;
	}

	/**
	 * Combines a grid of same-sized images into a single image. All tiles must be
	 * non-null and have identical dimensions.
	 *
	 * @throws NullPointerException     if {@code bufferedImages} is null
	 * @throws IllegalArgumentException if the grid is empty, ragged, contains null
	 *                                  tiles, or tiles with different sizes
	 */
	public static BufferedImage gridToSingle(BufferedImage[][] bufferedImages) {
		Objects.requireNonNull(bufferedImages, "bufferedImages must not be null");

		if (bufferedImages.length == 0)
			throw new IllegalArgumentException("BufferedImageTools.gridToSingle: grid must not be empty.");
		if (bufferedImages[0] == null || bufferedImages[0].length == 0)
			throw new IllegalArgumentException("BufferedImageTools.gridToSingle: first row must not be null or empty.");
		if (bufferedImages[0][0] == null)
			throw new IllegalArgumentException("BufferedImageTools.gridToSingle: first tile must not be null.");

		int tileW = bufferedImages[0][0].getWidth();
		int tileH = bufferedImages[0][0].getHeight();
		int xCount = bufferedImages.length;
		int yCount = bufferedImages[0].length;

		BufferedImage result = new BufferedImage(tileW * xCount, tileH * yCount, BufferedImage.TYPE_INT_ARGB);

		for (int x = 0; x < xCount; x++) {
			if (bufferedImages[x] == null || bufferedImages[x].length != yCount)
				throw new IllegalArgumentException("BufferedImageTools.gridToSingle: grid must be rectangular.");

			for (int y = 0; y < yCount; y++) {
				BufferedImage tile = bufferedImages[x][y];
				if (tile == null)
					throw new IllegalArgumentException(
							"BufferedImageTools.gridToSingle: tile at [" + x + "][" + y + "] must not be null.");
				if (tile.getWidth() != tileW || tile.getHeight() != tileH)
					throw new IllegalArgumentException(
							"BufferedImageTools.gridToSingle: all tiles must have the same size (" + tileW + "x" + tileH
									+ "), but tile [" + x + "][" + y + "] is " + tile.getWidth() + "x"
									+ tile.getHeight() + ".");

				overlay(result, tile, x * tileW, y * tileH, 1.0f);
			}
		}

		return result;
	}

	// ==================== COLOR CONVERSION ====================

	/**
	 * Returns a gray-scale copy of the given image with alpha preserved.
	 *
	 * <p>
	 * Uses ITU-R BT.709 luminance coefficients, the same as
	 * {@link #getLuminanceForPixel}. Output type is {@code TYPE_INT_ARGB} for
	 * consistency with the rest of the class.
	 * </p>
	 *
	 * @throws NullPointerException if {@code source} is null
	 */
	public static BufferedImage toGrayscale(BufferedImage source) {
		Objects.requireNonNull(source, "source must not be null");

		BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

		for (int x = 0; x < source.getWidth(); x++) {
			for (int y = 0; y < source.getHeight(); y++) {
				int argb = source.getRGB(x, y);
				int alpha = (argb >>> 24) & 0xFF;
				int r = (argb >>> 16) & 0xFF;
				int g = (argb >>> 8) & 0xFF;
				int b = argb & 0xFF;
				int gray = Math.min(255, (int) Math.round(0.2126 * r + 0.7152 * g + 0.0722 * b));
				result.setRGB(x, y, (alpha << 24) | (gray << 16) | (gray << 8) | gray);
			}
		}

		return result;
	}

	public static BufferedImage adjustBrightnessContrast(BufferedImage image, float contrast, float brightness) {
		Objects.requireNonNull(image, "image must not be null");

		BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		RescaleOp op = new RescaleOp(new float[] { contrast, contrast, contrast, 1.0f },
				new float[] { brightness, brightness, brightness, 0.0f }, null);
		op.filter(image, result);
		return result;
	}

	// ==================== HASH AND FILE IDENTITY ====================

	/**
	 * Returns a hash code based on all decoded pixel RGB values. Useful for
	 * comparing image content regardless of file format or encoding. Not suitable
	 * for cryptographic purposes.
	 *
	 * @throws NullPointerException if {@code img} is null
	 * @see #getMD5Hash(File) for file-level byte identity
	 */
	public static int hashCodeFromPixelColors(BufferedImage img) {
		Objects.requireNonNull(img, "img must not be null");

		int hash = 29;
		for (int x = 0; x < img.getWidth(); x++)
			for (int y = 0; y < img.getHeight(); y++)
				hash = 31 * hash + img.getRGB(x, y);

		return hash;
	}

	/**
	 * Returns the MD5 hash of the given image file as a zero-padded 32-character
	 * hex string, or null on failure.
	 *
	 * <p>
	 * This method hashes the raw file bytes without decoding the image. Two files
	 * with identical pixels but different compression or metadata will produce
	 * different hashes. It is suitable for file-level change detection, caching,
	 * and de-duplication. It is not suitable for pixel-level image comparison or
	 * cryptographic integrity verification.
	 * </p>
	 *
	 * @deprecated use FileTools.getMD5Hash
	 * @param file the image file; must not be null
	 * @return 32-character MD5 hex string, or null on failure
	 * @see #hashCodeFromPixelColors(BufferedImage) for decoded pixel identity
	 */
	@Deprecated
	public static String getMD5Hash(File file) {
		return FileTools.getMD5Hash(file);
	}

	/**
	 * Returns the MD5 hash of the image file at the given path, or null on failure.
	 *
	 * @deprecated use FileTools.getMD5Hash
	 * @param filePath path to the image file; null or blank returns null
	 * @return 32-character MD5 hex string, or null on failure
	 */
	@Deprecated
	public static String getMD5Hash(String filePath) {
		return FileTools.getMD5Hash(filePath);
	}

	/**
	 * Returns true if the given file exists and ImageIO can find a reader for it.
	 *
	 * <p>
	 * This checks reader availability by inspecting the file stream without fully
	 * decoding raster data. It is a cheap format probe, not a guarantee that the
	 * file is fully readable.
	 * </p>
	 *
	 * @param file the file to test; null returns false
	 * @return true if ImageIO recognizes the file as a readable image format
	 */
	public static boolean isReadableImage(File file) {
		if (file == null || !file.exists() || !file.isFile())
			return false;

		try (ImageInputStream stream = ImageIO.createImageInputStream(file)) {
			if (stream == null)
				return false;
			Iterator<?> readers = ImageIO.getImageReaders(stream);
			return readers.hasNext();
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Returns true if the file at the given path exists and ImageIO can find a
	 * reader for it.
	 *
	 * @param filePath path to the file; null or blank returns false
	 * @return true if ImageIO recognizes the file as a readable image format
	 */
	public static boolean isReadableImage(String filePath) {
		if (filePath == null || filePath.trim().isEmpty())
			return false;
		return isReadableImage(new File(filePath));
	}

	// ==================== TEXT ====================

	/**
	 * Renders the given string as a {@link BufferedImage} using SansSerif 48pt. The
	 * text is rendered in white on a transparent background.
	 *
	 * @throws NullPointerException if {@code string} is null
	 */
	public static BufferedImage stringToBufferedImage(String string) {
		Objects.requireNonNull(string, "string must not be null");

		Font font = new Font("SansSerif", Font.PLAIN, 48);

		BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = probe.createGraphics();
		g2d.setFont(font);
		FontMetrics fm = g2d.getFontMetrics();
		int width = Math.max(1, fm.stringWidth(string));
		int height = Math.max(1, fm.getHeight());
		g2d.dispose();

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		g2d = img.createGraphics();
		try {
			g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g2d.setFont(font);
			g2d.setColor(Color.WHITE);
			g2d.drawString(string, 0, g2d.getFontMetrics().getAscent());
		} finally {
			g2d.dispose();
		}

		return img;
	}

	// ==================== PRIVATE HELPERS ====================

	private static List<BufferedImage> loadImagesInternal(File directory, boolean recursive, String normalizedFilter) {
		List<BufferedImage> images = new ArrayList<>();

		if (!directory.isDirectory()) {
			LOG.warning("BufferedImageTools.loadImages: not a directory: " + directory);
			return images;
		}

		File[] files = directory.listFiles();
		if (files == null)
			return images;

		for (File file : files) {
			if (file.isDirectory()) {
				if (recursive)
					images.addAll(loadImagesInternal(file, true, normalizedFilter));
				continue;
			}

			if (normalizedFilter != null) {
				String name = file.getName().toLowerCase(Locale.ROOT);
				if (!name.endsWith("." + normalizedFilter))
					continue;
			}

			BufferedImage img = loadBufferedImage(file);
			if (img != null)
				images.add(img);
		}

		return images;
	}

	/**
	 * Normalizes a file-extension filter. Leading dots are removed. Blank values
	 * become null.
	 */
	private static String normalizeFormatFilter(String formatFilter) {
		if (formatFilter == null)
			return null;

		String normalizedFilter = formatFilter.trim().toLowerCase(Locale.ROOT);
		if (normalizedFilter.startsWith("."))
			normalizedFilter = normalizedFilter.substring(1);
		return normalizedFilter.isEmpty() ? null : normalizedFilter;
	}

	/**
	 * Infers an ImageIO format string from the file extension. Falls back to
	 * {@value #FORMAT_PNG} for unrecognized or missing extensions.
	 */
	private static String formatFromFile(File file) {
		if (file == null)
			return FORMAT_PNG;

		String name = file.getName();
		int dot = name.lastIndexOf('.');
		if (dot < 0 || dot == name.length() - 1)
			return FORMAT_PNG;

		String ext = name.substring(dot + 1).toLowerCase(Locale.ROOT);
		return switch (ext) {
		case "jpg", "jpeg" -> FORMAT_JPEG;
		case "png" -> FORMAT_PNG;
		case "gif" -> "gif";
		case "bmp" -> "bmp";
		case "wbmp" -> "wbmp";
		default -> FORMAT_PNG;
		};
	}

	/**
	 * Returns true if the given normalized format string identifies JPEG. Accepts
	 * both {@code "jpg"} and {@code "jpeg"} since callers may pass either before
	 * normalization.
	 */
	private static boolean isJpegFormat(String format) {
		return FORMAT_JPEG.equals(format) || "jpg".equals(format);
	}

	/**
	 * Composites the image onto a white background to remove the alpha channel.
	 * Returns the original image unchanged if it is already fully opaque.
	 */
	private static BufferedImage stripAlpha(BufferedImage image) {
		if (image.getTransparency() == Transparency.OPAQUE)
			return image;

		BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = rgb.createGraphics();
		try {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			g.drawImage(image, 0, 0, null);
		} finally {
			g.dispose();
		}
		return rgb;
	}
}