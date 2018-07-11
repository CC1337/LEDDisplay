package effects.text;

import led.ILEDArray;
import effects.*;

public class CenteredTextEffect implements IColorableEffect {
	
	private IPixelatedFont _font;
	private IColor _color;
	private byte[][] _pixels;
	private int _posX;
	private int _posY;
	private int _width;
	private String _text;

	
	public CenteredTextEffect(IColor color, String text, int posX, int posY, int width) {
		_font = new PixelatedFont(new FontDefault7px());
		setPosX(posX);
		setPosY(posY);
		setWidth(width);
		setText(text);
		_color = color;
	}
	
	public CenteredTextEffect(IColor color, String text) {
		this(color, text, 0, 0, 60);
	}

	public void setText(String text) {
		_text = text;
		byte[][] textArray = _font.toPixels(text);
		_pixels = new byte[_width][textArray[0].length];
		for (int x=0; x<textArray.length; x++) {
			for (int y=0; y<textArray[0].length; y++) {
				int xPosition = x+(int)Math.floor(_width/2)-textArray.length/2;
				if (xPosition >= 0 && xPosition < _width)
					_pixels[xPosition][y] = textArray[x][y];
			}
		}
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
		return _pixels;
	}

}
