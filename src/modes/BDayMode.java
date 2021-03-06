package modes;

import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;

import effects.IColor;
import effects.IColorableEffect;
import effects.background.SolidBackgroundEffect;
import effects.coloring.ColoringSolid;
import effects.text.*;
import helper.FpsController;
import output.IDisplayAdaptor;
import led.ILEDArray;
import modeselection.IModeSelector;


public class BDayMode implements IMode, Observer {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private IModeSelector _modeSelector;
	private boolean _aborted = false;
	private boolean _end = false;
	private IDisplayConfiguration _config;
	private FpsController _fpsController = FpsController.getInstance();
	
	private IColor _bgColor = null;
	private IColor _timeTextColor = null;
	private IColorableEffect _bg = null;
	private IPixelatedFont _font = new PixelatedFont(new FontBold10px(), 3);
	TextEffect _ageText = null;
	String _configuredBirthDate = null;
	
	public BDayMode(IDisplayAdaptor display, ILEDArray leds, IModeSelector modeSelector, String configFileName) {
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

		// Start with flashing effect
		SolidBackgroundEffect flashBackground = new SolidBackgroundEffect(new ColoringSolid(255, 255, 255));
		for (int i=0; i<150; i++) {
			_leds.reset();
			if (i%2 == 0)
				_leds.applyEffect(flashBackground);
			_display.show(_leds);
			_fpsController.waitForNextFrame();
		}

		// Show age then
		while (!_aborted && !_end) {
			updateData();			
			
			_leds.reset();
			_leds.applyEffect(_bg);			
			_leds.applyEffect(_ageText);
			
			_display.show(_leds);

			_fpsController.waitForNextFrame();
		}
		_modeSelector.modeEnded();
		LOGGER.info(modeName() + " exit");
	}
	
	private void updateData() {
		Date bDay = new Date();
		try {
			bDay = new SimpleDateFormat("dd.MM.yyyy").parse(_configuredBirthDate);
		} catch (ParseException e) {
			LOGGER.severe("Invalid birthday date in BDayMode config: " + _configuredBirthDate + " - format must be: DD.MM.YYYY");
			e.printStackTrace();
		}
		_ageText.setText(String.valueOf(yearsSince(bDay)));
	}
	
	private int yearsSince(Date pastDate) {
	    Calendar present = Calendar.getInstance();
	    Calendar past = Calendar.getInstance();
	    past.setTime(pastDate);

	    int years = 0;

	    while (past.before(present)) {
	        past.add(Calendar.YEAR, 1);
	        if (past.before(present)) {
	            years++;
	        }
	    } return years;
	}

	private void reloadConfig() {
		LOGGER.info(modeName() + " config reload");
		try {
			String newBgColor = _config.getString("bg.Coloring", "effects.coloring.ColoringSolid");
			String newBgEffect = _config.getString("bg.Effect", "effects.background.SolidBackgroundEffect");
			String newTimeTextColor = _config.getString("timetext.Coloring", "effects.coloring.ColoringSolid");
			_configuredBirthDate = _config.getString("birthday");

			if (_bgColor == null || !newBgColor.endsWith(_bgColor.getClass().getCanonicalName()))
				_bgColor = (IColor) Class.forName(newBgColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "bg.");
				_bg = (IColorableEffect) Class.forName(newBgEffect).getConstructor(IColor.class).newInstance(_bgColor);
			if (_bg == null || !newBgEffect.endsWith(_bg.getClass().getCanonicalName()))
				_bg = (IColorableEffect) Class.forName(newBgEffect).getConstructor(IColor.class).newInstance(_bgColor); 
			if (_timeTextColor == null || !newTimeTextColor.endsWith(_timeTextColor.getClass().getCanonicalName())) {
				_timeTextColor = (IColor) Class.forName(newTimeTextColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "timetext.");
				_ageText = new TextEffect(_font, _timeTextColor, _ageText != null ? _ageText.getText() : "", 24, 3);
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
	}

	@Override
	public void buttonPressedLong() {
	}
}
