package effects.coloring;

import java.awt.Color;
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
		_pixels = getPixelData(image);
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
	
	public void setAlpha(double alpha) {
		if (_alpha >= 0.0 || alpha <= 1.0)
			_alpha = alpha;
	}
	
	public String getFileName() {
		return _fileName;
	}
	
	public double getAlpha() {
		return _alpha;
	}
	
	public void setColorFromConfig() {
		if (_config != null) {
			String newFileName = _fileName = _config.getString(getConfigKey("filename"));
			if (!_fileName.equals(newFileName))
				clearPixelData();
			_fileName = _config.getString(getConfigKey("filename"));
			_alpha = _config.getDouble(getConfigKey("alpha"), 1.0);
		}
	}
	
	private void clearPixelData() {
		_pixels = null;
	}
	
	private String getConfigKey(String param) {
		return _configPrefix + this.getClass().getName() + "." + param;
	}
}
