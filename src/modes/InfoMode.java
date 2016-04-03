package modes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import net.PvData;
import net.RssReader;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;

import effects.IColor;
import effects.IColorableEffect;
import effects.info.PvDayChartEffect;
import effects.shape.RectEffect;
import effects.text.*;
import helper.FpsController;
import output.IDisplayAdaptor;
import led.ILEDArray;


public class InfoMode implements IMode {

	private enum TextType {
		INFOTEXT,
		NEWSTEXT
	}
	
	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private IModeSelector _modeSelector;
	private boolean _aborted = false;
	private boolean _end = false;
	private IDisplayConfiguration _config;
	private PvData _pvData = PvData.getInstance();
	private ArrayList<RssReader> _rssReaders = new ArrayList<RssReader>();
	private FpsController _fpsController = FpsController.getInstance();
	
	private IColor _bgColor = null;
	private IColor _timeTextColor = null;
	private IColor _pvTextColor = null;
	private IColor _infoTextColor = null;
	private IColor _newsTextColor = null;
	private IColor _secondPixelColor = null;
	private IColorableEffect _bg = null;
	private IPixelatedFont _font = new PixelatedFont(new FontDefault7px());
	TextEffect _timeText = null;
	TextEffect _pvText = null;
	InfoTextEffect _infoText = null;
	RectEffect _secondPixel = null;
	PvDayChartEffect _pvDayChart = null;
	MarqueeTextEffect _newsText = null;
	int _infoChangeDelay = 5;
	int _newsEnabled = 1;
	int _newsPerRssFeed = 3;
	int _newsScrollSpeed = 1;
	String _newsDelimiter;
	boolean _showSecondPixel = true;
	
	
	public InfoMode(IDisplayAdaptor display, ILEDArray leds, IModeSelector modeSelector) {
		_display = display;
		_leds = leds;
		_modeSelector = modeSelector;
		_config = new DisplayConfiguration(modeName().toLowerCase() + ".properties", true);
	}
	
	@Override
	public String modeName() {
		return this.getClass().getName();
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
		int currentMinute;
		int currentSecond;
		TextType currentText = TextType.INFOTEXT;
		
		int lastInfoUpdate = 1337;
		int lastPvUpdate = 1337;
		int lastNewsUpdate = 1337;
		_pvText.setText(getPvText());
		if (_newsEnabled == 1)
			_newsText.setText(getNews());

		while (!_aborted && !_end) {

			//reloadConfig();

			currentTime = new SimpleDateFormat("H:mm").format(new Date());
			calendar = Calendar.getInstance();
			currentMinute = calendar.get(Calendar.MINUTE);
			currentSecond = calendar.get(Calendar.SECOND);

			_timeText.setText(currentTime);
			
			if (currentMinute % 5 == 0 && currentMinute != lastPvUpdate) {
				_pvText.setText(getPvText());
				lastPvUpdate = currentMinute;
			}

			_leds.applyEffect(_bg);
			_leds.applyEffect(_timeText);
			_leds.applyEffect(_pvText);
			
			if (currentText == TextType.INFOTEXT) {
				if (lastInfoUpdate != currentSecond && currentSecond % _infoChangeDelay == 0) {
					lastInfoUpdate = currentSecond;
					_infoText.nextInfo();
				}
				if (_infoText.loopEnded()) {
					currentText = TextType.NEWSTEXT;
					_infoText.toStart();
				}
				if (_newsEnabled == 1 && currentMinute % 5 == 0 && currentMinute != lastNewsUpdate) {
					_newsText.setText(getNews());
					lastNewsUpdate = currentMinute;
				}
			}
			_leds.applyEffect(_infoText);

			if (currentText == TextType.NEWSTEXT) {
				if (_newsEnabled == 1) {
					_leds.applyEffect(_newsText);
					if (_newsText.shift(_newsScrollSpeed))
						currentText = TextType.INFOTEXT;
				} else {
					currentText = TextType.INFOTEXT;
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
		try {
			_infoChangeDelay = _config.getInt("infoChangeDelay", 5);
			_newsEnabled = _config.getInt("newsEnabled", 1);
			_newsPerRssFeed = _config.getInt("newsPerRssFeed", 3);
			_newsDelimiter = _config.getString("newsDelimiter", " - ").replace("\"", "");
			_showSecondPixel = _config.getInt("showSecond", 1) == 1;
			_newsScrollSpeed = _config.getInt("newsScrollSpeed", 1);
			initRss();
			System.out.println("cfg reload");
			String newBgColor = _config.getString("bg.Coloring", "effects.coloring.ColoringSolid");
			String newBgEffect = _config.getString("bg.Effect", "effects.background.SolidBackgroundEffect");
			String newTimetextColor = _config.getString("timetext.Coloring", "effects.coloring.ColoringSolid");
			String newPvtextColor = _config.getString("pvtext.Coloring", "effects.coloring.ColoringSolid");
			String newInfotextColor = _config.getString("infotext.Coloring", "effects.coloring.ColoringSolid");
			String newSecondPixelColor = _config.getString("second.Coloring", "effects.coloring.ColoringSolid");
			String newNewstextColor = _config.getString("newstext.Coloring", "effects.coloring.ColoringSolid");

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
			if (_secondPixelColor == null || !newSecondPixelColor.endsWith(_secondPixelColor.getClass().getCanonicalName())) {
				_secondPixelColor = (IColor) Class.forName(newSecondPixelColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "second.");
				_secondPixel = new RectEffect(0, 7, 1, 1, _secondPixelColor);
			}
			if (_newsEnabled == 1 && (_newsTextColor == null || !newInfotextColor.endsWith(_newsTextColor.getClass().getCanonicalName()))) {
				_newsTextColor = (IColor) Class.forName(newNewstextColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "newstext.");
				_newsText = new MarqueeTextEffect(_font, _newsTextColor, getNews(), 1, 8, _leds.sizeX());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initRss() {
		_rssReaders.clear();
		String rssUrls = _config.getString("rssUrls");
		if (rssUrls != null && rssUrls.length() > 10) {
			String[] urls = rssUrls.split(" ");
			for (String url : urls) {
				_rssReaders.add(new RssReader(url));
			}
		}
	}
	
	private String getNews() {
		if (_newsEnabled == 0)
			return "";
		if (_rssReaders.size() == 0)
			return "No RSS Feeds configured :(";
		String result = "";
		for (RssReader reader : _rssReaders) {
			if (result != "")
				result += _newsDelimiter;
			result += reader.getLastMessages(3, _newsDelimiter);
		}
		if (result.isEmpty())
			return "No News available :(";
		System.out.println(result);
		return result;
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
