package effects.text;

import led.ILEDArray;
import effects.*;

public class TextEffect implements IColorableEffect {
	
	private IPixelatedFont _font;
	private IColor _color;
	private byte[][] _textArray;
	private int _posX;
	private int _posY;
	private String _text;
	
	public TextEffect(IPixelatedFont font, IColor color, String text, int posX, int posY) {
		_font = font;
		setPosX(posX);
		setPosY(posY);
		setText(text);
		_color = color;
	}
	
	public TextEffect(IPixelatedFont font, IColor color, String text) {
		this(font, color, text, 0, 0);
	}
	
	public void setText(String text) {
		_text = text;
		_textArray = _font.toPixels(text);
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
