package effects.text;

import led.ILEDArray;
import effects.*;
import effects.background.SolidBackgroundEffect;
import effects.coloring.ColoringSolid;

public class OverlayTextEffect implements IEffect {
	
	private IColor _color;
	private int _posX;
	private int _posY;
	private int _width;
	private String _textLine1;
	private String _textLine2;
	private IColor _bgColor = new ColoringSolid(0, 0, 0, 0.6);
	private IEffect _background = new SolidBackgroundEffect(_bgColor);

	
	public OverlayTextEffect(IColor color, String textLine1, String textLine2, int posX, int posY, int width) {
		setPosX(posX);
		setPosY(posY);
		setWidth(width);
		setTextLine1(textLine1);
		setTextLine2(textLine2);
		_color = color;
	}
	
	public OverlayTextEffect(IColor color, String textLine1, String textLine2) {
		this(color, textLine1, textLine2, 0, 0, 60);
	}
	
	public OverlayTextEffect(String textLine1, String textLine2) {
		this(new ColoringSolid(255, 255, 255), textLine1, textLine2, 0, 0, 60);
	}
	
	public OverlayTextEffect(String textLine1) {
		this(new ColoringSolid(255, 255, 255), textLine1, null, 0, 0, 60);
	}
	
	public void setTextLine1(String textLine1) {
		_textLine1 = textLine1;
	}
	
	public void setTextLine2(String textLine2) {
		_textLine2 = textLine2;
	}

	public String getTextLine1() {
		return _textLine1;
	}
	
	public String getTextLine2() {
		return _textLine2;
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
		leds.applyEffect(_background);
		if (_textLine2 != null && _textLine2.length() > 0 && _textLine1 != null && _textLine1.length() > 0) {
			_color.apply(leds, new CenteredTextEffect(_color, _textLine1, _posX, _posY, _width));
			_color.apply(leds, new CenteredTextEffect(_color, _textLine2, _posX, _posY+8, _width));
		} else if(_textLine1 != null && _textLine1.length() > 0) {
			_color.apply(leds, new CenteredTextEffect(_color, _textLine1, _posX, _posY+4, _width));
		}
	}

	public IColor getColor() {
		return _color;
	}

	public void setColor(IColor color) {
		_color = color;
	}
}
