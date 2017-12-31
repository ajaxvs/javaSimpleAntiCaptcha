package ru.ajaxvs.images;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;


/**
 * Images class.
 * 
 * <br><br>Pixels array format: [y][x]:
 * <br> 00 01 02 03 04
 * <br> 10 11 12 13 14
 * 
 * @author ajaxvs.
 * 
 */
public class CajImage {
	//========================================
	protected String id;
	protected String address;	
	protected int[][] aPixels;
	protected int width;
	protected int height;
	protected boolean isLoaded;
	protected boolean hasIgnoredColor;
	protected int ignoredColor;
	//===
	protected BufferedImage img;
	protected boolean isTransparent;
	//========================================
	/**
	 * Creates new object. Use load() later.
	 * @param id any String for identification
	 */
	public CajImage(String id) {
		this.id = id;
		isLoaded = false;
		hasIgnoredColor = false;
	}
	//========================================
	/**
	 * Loads an image to the pixels array.
	 * @param address file address, i.e. "http://ex.com/url.png" or "c:/local.png".
	 *  <br>Note: address like "file:///c:/local.png" is common URL format.
	 * @param isLocal set true for local address format
	 * @param rgbParsing set true if image has unknown or GIF type 
	 *  and can't be parsed using fast getRaster().getDataBuffer() way. 
	 */
	public boolean load(String address, boolean isLocal, boolean rgbParsing) 
					throws MalformedURLException, IOException {
		this.address = address;
		
		if (isLocal) {
			img = ImageIO.read(new File(address));
		} else {			
			img = ImageIO.read(new URL(address));			
		}
		
		width = img.getWidth();
		height = img.getHeight();
		aPixels = new int[height][width];
		
		if (rgbParsing) {
			createPixelArrayFromRGBParsing();
		} else {
			createPixelArrayFromDataBuffer();
		}

		isLoaded = true;
		
		return isLoaded;
	}
	//========================================
	/**
	 * Checks if image was loaded successfully. 
	 * There're no any other checks in this class for faster performance.
	 * @return true if everything is ok. 
	 */
	public boolean isLoaded() {
		return isLoaded;
	}
	//========================================
	/**
	 * @return original file address.
	 */
	public String getAddress() {
		return address;
	}
	//========================================
	/**
	 * @return custom id that was set in constructor.
	 */
	public String getId() {
		return id;
	}
	//========================================
	/**
	 * Direct access to image pixels.
	 * <br>We really need MUCH better performance, so nn any sort of encapsulation here.
	 * @return loaded pixels array. 
	 */
	public int[][] getPixelArray() {
		return aPixels;
	}
	//========================================
	/**
	 * @param x column.
	 * @param y row.
	 * @return ARGB color of pixel.
	 */
	public int getPixel(int x, int y) {
		return aPixels[y][x];
	}
	//========================================
	/**
	 * @param x column.
	 * @param y row.
	 * @return hex string of pixel.
	 */
	public String getHexPixel(int x, int y) {
		return Integer.toHexString(aPixels[y][x]);
	}
	//========================================
	/**
	 * Using for fast debug. 
	 * @return hex string of last image's pixel.
	 */
	public String getLastHexPixel() {
		return Integer.toHexString(aPixels[height-1][width-1]);
	}
	//========================================
	/**
	 * @return image's width in pixels.
	 */
	public int getWidth() {
		return width;
	}
	//========================================
	/**
	 * @return image's height in pixels.
	 */	
	public int getHeight() {
		return height;
	}
	//========================================
	/**
	 * Ignored color is using for images matching.
	 * @param state is ignored color using.
	 * @param color color that won't be count during images matching.
	 */
	public void setIgnoredColor(boolean state, int color) {
		hasIgnoredColor = state;
		ignoredColor = color;
	}
	//========================================
	/**
	 * Find another image in this current one.
	 * @param image another image.
	 * @param startX
	 * @param startY
	 * @return found image's coords or null if not found.
	 */
	public Point findInside(CajImage image, int startX, int startY) {		
		int fWidth = image.getWidth();
		int fHeight = image.getHeight();
		boolean fHIC = image.hasIgnoredColor;
		int fIC = image.ignoredColor;
		int[][] fPixels = image.getPixelArray();

		int maxX0 = width - fWidth + 1;
		int maxY0 = height - fHeight + 1;
		
		int x0, y0, x1, y1, color0, color1;
		for (y0 = startY; y0 < maxY0; y0++) {
			LABEL_NEXT_ORIGINAL_PIXEL:
			for (x0 = startX; x0 < maxX0; x0++) {
				for (y1 = 0; y1 < fHeight; y1++) {
					for (x1 = 0; x1 < fWidth; x1++) {
						color0 = aPixels[y0 + y1][x0 + x1];
						color1 = fPixels[y1][x1];
						if (fHIC && fIC == color1) {
							//atm images are matching
						} else if (color0 != color1) {
							continue LABEL_NEXT_ORIGINAL_PIXEL;
						}
					}
				}
				//found:
				Point p = new Point();
				p.x = x0;
				p.y = y0;
				return p;
			}
		}
		
		return null;
	}
	//========================================
	/**
	 * @private
	 */
	private void createPixelArrayFromDataBuffer() {
		final int nonTransparentAlpha = -16777216;
		
		DataBuffer dataBuffer = img.getRaster().getDataBuffer();
		DataBufferByte buffer = (DataBufferByte) dataBuffer;

		byte[] bufferData = buffer.getData();
		
		int bytesPerPixel;
		if (img.getAlphaRaster() == null) {
			isTransparent = false;
			bytesPerPixel = 3;
		} else {
			isTransparent = true;
			bytesPerPixel = 4;
		}

		int row = 0;
		int col = 0;		
		int pixel = 0;
		int color = 0;
		
		for (;;) {
			if (isTransparent) {
				color = (((int) bufferData[pixel] & 0xff) << 24) +
						(((int) bufferData[pixel + 3] & 0xff) << 16) +
						(((int) bufferData[pixel + 2] & 0xff) << 8) +
						((int) bufferData[pixel + 1] & 0xff);
			} else {
				color = nonTransparentAlpha +
					    (((int) bufferData[pixel + 2] & 0xff) << 16) +
					    (((int) bufferData[pixel + 1] & 0xff) << 8) +
						((int) bufferData[pixel] & 0xff);
			}			
			aPixels[row][col] = color;
			if (++col == width) {
				col = 0;
				row++;
			}
			pixel += bytesPerPixel;
			if (pixel >= bufferData.length) {
				break;
			}
		}
	}
	//========================================
	/**
	 * @private
	 */
	private void createPixelArrayFromRGBParsing() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				aPixels[y][x] = img.getRGB(x, y);
			}
		}
	}
	//========================================
}
