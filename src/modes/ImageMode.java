package modes;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;

import effects.IColor;
import effects.IColorableEffect;
import helper.FpsController;
import input.ButtonFeedbackLed;
import output.IDisplayAdaptor;
import led.ILEDArray;
import modeselection.IModeSelector;


public class ImageMode implements IMode, Observer {

	private static final String SLIDESHOW_DELAY = "slideshowDelay";
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private IModeSelector _modeSelector;
	private boolean _aborted = false;
	private boolean _end = false;
	private IDisplayConfiguration _config;
	private FpsController _fpsController = FpsController.getInstance();
	private ButtonFeedbackLed _buttonFeedbackLed = ButtonFeedbackLed.getInstance();
	
	private IColor _bgColor = null;
	private IColorableEffect _bg = null;
	private int _slideshowDelay = 10;
	private String _imageFolder;
	private long _lastImageChangeMs = 0;
	private int _currentImageIndex = 0;
	private double _currentFadeAlpha = 1;

	private Object _prevColor;

	private IColorableEffect _prev;

	private boolean _shouldFadeOnConfigChange;
	
	public ImageMode(IDisplayAdaptor display, ILEDArray leds, IModeSelector modeSelector, String configFileName) {
		_display = display;
		_leds = leds;
		_modeSelector = modeSelector;
		_config = new DisplayConfiguration(configFileName, true);
		((Observable) _config).addObserver(this);
	}
	
	@Override
	public String modeName() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public void abort() {
		_aborted = true;
		LOGGER.fine(modeName() + " abort() called");
	}

	@Override
	public void end() {
		_end = true;
		LOGGER.fine(modeName() + " end() called");
	}

	@Override
	public void run() {
		_aborted = false;
		_end = false;
		
		reloadConfig();
			
		while (!_aborted && !_end) {

			if (shouldChangeImageNow())
				nextImage();
			
			_leds.reset();
			
			if (_currentFadeAlpha < 1.0) {
				_leds.applyEffect(_prev);
				_currentFadeAlpha += 0.05;
				_bg.getColor().setAlpha(_currentFadeAlpha);
			}
			
			_leds.applyEffect(_bg);

			_display.show(_leds);

			_fpsController.waitForNextFrame();
		}
		_modeSelector.modeEnded();
		LOGGER.info(modeName() + " exit");
	}
	
	private boolean shouldChangeImageNow() {
		return _slideshowDelay > 0 && (Instant.now().toEpochMilli() - _lastImageChangeMs) >= (_slideshowDelay * 1000);
	}
	
	private void nextImage() {
		_lastImageChangeMs = Instant.now().toEpochMilli();
		ArrayList<File> files = getFilesInFolder();
		if (files == null)
			return;
		_config.setString("prev.effects.coloring.ColoringImage.filename", files.get(_currentImageIndex).getAbsolutePath());
		_currentImageIndex = (_currentImageIndex + 1) % files.size();
		_config.setString("bg.effects.coloring.ColoringImage.filename", files.get(_currentImageIndex).getAbsolutePath());
		_shouldFadeOnConfigChange = true;
	}
	
	 private ArrayList<File> getFilesInFolder() {
		 ArrayList<File> fileList = new ArrayList<File>();
		 File folder = new File(_imageFolder);
		 if (!folder.exists()) {
			 LOGGER.severe("Image folder does not exist: " + _imageFolder);
			 return null;
		 }
		 File[] files = folder.listFiles();
		 if (files.length == 0) {
			 LOGGER.severe("No (image) files in folder " + _imageFolder);
			 return null;
		 }
		 for(File file : files){
			 if(file.isFile())
				 try {
			        ImageIO.read(file).toString();
			        fileList.add(file);
			     } catch (Exception e) {
			    	 LOGGER.info("Skipping file " + _imageFolder + " in image folder " + _imageFolder + " because it seems to be no valid image");
			     }
		 }
		 if (fileList.size() == 0) {
			 LOGGER.severe("Image folder does not contain valid image files: " + _imageFolder);
			 return null;
		 }
		 return fileList;
    }
	
	private void reloadConfig() {
		LOGGER.info(modeName() + " config reload");
		try {
			String newBgColor = _config.getString("bg.Coloring", "effects.coloring.ColoringImage");
			String newBgEffect = _config.getString("bg.Effect", "effects.background.SolidBackgroundEffect");
			String newPrevColor = _config.getString("prev.Coloring", "effects.coloring.ColoringImage");
			String newPrevEffect = _config.getString("prev.Effect", "effects.background.SolidBackgroundEffect");
			_slideshowDelay = _config.getInt(SLIDESHOW_DELAY, 5);
			_imageFolder = _config.getString("imageFolder", "images");

			if (_bgColor == null || !newBgColor.endsWith(_bgColor.getClass().getCanonicalName()))
				_bgColor = (IColor) Class.forName(newBgColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "bg.");
				_bg = (IColorableEffect) Class.forName(newBgEffect).getConstructor(IColor.class).newInstance(_bgColor);
			if (_bg == null || !newBgEffect.endsWith(_bg.getClass().getCanonicalName()))
				_bg = (IColorableEffect) Class.forName(newBgEffect).getConstructor(IColor.class).newInstance(_bgColor); 
			
			if (_prevColor == null || !newPrevColor.endsWith(_prevColor.getClass().getCanonicalName()))
				_prevColor = (IColor) Class.forName(newPrevColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "prev.");
				_prev = (IColorableEffect) Class.forName(newPrevEffect).getConstructor(IColor.class).newInstance(_prevColor);
			if (_prev == null || !newPrevEffect.endsWith(_prev.getClass().getCanonicalName()))
				_prev = (IColorableEffect) Class.forName(newPrevEffect).getConstructor(IColor.class).newInstance(_prevColor); 
			
			if (_shouldFadeOnConfigChange) {
				_shouldFadeOnConfigChange = false;
				_currentFadeAlpha = 0.0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable observable, Object arg1) {
		if (observable instanceof IDisplayConfiguration) {
			LOGGER.info(modeName() + " config updated");
			reloadConfig();		
		}
	}

	@Override
	public void changeConfig(String newConfigFileName) {
		_config.changeConfigFile(newConfigFileName);
	}

	@Override
	public void buttonPressedShort() {
		nextImage();
		_buttonFeedbackLed.blinkOnce();
	}

	@Override
	public void buttonPressedLong() {
		switch (_slideshowDelay) {
			case 0:  
			default:
				_config.setString(SLIDESHOW_DELAY, "10");
				_buttonFeedbackLed.blinkOnce();
				break;
			case 10:  
				_config.setString(SLIDESHOW_DELAY, "30");
				_buttonFeedbackLed.blinkOnce();
				break;
			case 30:  
				_config.setString(SLIDESHOW_DELAY, "60");
				_buttonFeedbackLed.blinkTwice();
				break;
			case 60:  
				_config.setString(SLIDESHOW_DELAY, "300");
				_buttonFeedbackLed.blinkThreeTimes();
				break;
			case 300:  
				_config.setString(SLIDESHOW_DELAY, "0");
				_buttonFeedbackLed.blinkLong();
				break;
		}
	}
}
