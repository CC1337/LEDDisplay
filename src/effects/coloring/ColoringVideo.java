package effects.coloring;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import configuration.IDisplayConfiguration;
import led.ILEDArray;
import marvin.gui.MarvinImagePanel;
import marvin.image.MarvinImage;
import marvin.video.MarvinJavaCVAdapter;
import marvin.video.MarvinVideoInterface;
import marvin.video.MarvinVideoInterfaceException;
import effects.*;
import helper.DebouncedFileModifiedWatch;
import helper.IDebounceFileWatchListener;

public class ColoringVideo implements IColor, Observer, IDebounceFileWatchListener {
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private String _fileName;
	private double _alpha = 1.0;
	private int _offsetX = 0;
	private int _offsetY = 0;
	private boolean _center = false;
	private IDisplayConfiguration _config;
	private String _configPrefix = "";
	private Color[][] _pixels;
	private DebouncedFileModifiedWatch _fileWatch;
	private MarvinVideoInterface _videoAdapter = null;
	//private MarvinImagePanel videoPanel = new MarvinImagePanel();
	//private MarvinImage videoFrame, videoOut = new MarvinImage(640,480);

	
	public ColoringVideo(String fileName, double alpha) {
		_fileWatch = new DebouncedFileModifiedWatch(fileName, this);
	}
	
	public ColoringVideo(String fileName) {
		this(fileName, 1.0);
	}
	
	public ColoringVideo(IDisplayConfiguration config, String configPrefix) {
		_config = config;
		_configPrefix = configPrefix;
		setColorFromConfig();
		((Observable) _config).addObserver(this);
	}

	@Override
	public void apply(ILEDArray leds, IColorableEffect effect) {
		if (_pixels == null)
			try {
				updatePixelData();
			} catch (IOException e) {
				LOGGER.severe("Error reading ColoringVideo video file " + new File(_fileName).getAbsolutePath() + ": " + e.toString());
				return;
			}
		
		byte[][] effectData = effect.getEffectData();
		
		int offsetX = getEffectOffsetX(effectData);
		int offsetY = getEffectOffsetY(effectData);
		
		for(int x=0; x<effectData.length; x++) {
			for(int y=0; y<effectData[0].length; y++) {
				if (effectData[x][y] == 1) {
					int posX = x+effect.getPosX();
					int posXPixels = posX - offsetX;
					int posY = y+effect.getPosY();
					int posYPixels = posY - offsetY;
					
					if (posX < 0 || posXPixels >= _pixels.length || posXPixels < 0)
						continue;
					if (posY < 0 || posYPixels >= _pixels[posXPixels].length || posYPixels < 0)
						continue;
					
					Color color = _pixels[posXPixels][posYPixels];
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
	
	private int getEffectOffsetX(byte[][] effectData) {
		if (_pixels == null)
			return 0;
		if (_center)
			return (int)Math.floor((double)(effectData.length - _pixels.length)/2.0);
		return _offsetX;
	}
	
	private int getEffectOffsetY(byte[][] effectData) {
		if (_pixels == null)
			return 0;
		if (_center)
			return (int)Math.floor((double)(effectData[0].length - _pixels[0].length)/2.0);
		return _offsetY;
	}
	
	private void initVideoAdapter() {
		_videoAdapter = new MarvinJavaCVAdapter();
		try {
			_videoAdapter.loadResource(_fileName);
		} catch (MarvinVideoInterfaceException e) {
			LOGGER.severe("ColoringVideo exception loading video file '"+_fileName+"': " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void updatePixelData() throws IOException {
		if (_videoAdapter == null)
			initVideoAdapter();
		if (_videoAdapter == null)
			return;
		try {
			MarvinImage frame = _videoAdapter.getFrame();
			BufferedImage image = frame.getBufferedImage();
			_pixels = getPixelData(image);
		} catch (MarvinVideoInterfaceException e) {
			LOGGER.severe("ColoringVideo exception loading video frame: " + e.getMessage());
			e.printStackTrace();
		}
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
		if (fileName == null)
			return;
		if (!fileName.equals(_fileName)) {
			clearPixelData();
			_fileWatch.changeFile(fileName);
		}
		_fileName = fileName;
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
	
	public int getOffsetX() {
		return _offsetX;
	}
	
	public int getOffsety() {
		return _offsetY;
	}
	
	public boolean getCenter() {
		return _center;
	}
	
	public void setColorFromConfig() {
		if (_config == null) 
			return;
	
		String newFileName = _config.getString(getConfigKey("filename"));
		
		if (newFileName == null) {
			LOGGER.info("Skipping ColoringVideo config update becasuse no filename set in config " + _config.getConfigFileName());
			return;
		}
		
		int newOffsetX = _config.getInt(getConfigKey("offsetx"), 0);
		int newOffsetY = _config.getInt(getConfigKey("offsety"), 0);
		boolean newCenter = _config.getInt(getConfigKey("center"), 0) == 1;
		_alpha = _config.getDouble(getConfigKey("alpha"), 1.0);
		
		if (!newFileName.equals(_fileName) || newOffsetX != _offsetX || newOffsetY != _offsetY || newCenter != _center)
			clearPixelData();
		
		if (_fileWatch == null)
			_fileWatch = new DebouncedFileModifiedWatch(newFileName, this);
		
		setFileName(newFileName);
		_offsetX = newOffsetX;
		_offsetY = newOffsetY;
		_center = newCenter;
	}
	
	private void clearPixelData() {
		_pixels = null;
	}
	
	private String getConfigKey(String param) {
		return _configPrefix + this.getClass().getName() + "." + param;
	}

	@Override
	public void update(Observable observable, Object arg1) {
		if (observable instanceof IDisplayConfiguration) {
			LOGGER.info("Updating ColoringVideo '" + _fileName + "' (config change)");
			setColorFromConfig();
		}
	}

	@Override
	public void fileChanged(String fileName) {
		LOGGER.info("Updating ColoringVideo '" + _fileName + "' (image file change)");
		clearPixelData();
	}
}
