package effects.text;

import net.RssReader;
import led.ILEDArray;

import java.util.ArrayList;
import java.util.Calendar;

import configuration.IDisplayConfiguration;
import effects.*;

public class NewsMarqueeTextEffect implements IColorableEffect {
	
	private ArrayList<RssReader> _rssReaders = new ArrayList<RssReader>();
	private IColor _color;
	private int _newsPerRssFeed = 3;
	private int _newsScrollSpeed = 1;
	private String _rssUrls;
	private String _newsDelimiter;
	private MarqueeTextEffect _marqueeNewsText;
	private int _lastNewsTextUpdate = -1;
	
	public NewsMarqueeTextEffect(IPixelatedFont font, int posX, int posY, int width, IDisplayConfiguration config, String configPrefix) {
		loadConfig(config, configPrefix);
		initRssReaders();
		_marqueeNewsText = new MarqueeTextEffect(font, _color, "", posX, posY, width);
		updateNewsText();
	}
	
	@Override
	public void apply(ILEDArray leds) {
		updateNewsText();
		_marqueeNewsText.apply(leds);
	}
	
	/**
	 * shifts text by numPixels px, returns true at the end of each loop, false instead.
	 * Updates news text every end of loop if at least 5 minutes old
	 * @param numPixels
	 */
	public boolean shift(int numPixels) {
		boolean shiftEnded = _marqueeNewsText.shift(numPixels);
		if (shiftEnded)
			updateNewsText();
		return shiftEnded;
	}
	
	/**
	 * shifts text by configured num pixels, returns true at the end of each loop, false instead.
	 * Updates news text every end of loop if at least 5 minutes old
	 */
	public boolean shift() {
		return shift(_newsScrollSpeed);
	}
	
	private void loadConfig(IDisplayConfiguration config, String configPrefix) {
		_newsPerRssFeed = config.getInt(configPrefix + "newsPerRssFeed", 3);
		_newsDelimiter = config.getString(configPrefix + "newsDelimiter", " - ").replace("\"", "");
		_newsScrollSpeed = config.getInt(configPrefix + "newsScrollSpeed", 1);
		_rssUrls = config.getString(configPrefix + "rssUrls");
		String colorName = config.getString(configPrefix + "Coloring", "effects.coloring.ColoringSolid");
		try {
			_color = (IColor) Class.forName(colorName).getConstructor(IDisplayConfiguration.class, String.class).newInstance(config, configPrefix);
		} catch (Exception exception) {
			System.out.println("Configured NewsMarqueeTextEffect color '" + configPrefix + colorName + "' not found!" );
			System.out.println(exception);
		}
	}
	
	private void updateNewsText() {
		int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
		if (_lastNewsTextUpdate > 0 && !(currentMinute % 5 == 0 && currentMinute != _lastNewsTextUpdate))
			return;
		_marqueeNewsText.setText(getNews());
		_lastNewsTextUpdate = currentMinute;
	}
	
	private void initRssReaders() {
		_rssReaders.clear();
		
		if (_rssUrls != null && _rssUrls.length() > 10) {
			String[] urls = _rssUrls.split(" ");
			for (String url : urls) {
				_rssReaders.add(new RssReader(url));
			}
		}
	}
	
	private String getNews() {
		if (_rssReaders.size() == 0)
			return "No RSS Feeds configured :(";
		String result = "";
		for (RssReader reader : _rssReaders) {
			if (result != "")
				result += _newsDelimiter;
			result += reader.getLastMessages(_newsPerRssFeed, _newsDelimiter);
		}
		if (result.isEmpty())
			return "No News available :(";
		System.out.println(result);
		return result;
	}
	
	@Override
	public void setPosX(int x) {
		_marqueeNewsText.setPosX(x);
	}

	@Override
	public void setPosY(int y) {
		_marqueeNewsText.setPosY(y);
	}

	@Override
	public int getPosX() {
		return _marqueeNewsText.getPosX();
	}

	@Override
	public int getPosY() {
		return _marqueeNewsText.getPosY();
	}

	@Override
	public IColor getColor() {
		return _marqueeNewsText.getColor();
	}

	@Override
	public void setColor(IColor color) {
		_marqueeNewsText.setColor(color);
	}

	@Override
	public byte[][] getEffectData() {
		return _marqueeNewsText.getEffectData();
	}

}
