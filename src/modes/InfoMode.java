package modes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.PvData;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;

import effects.IColor;
import effects.IColorableEffect;
import effects.info.PvDayChartEffect;
import effects.text.*;
import output.IDisplayAdaptor;
import led.ILEDArray;

public class InfoMode implements IMode {

	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private IModeSelector _modeSelector;
	private boolean _aborted = false;
	private boolean _end = false;
	private IDisplayConfiguration _config;
	private PvData _pvData = PvData.getInstance();
	
	private IColor _bgColor = null;
	private IColor _timeTextColor = null;
	private IColor _pvTextColor = null;
	private IColor _infoTextColor = null;
	private IColorableEffect _bg = null;
	private IPixelatedFont _font = new PixelatedFont(new FontDefault7px());
	TextEffect _timeText = null;
	TextEffect _pvText = null;
	InfoTextEffect _infoText = null;
	PvDayChartEffect _pvDayChart = null;
	int _infoChangeDelay = 5;
	
	
	public InfoMode(IDisplayAdaptor display, ILEDArray leds, IModeSelector modeSelector) {
		_display = display;
		_leds = leds;
		_modeSelector = modeSelector;
		_config = new DisplayConfiguration("infomode.properties", true);
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
		int currentSecond;
		
		int lastInfoUpdate = 1337;
		int lastPvUpdate = 1337;
		_pvText.setText(getPvText());
		
		while (!_aborted && !_end) {
			reloadConfig();
			
			currentTime = new SimpleDateFormat("H:mm").format(new Date());
			calendar = Calendar.getInstance();
			currentMinute = calendar.get(Calendar.MINUTE);
			currentSecond = calendar.get(Calendar.SECOND);

			_timeText.setText(currentTime);
			
			if (currentMinute != lastPvUpdate) {
				_pvText.setText(getPvText());
				_pvDayChart.updateData();
				lastPvUpdate = currentMinute;
			}
			
			if (lastInfoUpdate != currentSecond && currentSecond % _infoChangeDelay == 0) {
				lastInfoUpdate = currentSecond;
				_infoText.nextInfo();
			}
			
			_leds.applyEffect(_bg);
			_leds.applyEffect(_timeText);
			_leds.applyEffect(_pvText);
			_leds.applyEffect(_infoText);
		
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
			_infoChangeDelay = _config.getInt("infoChangeDelay", 5);
						
			String newBgColor = _config.getString("bg.Coloring", "effects.coloring.ColoringSolid");
			String newBgEffect = _config.getString("bg.Effect", "effects.background.SolidBackgroundEffect");
			String newTimetextColor = _config.getString("timetext.Coloring", "effects.coloring.ColoringSolid");
			String newPvtextColor = _config.getString("pvtext.Coloring", "effects.coloring.ColoringSolid");
			String newInfotextColor = _config.getString("infotext.Coloring", "effects.coloring.ColoringSolid");

			if (_bgColor == null || !newBgColor.endsWith(_bgColor.getClass().getCanonicalName()))
				_bgColor = (IColor) Class.forName(newBgColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "bg.");
				_bg = (IColorableEffect) Class.forName(newBgEffect).getConstructor(IColor.class).newInstance(_bgColor);
			if (_bg == null || !newBgEffect.endsWith(_bg.getClass().getCanonicalName()))
				_bg = (IColorableEffect) Class.forName(newBgEffect).getConstructor(IColor.class).newInstance(_bgColor); 
			if (_timeTextColor == null || !newTimetextColor.endsWith(_timeTextColor.getClass().getCanonicalName())) {
				_timeTextColor = (IColor) Class.forName(newTimetextColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "timetext.");
				_timeText = new TextEffect(_font, _timeTextColor, _timeText != null ? _timeText.getText() : "1337 ALTA!", 1, 0);
			}
			if (_pvTextColor == null || !newPvtextColor.endsWith(_pvTextColor.getClass().getCanonicalName())) {
				_pvTextColor = (IColor) Class.forName(newPvtextColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "pvtext.");
				_pvText = new TextEffect(_font, _pvTextColor, _pvText != null ? _pvText.getText() : "1337 ALTA!", 31, 0);
				_pvDayChart = new PvDayChartEffect(0, 8, 36, 7, _pvTextColor);
			}
			if (_infoTextColor == null || !newInfotextColor.endsWith(_infoTextColor.getClass().getCanonicalName())) {
				_infoTextColor = (IColor) Class.forName(newInfotextColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "infotext.");
				_infoText = new InfoTextEffect(_font, _infoTextColor, 1, 8);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getPvText() {
		int pac = _pvData.getPac();
		double kwh = _pvData.getKwhDay();
		if (pac > 0)
			return getSpaces(4-String.valueOf(pac).length())+pac+"W";
		else 
			return String.format("%s%.1f", getSpaces(5-String.valueOf(kwh).length()) , (float)kwh);
	}

	private String getSpaces(int count) {
		String result = "";
		for (int i = 0; i < count; i++)
			result += " ";
		return result;
	}

}
