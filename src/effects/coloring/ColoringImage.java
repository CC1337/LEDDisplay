package effects.coloring;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import configuration.IDisplayConfiguration;
import led.ILEDArray;
import effects.*;

public class ColoringImage implements IColor {
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private String _fileName;
	private double _alpha = 1.0;
	private int _scaledWidth = -1;
	private int _scaledHeight = -1;
	private int _offsetX = 0;
	private int _offsetY = 0;
	private boolean _center = false;
	private boolean _keepRatio;
	private IDisplayConfiguration _config;
	private String _configPrefix = "";
	private Color[][] _pixels;
	
	public ColoringImage(String fileName, double alpha) {

	}
	
	public ColoringImage(String fileName) {
		this(fileName, 1.0);
	}
	
	public ColoringImage(IDisplayConfiguration config, String configPrefix) {
		_config = config;
		_configPrefix = configPrefix;
	}

	@Override
	public void apply(ILEDArray leds, IColorableEffect effect) {
		setColorFromConfig();
		
		if (_pixels == null)
			try {
				updatePixelData();
			} catch (IOException e) {
				LOGGER.severe("Error reading ColoringImage image file " + new File(_fileName).getAbsolutePath() + ": " + e.toString());
				return;
			}
		
		byte[][] effectData = effect.getEffectData();
		for(int x=0; x<effectData.length; x++) {
			for(int y=0; y<effectData[0].length; y++) {
				if (effectData[x][y] == 1) {
					int posX = x+effect.getPosX();
					int posY = y+effect.getPosY();
					if (posX < 0 || posX > _pixels.length)
						continue;
					if (posY < 0 || posY > _pixels[posX].length)
						continue;
					Color color = _pixels[posX][posY];
					leds.blendLed(
							posX, 
							posY, 
							color.getRed(), 
							color.getGreen(), 
							color.getBlue(), 
							_alpha * ((double)color.getAlpha()/255.0));
				}
			}
		}		
	}
	
	private void updatePixelData() throws IOException {
		BufferedImage image = ImageIO.read(new File(_fileName));
		if (_scaledWidth != -1 || _scaledHeight != -1)
			image = getScaledImage(image);
		_pixels = getPixelData(image);
	}
	
	private BufferedImage getScaledImage(BufferedImage image) {
		int targetWidth = _scaledWidth;
		int targetHeight = _scaledHeight;

		if (_keepRatio && targetWidth > 0 && targetHeight > 0) {
			int originalWidth = image.getWidth();
			int originalHeight = image.getHeight();
			double scalingRatioWidth = (double)targetWidth / (double)originalWidth;
			double scalingRatioHeight = (double)targetHeight / (double)originalHeight;
			double scalingRatio = scalingRatioHeight;
			
			//LOGGER.info("pre scaling - w: " + targetWidth + " h: " + targetHeight + " org w: " + originalWidth + " org h: " + originalHeight + " rat-w: " + scalingRatioWidth + " rat-h:" + scalingRatioHeight);
			
			if (scalingRatioWidth < scalingRatioHeight) 
				scalingRatio = scalingRatioWidth;
			targetWidth = (int)Math.ceil((originalWidth * scalingRatio));
			targetHeight = (int)Math.ceil((originalHeight * scalingRatio));
			
			//LOGGER.info("post scaling - w: " + targetWidth + " h: " + targetHeight + " rat: " + scalingRatio);
		}
		
		return convertToBufferedImage(image.getScaledInstance(_scaledWidth, _scaledHeight, Image.SCALE_DEFAULT));
	}
	
	public static BufferedImage convertToBufferedImage(Image image)
	{
	    BufferedImage newImage = new BufferedImage(
	        image.getWidth(null), image.getHeight(null),
	        BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g = newImage.createGraphics();
	    g.drawImage(image, 0, 0, null);
	    g.dispose();
	    return newImage;
	}
	
	private Color[][] getPixelData(BufferedImage image) {

		int width = image.getWidth();
		int height = image.getHeight();
		Color[][] result = new Color[width][height];

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				result[col][row] = new Color(image.getRGB(col, row));
			}
		}

		return result;
	}
	
	public void setFileName(String fileName) {
		if (!_fileName.equals(fileName))
			clearPixelData();
		_fileName = fileName;
	}
	
	public void setScaledWidth(int width) {
		if (width == -1 || width > 0) {
			_scaledWidth = width;
			clearPixelData();
		}
	}
	
	public void setScaledHeight(int height) {
		if (height == -1 || height > 0) {
			_scaledHeight = height;
			clearPixelData();
		}
	}
	
	public void setOffsetX(int offset) {
		_offsetX = offset;
		clearPixelData();
	}
	
	public void setOffsetY(int offset) {
		_offsetY = offset;
		clearPixelData();
	}
	
	public void setCenter(boolean center) {
		_center = center;
		clearPixelData();
	}
	
	public void setKeepRatio(boolean keepRatio) {
		_keepRatio = keepRatio;
		clearPixelData();
	}
	
	public void setAlpha(double alpha) {
		if (alpha >= 0.0 || alpha <= 1.0)
			_alpha = alpha;
	}
	
	public String getFileName() {
		return _fileName;
	}
	
	public double getAlpha() {
		return _alpha;
	}
	
	public int getScaledWidth() {
		return _scaledWidth;
	}
	
	public int getScaledHeight() {
		return _scaledHeight;
	}
	
	public int getOffsetX() {
		return _offsetX;
	}
	
	public int getOffsety() {
		return _offsetY;
	}
	
	public boolean getCenter() {
		return _center;
	}
	
	public boolean getKeepRatio() {
		return _keepRatio;
	}
	
	public void setColorFromConfig() {
		if (_config == null) 
			return;
		
		String newFileName = _fileName = _config.getString(getConfigKey("filename"));
		int newScaledWidth = _config.getInt(getConfigKey("width"), -1);
		int newScaledHeight = _config.getInt(getConfigKey("height"), -1);
		int newOffsetX = _config.getInt(getConfigKey("offsetx"), 0);
		int newOffsetY = _config.getInt(getConfigKey("offsety"), 0);
		boolean newCenter = _config.getInt(getConfigKey("center"), 0) == 1;
		boolean newKeepRatio = _config.getInt(getConfigKey("keepratio"), 1) == 1;
		_alpha = _config.getDouble(getConfigKey("alpha"), 1.0);
		
		if (!_fileName.equals(newFileName) || newScaledWidth != _scaledWidth || newScaledHeight != _scaledHeight || newKeepRatio != _keepRatio || 
				newOffsetX != _offsetX || newOffsetY != _offsetY || newCenter != _center)
			clearPixelData();
		
		_fileName = newFileName;
		_scaledWidth = newScaledWidth;
		_scaledHeight = newScaledHeight;
		_offsetX = newOffsetX;
		_offsetY = newOffsetY;
		_center = newCenter;
		_keepRatio = newKeepRatio;
	}
	
	private void clearPixelData() {
		_pixels = null;
	}
	
	private String getConfigKey(String param) {
		return _configPrefix + this.getClass().getName() + "." + param;
	}
}
