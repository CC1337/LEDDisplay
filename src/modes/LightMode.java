package modes;

import java.lang.invoke.MethodHandles;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;

import effects.IColor;
import effects.IColorableEffect;
import helper.FpsController;
import output.IDisplayAdaptor;
import led.ILEDArray;
import modeselection.IModeSelector;


public class LightMode implements IMode, Observer {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private IModeSelector _modeSelector;
	private boolean _aborted = false;
	private boolean _end = false;
	private IDisplayConfiguration _config;
	private FpsController _fpsController = FpsController.getInstance();
	
	private IColor _bgColor = null;
	private IColorableEffect _bg = null;
	
	public LightMode(IDisplayAdaptor display, ILEDArray leds, IModeSelector modeSelector, String configFileName) {
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
			_leds.reset();
			_leds.applyEffect(_bg);			
			
			_display.show(_leds);

			_fpsController.waitForNextFrame();
		}
		_modeSelector.modeEnded();
		LOGGER.info(modeName() + " exit");
	}
	
	private void reloadConfig() {
		LOGGER.info(modeName() + " config reload");
		try {
			String newBgColor = _config.getString("bg.Coloring", "effects.coloring.ColoringSolid");
			String newBgEffect = _config.getString("bg.Effect", "effects.background.SolidBackgroundEffect");

			if (_bgColor == null || !newBgColor.endsWith(_bgColor.getClass().getCanonicalName()))
				_bgColor = (IColor) Class.forName(newBgColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "bg.");
				_bg = (IColorableEffect) Class.forName(newBgEffect).getConstructor(IColor.class).newInstance(_bgColor);
			if (_bg == null || !newBgEffect.endsWith(_bg.getClass().getCanonicalName()))
				_bg = (IColorableEffect) Class.forName(newBgEffect).getConstructor(IColor.class).newInstance(_bgColor); 
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
	}

	@Override
	public void buttonPressedLong() {
	}
}
