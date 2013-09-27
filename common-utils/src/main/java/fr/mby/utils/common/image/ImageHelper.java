/**
 * Copyright 2013 Maxime Bossard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.mby.utils.common.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * This class can be used to convert images. Note that all the methods of this class are declared as static. Supports
 * the following image operations
 * <ul>
 * <li>Convert between Image and BufferedImage</li>
 * <li>Split images</li>
 * <li>Resize image</li>
 * <li>Create tiled image</li>
 * <li>Create empty transparent image</li>
 * <li>Create a colored image</li>
 * <li>Flip image horizontally</li>
 * <li>Flip image vertically</li>
 * <li>Clone image</li>
 * <li>Rotate image</li>
 * </ul>
 * 
 * @author Sri Harsha Chilakapati
 * @author Maxime Bossard - 2013
 */
public abstract class ImageHelper {

	/**
	 * Converts a given Image into a BufferedImage
	 * 
	 * @param img
	 *            The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(final Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}
		// Create a buffered image with transparency
		final BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		// Draw the image on to the buffered image
		final Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();
		// Return the buffered image
		return bimage;
	}

	/**
	 * Splits an image into a number of rows and columns
	 * 
	 * @param img
	 *            The image to be split
	 * @param rows
	 *            The number of rows
	 * @param cols
	 *            The number of columns
	 * @return The array of split images in the vertical order
	 */
	public static BufferedImage[] splitImage(final Image img, final int rows, final int cols) {
		// Determine the width of each part
		final int w = img.getWidth(null) / cols;
		// Determine the height of each part
		final int h = img.getHeight(null) / rows;
		// Determine the number of BufferedImages to be created
		final int num = rows * cols;
		// The count of images we'll use in looping
		int count = 0;
		// Create the BufferedImage array
		final BufferedImage[] imgs = new BufferedImage[num];
		// Start looping and creating images [splitting]
		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < cols; y++) {
				// The BITMASK type allows us to use bmp images with coloured
				// text and any background
				imgs[count] = new BufferedImage(w, h, Transparency.BITMASK);
				// Get the Graphics2D object of the split part of the image
				final Graphics2D g = imgs[count++].createGraphics();
				// Draw only the required portion of the main image on to the
				// split image
				g.drawImage(img, 0, 0, w, h, w * y, h * x, w * y + w, h * x + h, null);
				// Now Dispose the Graphics2D class
				g.dispose();
			}
		}
		return imgs;
	}

	/**
	 * Converts a given BufferedImage into an Image
	 * 
	 * @param bimage
	 *            The BufferedImage to be converted
	 * @return The converted Image
	 */
	public static Image toImage(final BufferedImage bimage) {
		// Casting is enough to convert from BufferedImage to Image
		final Image img = bimage;
		return img;
	}

	/**
	 * Resizes a given image to given width and height
	 * 
	 * @param img
	 *            The image to be resized
	 * @param width
	 *            The new width
	 * @param height
	 *            The new height
	 * @return The resized image
	 */
	public static Image resize(final Image img, final int width, final int height) {
		// Create a null image
		Image image = null;
		// Resize into a BufferedImage
		final BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D bGr = bimg.createGraphics();
		bGr.drawImage(img, 0, 0, width, height, null);
		bGr.dispose();
		// Convert to Image and return it
		image = ImageHelper.toImage(bimg);
		return image;
	}

	public static BufferedImage resizeWithoutHint(final Image originalImage, final int width, final int height,
			final int type) {
		final BufferedImage resizedImage = new BufferedImage(width, height, type);

		final Graphics2D graphics2D = resizedImage.createGraphics();
		graphics2D.drawImage(originalImage, 0, 0, width, height, null);
		graphics2D.dispose();

		return resizedImage;
	}

	public static BufferedImage resizeWithHint(final Image originalImage, final int width, final int height,
			final int type) {
		final BufferedImage resizedImage = new BufferedImage(width, height, type);

		final Graphics2D graphics2D = resizedImage.createGraphics();
		graphics2D.setComposite(AlphaComposite.Src);
		// below three lines are for RenderingHints for better image quality at cost of higher processing time
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2D.drawImage(originalImage, 0, 0, width, height, null);
		graphics2D.dispose();

		return resizedImage;
	}

	public static BufferedImage resize(final BufferedImage image, final int width, final int height,
			final boolean keepScale, final boolean withHint) {
		final int oldWidth = image.getWidth();
		final int oldHeight = image.getHeight();

		int newWidth = -1;
		int newHeight = -1;

		if (keepScale) {
			// Ratio of image to resize
			final double ratio = (double) (oldWidth) / oldHeight;
			// Ratio of frame the thumbnail will be embbed
			final double frameRatio = (double) (width) / height;

			if (ratio < frameRatio) {
				newHeight = height;
				newWidth = (int) (newHeight * ratio);
			} else {
				newWidth = width;
				newHeight = (int) (newWidth / ratio);
			}
		} else {
			newWidth = width;
			newHeight = height;
		}

		final int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();

		return withHint ? ImageHelper.resizeWithHint(image, newWidth, newHeight, type) : ImageHelper.resizeWithoutHint(
				image, newWidth, newHeight, type);
	}

	public static byte[] toByteArray(final BufferedImage image, final String format) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, format, output);

		final byte[] data = output.toByteArray();
		output.close();

		return data;
	}

	/**
	 * Creates a tiled image with an image upto given width and height
	 * 
	 * @param img
	 *            The source image
	 * @param width
	 *            The width of image to be created
	 * @param height
	 *            The height of the image to be created
	 * @return The created image
	 */
	public static Image createTiledImage(final Image img, final int width, final int height) {
		// Create a null image
		Image image = null;
		final BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		// The width and height of the given image
		final int imageWidth = img.getWidth(null);
		final int imageHeight = img.getHeight(null);
		// Start the counting
		final int numX = (width / imageWidth) + 2;
		final int numY = (height / imageHeight) + 2;
		// Create the graphics context
		final Graphics2D bGr = bimg.createGraphics();
		for (int y = 0; y < numY; y++) {
			for (int x = 0; x < numX; x++) {
				bGr.drawImage(img, x * imageWidth, y * imageHeight, null);
			}
		}
		// Convert and return the image
		image = ImageHelper.toImage(bimg);
		return image;
	}

	/**
	 * Creates an empty image with transparency
	 * 
	 * @param width
	 *            The width of required image
	 * @param height
	 *            The height of required image
	 * @return The created image
	 */
	public static Image getEmptyImage(final int width, final int height) {
		final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		return ImageHelper.toImage(img);
	}

	/**
	 * Creates a colored image with a specified color
	 * 
	 * @param color
	 *            The color to be filled with
	 * @param width
	 *            The width of the required image
	 * @param height
	 *            The height of the required image
	 * @return The created image
	 */
	public static Image getColoredImage(final Color color, final int width, final int height) {
		final BufferedImage img = ImageHelper.toBufferedImage(ImageHelper.getEmptyImage(width, height));
		final Graphics2D g = img.createGraphics();
		g.setColor(color);
		g.fillRect(0, 0, width, height);
		g.dispose();
		return img;
	}

	/**
	 * Flips an image horizontally. (Mirrors it)
	 * 
	 * @param img
	 *            The source image
	 * @return The image after flip
	 */
	public static Image flipImageHorizontally(final Image img) {
		final int w = img.getWidth(null);
		final int h = img.getHeight(null);
		final BufferedImage bimg = ImageHelper.toBufferedImage(ImageHelper.getEmptyImage(w, h));
		final Graphics2D g = bimg.createGraphics();
		g.drawImage(img, 0, 0, w, h, w, 0, 0, h, null);
		g.dispose();
		return ImageHelper.toImage(bimg);
	}

	/**
	 * Flips an image vertically. (Mirrors it)
	 * 
	 * @param img
	 *            The source image
	 * @return The image after flip
	 */
	public static Image flipImageVertically(final Image img) {
		final int w = img.getWidth(null);
		final int h = img.getHeight(null);
		final BufferedImage bimg = ImageHelper.toBufferedImage(ImageHelper.getEmptyImage(w, h));
		final Graphics2D g = bimg.createGraphics();
		g.drawImage(img, 0, 0, w, h, 0, h, w, 0, null);
		g.dispose();
		return ImageHelper.toImage(bimg);
	}

	/**
	 * Clones an image. After cloning, a copy of the image is returned.
	 * 
	 * @param img
	 *            The image to be cloned
	 * @return The clone of the given image
	 */
	public static Image clone(final Image img) {
		final BufferedImage bimg = ImageHelper.toBufferedImage(ImageHelper.getEmptyImage(img.getWidth(null),
				img.getHeight(null)));
		final Graphics2D g = bimg.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return ImageHelper.toImage(bimg);
	}

	/**
	 * Rotates an image. Actually rotates a new copy of the image.
	 * 
	 * @param img
	 *            The image to be rotated
	 * @param angle
	 *            The angle in degrees
	 * @return The rotated image
	 */
	public static Image rotate(final Image img, final double angle) {
		final double sin = Math.abs(Math.sin(Math.toRadians(angle))), cos = Math.abs(Math.cos(Math.toRadians(angle)));
		final int w = img.getWidth(null), h = img.getHeight(null);
		final int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);
		final BufferedImage bimg = ImageHelper.toBufferedImage(ImageHelper.getEmptyImage(neww, newh));
		final Graphics2D g = bimg.createGraphics();

		g.translate((neww - w) / 2, (newh - h) / 2);
		g.rotate(Math.toRadians(angle), w / 2, h / 2);
		g.drawRenderedImage(ImageHelper.toBufferedImage(img), null);
		g.dispose();
		return ImageHelper.toImage(bimg);
	}

	/**
	 * Rotates an image. Actually rotates a new copy of the image.
	 * 
	 * @param img
	 *            The image to be rotated
	 * @param angle
	 *            The angle in degrees
	 * @return The rotated image
	 */
	public static Image rotateWithHint(final Image img, final double angle) {
		final double sin = Math.abs(Math.sin(Math.toRadians(angle))), cos = Math.abs(Math.cos(Math.toRadians(angle)));
		final int w = img.getWidth(null), h = img.getHeight(null);
		final int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);
		// final BufferedImage bimg = ImageHelper.toBufferedImage(ImageHelper.getEmptyImage(neww, newh));
		final BufferedImage bimg = new BufferedImage(neww, newh, ImageHelper.toBufferedImage(img).getType());
		final Graphics2D g = bimg.createGraphics();
		g.setComposite(AlphaComposite.Src);
		// below three lines are for RenderingHints for better image quality at cost of higher processing time
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.translate((neww - w) / 2, (newh - h) / 2);
		g.rotate(Math.toRadians(angle), w / 2, h / 2);
		g.drawImage(ImageHelper.toBufferedImage(img), null, 0, 0);
		// g.drawRenderedImage(ImageHelper.toBufferedImage(img), null);
		g.dispose();
		return ImageHelper.toImage(bimg);
	}

	/**
	 * Makes a color in an Image transparent.
	 */
	public static Image mask(final Image img, final Color color) {
		final BufferedImage bimg = ImageHelper.toBufferedImage(ImageHelper.getEmptyImage(img.getWidth(null),
				img.getHeight(null)));
		final Graphics2D g = bimg.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		for (int y = 0; y < bimg.getHeight(); y++) {
			for (int x = 0; x < bimg.getWidth(); x++) {
				final int col = bimg.getRGB(x, y);
				if (col == color.getRGB()) {
					bimg.setRGB(x, y, col & 0x00ffffff);
				}
			}
		}
		return ImageHelper.toImage(bimg);
	}

}
