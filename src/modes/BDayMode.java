package modes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import brightness.BrightnessCorrection;
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


public class BDayMode implements IMode, Observer {

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
	
	public BDayMode(IDisplayAdaptor display, ILEDArray leds, IModeSelector modeSelector) {
		_display = display;
		_leds = leds;
		_modeSelector = modeSelector;
		_config = new DisplayConfiguration(modeName().toLowerCase() + ".properties", true);
		((Observable) _config).addObserver(this);
	}
	
	@Override
	public String modeName() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public void abort() {
		_aborted = true;
		System.out.println(modeName() + " abort() called");
	}

	@Override
	public void end() {
		_end = true;
		System.out.println(modeName() + " end() called");
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
		System.out.println(modeName() + " exit");
	}
	
	private void updateData() {
		_ageText.setText("30"); // TODO dynamic
	}
	
	private void reloadConfig() {
		System.out.println(modeName() + " config reload");
		try {
			String newBgColor = _config.getString("bg.Coloring", "effects.coloring.ColoringSolid");
			String newBgEffect = _config.getString("bg.Effect", "effects.background.SolidBackgroundEffect");
			String newTimeTextColor = _config.getString("timetext.Coloring", "effects.coloring.ColoringSolid");

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
			System.out.println(modeName() + " config updated");
			reloadConfig();		
		}
	}

}
