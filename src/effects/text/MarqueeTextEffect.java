package effects.text;

import led.ILEDArray;
import effects.*;

public class MarqueeTextEffect implements IColorableEffect {
	
	private IPixelatedFont _font;
	private IColor _color;
	private byte[][] _fullTextArray;
	private byte[][] _textArray;
	private int _posX;
	private int _posY;
	private int _width;
	private String _text;
	private int _scrollPosition = 0;
	
	public MarqueeTextEffect(IPixelatedFont font, IColor color, String text, int posX, int posY, int width) {
		_font = font;
		setPosX(posX);
		setPosY(posY);
		setWidth(width);
		setText(text);
		_color = color;
	}
	
	public MarqueeTextEffect(IPixelatedFont font, IColor color, String text) {
		this(font, color, text, 0, 0, 60);
	}
	
	/**
	 * shifts text by numPixels px, returns true at the end of each loop, false instead.
	 * @param numPixels
	 */
	public boolean shift(int numPixels) {
		for (int x=0; x<_textArray.length; x++) {
			for (int y=0; y<_textArray[0].length; y++) {
				if (x+_scrollPosition >= 0 && x+_scrollPosition < _fullTextArray.length)
					_textArray[x][y] = _fullTextArray[x+_scrollPosition][y];
				else
					_textArray[x][y] = 0;
			}
		}
		_scrollPosition = _scrollPosition + numPixels;
		if (_scrollPosition > _fullTextArray.length) {
			_scrollPosition = -_width;
			return true;
		}
		return false;
	}
	
	public void setText(String text) {
		_text = text;
		_fullTextArray = _font.toPixels(text);
		_textArray = new byte[_width][_fullTextArray[0].length];
		_scrollPosition = -_width;
	}
	
	public String getText() {
		return _text;
	}
	
	public int getPosX() {
		return _posX;
	}

	public void setPosX(int _posX) {
		this._posX = _posX;
	}

	public int getPosY() {
		return _posY;
	}

	public void setPosY(int _posY) {
		this._posY = _posY;
	}
	
	public int getWidth() {
		return _width;
	}

	public void setWidth(int _width) {
		this._width = _width;
	}

	@Override
	public void apply(ILEDArray leds) {
		_color.apply(leds, this);
	}

	@Override
	public IColor getColor() {
		return _color;
	}

	@Override
	public void setColor(IColor color) {
		_color = color;
	}

	@Override
	public byte[][] getEffectData() {
		return _textArray;
	}

}
