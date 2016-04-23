package modes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import net.PvData;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;

import effects.IColor;
import effects.IColorableEffect;
import effects.info.PvChartEffect;
import effects.text.*;
import helper.FpsController;
import helper.Helper;
import output.IDisplayAdaptor;
import led.ILEDArray;

public class ClockMode implements IMode, Observer {

	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private IModeSelector _modeSelector;
	private boolean _aborted = false;
	private boolean _end = false;
	private IDisplayConfiguration _config;
	private PvData _pvData = PvData.getInstance();
	private FpsController _fpsController = FpsController.getInstance();
	
	private IColor _bgColor = null;
	private IColor _timeTextColor = null;
	private IColor _pvTextColor = null;
	private IColorableEffect _bg = null;
	private IPixelatedFont _font = new PixelatedFont(new FontDefault7px());
	TextEffect _timeText = null;
	TextEffect _dateText = null;
	TextEffect _pvText = null;
	PvChartEffect _pvDayChart = null;
	int _displayDateEverySeconds = 30;
	int _displayDateForSeconds = 5;
	
	
	public ClockMode(IDisplayAdaptor display, ILEDArray leds, IModeSelector modeSelector) {
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
		System.out.println(modeName() + " abort() called");
	}

	@Override
	public void run() {
		_aborted = false;
		_end = false;
		
		reloadConfig();
					
		String currentTime;
		Calendar calendar;
		int currentMinute;
		int currentSecond;
		
		int lastPvUpdate = 1337;
		_pvText.setText(getPvText());
		
		while (!_aborted && !_end) {
			
			currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
			calendar = Calendar.getInstance();
			currentMinute = calendar.get(Calendar.MINUTE);
			currentSecond = calendar.get(Calendar.SECOND);

			_timeText.setText(currentTime);
			
			if (currentMinute != lastPvUpdate) {
				_pvText.setText(getPvText());
				_pvDayChart.updateData();
				lastPvUpdate = currentMinute;
			}
			
			_leds.applyEffect(_bg);
			_leds.applyEffect(_timeText);
			if (_displayDateEverySeconds > 0 && currentSecond % _displayDateEverySeconds < _displayDateForSeconds) {
				_dateText.setText(new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
				_leds.applyEffect(_dateText);
			} else {
				_leds.applyEffect(_pvText);
				if (_pvData.getCurrentPac() == 0) {
					_leds.applyEffect(_pvDayChart);
				}
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
			_displayDateEverySeconds = _config.getInt("displayDateEverySeconds", 30);
			_displayDateForSeconds = _config.getInt("displayDateForSeconds", 5);
						
			String newBgColor = _config.getString("bg.Coloring", "effects.coloring.ColoringSolid");
			String newBgEffect = _config.getString("bg.Effect", "effects.background.SolidBackgroundEffect");
			String newTimetextColor = _config.getString("timetext.Coloring", "effects.coloring.ColoringSolid");
			String newPvtextColor = _config.getString("pvtext.Coloring", "effects.coloring.ColoringSolid");

			if (_bgColor == null || !newBgColor.endsWith(_bgColor.getClass().getCanonicalName()))
				_bgColor = (IColor) Class.forName(newBgColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "bg.");
				_bg = (IColorableEffect) Class.forName(newBgEffect).getConstructor(IColor.class).newInstance(_bgColor);
			if (_bg == null || !newBgEffect.endsWith(_bg.getClass().getCanonicalName()))
				_bg = (IColorableEffect) Class.forName(newBgEffect).getConstructor(IColor.class).newInstance(_bgColor); 
			if (_timeTextColor == null || !newTimetextColor.endsWith(_timeTextColor.getClass().getCanonicalName())) {
				_timeTextColor = (IColor) Class.forName(newTimetextColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "timetext.");
				_timeText = new TextEffect(_font, _timeTextColor, _timeText != null ? _timeText.getText() : "1337 ALTA!", 7, 0);
				_dateText = new TextEffect(_font, _timeTextColor, _dateText != null ? _dateText.getText() : "1337 ALTA!", 1, 8);
			}
			if (_pvTextColor == null || !newPvtextColor.endsWith(_pvTextColor.getClass().getCanonicalName())) {
				_pvTextColor = (IColor) Class.forName(newPvtextColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "pvtext.");
				_pvText = new TextEffect(_font, _pvTextColor, _pvText != null ? _pvText.getText() : "1337 ALTA!", 1, 8);
				_pvDayChart = new PvChartEffect(0, 8, 36, 7, _pvTextColor, PvChartEffect.RenderData.PRODUCTION);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getPvText() {
		int pac = _pvData.getCurrentPac();
		double kwh = _pvData.getKwhDay();
		return String.format("%dW%s%.1f", pac, Helper.getSpaces(4-String.valueOf(pac).length() + 5-String.valueOf(kwh).length()) , (float)kwh).replace("0W", "  ");
	}
	
	@Override
	public void update(Observable observable, Object arg1) {
		if (observable instanceof IDisplayConfiguration) {
			System.out.println(modeName() + " config updated");
			reloadConfig();		
		}
	}

}
