package modes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import configuration.CycleableModeConfiguration;
import configuration.IDisplayConfiguration;

import effects.IColor;
import effects.IColorableEffect;
import effects.info.PvChartEffect;
import effects.shape.RectEffect;
import effects.text.*;
import helper.FpsController;
import output.IDisplayAdaptor;
import led.ILEDArray;
import modeselection.IModeSelector;


public class InfoMode implements IMode, Observer {

	private enum InfoType {
		INFOTEXT,
		PVDAYCHART,
		NEWSTEXT
	}
	
	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private IModeSelector _modeSelector;
	private boolean _aborted = false;
	private boolean _end = false;
	private CycleableModeConfiguration _config;
	private FpsController _fpsController = FpsController.getInstance();
	
	private IColor _bgColor = null;
	private IColor _timeTextColor = null;
	private IColor _pvTextColorPositive = null;
	private IColor _pvTextColorNegative = null;
	private IColor _infoTextColor = null;
	private IColor _secondPixelColor = null;
	private IColorableEffect _bg = null;
	private IPixelatedFont _font = new PixelatedFont(new FontDefault7px());
	TextEffect _timeText = null;
	CurrentD0PacTextEffect _d0Text = null;
	InfoTextEffect _infoText = null;
	RectEffect _secondPixel = null;
	PvChartEffect _pvDayChart = null;
	NewsMarqueeTextEffect _newsText = null;
	int _newsEnabled = 1;
	int _infoChangeDelay = 5;
	boolean _showSecondPixel = true;
	
	
	public InfoMode(IDisplayAdaptor display, ILEDArray leds, IModeSelector modeSelector) {
		_display = display;
		_leds = leds;
		_modeSelector = modeSelector;
		_config = new CycleableModeConfiguration(modeName().toLowerCase(), true);
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
					
		String currentTime;
		Calendar calendar;
		int currentSecond;
		InfoType currentInfo = InfoType.INFOTEXT;
		
		int lastInfoUpdate = 1337;

		while (!_aborted && !_end) {

			currentTime = new SimpleDateFormat("HH:mm").format(new Date());
			calendar = Calendar.getInstance();
			currentSecond = calendar.get(Calendar.SECOND);

			_timeText.setText(currentTime);

			_leds.applyEffect(_bg);
			_leds.applyEffect(_timeText);
			_leds.applyEffect(_d0Text);
			
			if (currentInfo == InfoType.INFOTEXT) {
				if (lastInfoUpdate != currentSecond && currentSecond % _infoChangeDelay == 0) {
					lastInfoUpdate = currentSecond;
					_infoText.nextInfo();
				}
				if (_infoText.loopEnded()) {
					currentInfo = InfoType.PVDAYCHART;
					_infoText.toStart();
				}
			}
			_leds.applyEffect(_infoText);
			
			if (currentInfo == InfoType.PVDAYCHART) {
				_leds.applyEffect(_pvDayChart);
				if (lastInfoUpdate != currentSecond && currentSecond % (2*_infoChangeDelay) == 0) {
					currentInfo = InfoType.NEWSTEXT;
				}
			}

			if (currentInfo == InfoType.NEWSTEXT) {
				if (_newsEnabled == 1) {
					_leds.applyEffect(_newsText);
					if (_newsText.shift())
						currentInfo = InfoType.INFOTEXT;
				} else {
					currentInfo = InfoType.INFOTEXT;
				}
			}
			
			if (_showSecondPixel) {
				_secondPixel.setPosX(currentSecond);
				_leds.applyEffect(_secondPixel);
			}

			_display.show(_leds);

			_fpsController.waitForNextFrame();
		}
		_modeSelector.modeEnded();
		System.out.println(modeName() + " exit");
	}
	
	private void reloadConfig() {
		System.out.println(modeName() + " config reload");
		try {
			_infoChangeDelay = _config.getInt("infoChangeDelay", 5);
			_newsEnabled = _config.getInt("newsEnabled", 1);
			String newBgColor = _config.getString("bg.Coloring", "effects.coloring.ColoringSolid");
			String newBgEffect = _config.getString("bg.Effect", "effects.background.SolidBackgroundEffect");
			String newTimetextColor = _config.getString("timetext.Coloring", "effects.coloring.ColoringSolid");
			String newPvtextColorPositive = _config.getString("pvtextpositive.Coloring", "effects.coloring.ColoringSolid");
			String newPvtextColorNegative = _config.getString("pvtextnegative.Coloring", "effects.coloring.ColoringSolid");
			String newInfotextColor = _config.getString("infotext.Coloring", "effects.coloring.ColoringSolid");
			String newSecondPixelColor = _config.getString("second.Coloring", "effects.coloring.ColoringSolid");

			if (_bgColor == null || !newBgColor.endsWith(_bgColor.getClass().getCanonicalName()))
				_bgColor = (IColor) Class.forName(newBgColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "bg.");
				_bg = (IColorableEffect) Class.forName(newBgEffect).getConstructor(IColor.class).newInstance(_bgColor);
			if (_bg == null || !newBgEffect.endsWith(_bg.getClass().getCanonicalName()))
				_bg = (IColorableEffect) Class.forName(newBgEffect).getConstructor(IColor.class).newInstance(_bgColor); 
			if (_timeTextColor == null || !newTimetextColor.endsWith(_timeTextColor.getClass().getCanonicalName())) {
				_timeTextColor = (IColor) Class.forName(newTimetextColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "timetext.");
				_timeText = new TextEffect(_font, _timeTextColor, _timeText != null ? _timeText.getText() : "1337 ALTA!", 1, 0);
			}
			if (_pvTextColorPositive == null || !newPvtextColorPositive.endsWith(_pvTextColorPositive.getClass().getCanonicalName())) {
				_pvTextColorPositive = (IColor) Class.forName(newPvtextColorPositive).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "pvtextpositive.");
				_pvTextColorNegative = (IColor) Class.forName(newPvtextColorNegative).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "pvtextnegative.");
				_d0Text = new CurrentD0PacTextEffect(_font, _pvTextColorPositive, _pvTextColorNegative, 31, 0);
				_pvDayChart = new PvChartEffect(-5, 7, 65, 9, _pvTextColorPositive, PvChartEffect.RenderData.PRODUCTION, true);
			}
			if (_infoTextColor == null || !newInfotextColor.endsWith(_infoTextColor.getClass().getCanonicalName())) {
				_infoTextColor = (IColor) Class.forName(newInfotextColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "infotext.");
				_infoText = new InfoTextEffect(_font, _infoTextColor, 1, 8);
			}
			if (_secondPixelColor == null || !newSecondPixelColor.endsWith(_secondPixelColor.getClass().getCanonicalName())) {
				_secondPixelColor = (IColor) Class.forName(newSecondPixelColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "second.");
				_secondPixel = new RectEffect(0, 7, 1, 1, _secondPixelColor);
			}
			if (_newsEnabled == 1) {
				_newsText = new NewsMarqueeTextEffect(_font, 1, 8, _leds.sizeX(), _config, "newstext.");
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

	@Override
	public void nextConfig() {
		_config.nextConfiguration();
	}
}
