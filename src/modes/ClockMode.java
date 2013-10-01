package modes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.PvData;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;

import effects.IColor;
import effects.IColorableEffect;
import effects.text.*;
import output.IDisplayAdaptor;
import led.ILEDArray;

public class ClockMode implements IMode {

	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private IModeSelector _modeSelector;
	private boolean _aborted = false;
	private boolean _end = false;
	private IDisplayConfiguration _config;
	
	private IColor _bgColor = null;
	private IColor _timeTextColor = null;
	private IColor _pvTextColor = null;
	private IColorableEffect _bg = null;
	private IPixelatedFont _font = new PixelatedFont(new FontDefault7px());
	TextEffect _timeText = null;
	TextEffect _pvText = null;
	private PvData _pvData = PvData.getInstance();
	
	public ClockMode(IDisplayAdaptor display, ILEDArray leds, IModeSelector modeSelector) {
		_display = display;
		_leds = leds;
		_modeSelector = modeSelector;
		_config = new DisplayConfiguration("clockmode.properties", true);
	}
	
	@Override
	public void abort() {
		_aborted = true;
	}

	@Override
	public void end() {
		_end = true;
	}

	@Override
	public void run() {
		_aborted = false;
		_end = false;
		
		reloadConfig();
					
		String currentTime;
		Calendar calendar;
		int currentMinute;
		
		int lastPvUpdate = 1337;
		_pvText.setText(getPvText());
		
		while (!_aborted && !_end) {
			reloadConfig();
			
			currentTime = new SimpleDateFormat("H:mm:ss").format(new Date());
			calendar = Calendar.getInstance();
			currentMinute = calendar.get(Calendar.MINUTE);
			_timeText.setText(currentTime);
			
			if (currentMinute != lastPvUpdate) {
				_pvText.setText(getPvText());
				lastPvUpdate = currentMinute;
			}
			
			_leds.applyEffect(_bg);
			_leds.applyEffect(_timeText);
			_leds.applyEffect(_pvText);
			_display.show(_leds);
			
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				break;
			}
			
		}
		_modeSelector.modeEnded();
		System.out.println("ClockMode exit");
	}
	
	private void reloadConfig() {
		try {
			String newBgColor = _config.getString("bg.Coloring", "effects.coloring.ColoringSolid");
			String newBgEffect = _config.getString("bg.Effect", "effects.background.SolidBackgroundEffect");
			String newTimetextColor = _config.getString("timetext.Coloring", "effects.coloring.ColoringSolid");
			String newPvtextColor = _config.getString("pvtext.Coloring", "effects.coloring.ColoringSolid");

			if (_bgColor == null || !newBgColor.endsWith(_bgColor.getClass().getCanonicalName()))
				_bgColor = (IColor) Class.forName(newBgColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "bg.");
			if (_bg == null || !newBgEffect.endsWith(_bg.getClass().getCanonicalName()))
				_bg = (IColorableEffect) Class.forName(newBgEffect).getConstructor(IColor.class).newInstance(_bgColor); 
			if (_timeTextColor == null || !newTimetextColor.endsWith(_timeTextColor.getClass().getCanonicalName())) {
				_timeTextColor = (IColor) Class.forName(newTimetextColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "timetext.");
				_timeText = new TextEffect(_font, _timeTextColor, _timeText != null ? _timeText.getText() : "1337 ALTA!", 7, 0);
			}
			if (_pvTextColor == null || !newPvtextColor.endsWith(_pvTextColor.getClass().getCanonicalName())) {
				_pvTextColor = (IColor) Class.forName(newPvtextColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "pvtext.");
				_pvText = new TextEffect(_font, _pvTextColor, _pvText != null ? _pvText.getText() : "1337 ALTA!", 1, 8);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getPvText() {
		int pac = _pvData.getPac();
		double kwh = _pvData.getKwhDay();
		return String.format("%dW%s%.1f", pac, getSpaces(4-String.valueOf(pac).length() + 5-String.valueOf(kwh).length()) , (float)kwh);
	}

	private String getSpaces(int count) {
		String result = "";
		for (int i = 0; i < count; i++)
			result += " ";
		return result;
	}

}