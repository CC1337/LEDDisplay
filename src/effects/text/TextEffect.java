package effects.text;

import effects.IEffect;
import led.ILEDArray;

public class TextEffect implements IEffect {
	
	private IPixelatedFont _font;
	private byte[][] _textArray;
	private int _posX;
	private int _posY;
	private int _r;
	private int _g;
	private int _b;
	
	public TextEffect(IPixelatedFont font, String text, int posX, int posY, int r, int g, int b) {
		_font = font;
		setPosX(posX);
		setPosY(posY);
		setR(r);
		_g = g;
		_b = b;
		setText(text);
	}
	
	public void setText(String text) {
		_textArray = _font.toPixels(text);
	}
	
	
	@Override
	public void apply(ILEDArray leds) {
		for(int x=0; x<_textArray.length; x++) {
			for(int y=0; y<_textArray[0].length; y++) {
				if (_textArray[x][y] == 1)
					leds.setLed(x+_posX, y+_posY, _r, _g, _b);
			}
		}		
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

	public int getR() {
		return _r;
	}

	public void setR(int _r) {
		this._r = _r;
	}
	
	public int getG() {
		return _g;
	}

	public void setG(int _g) {
		this._g = _g;
	}
	
	public int getB() {
		return _b;
	}

	public void setB(int _b) {
		this._b = _b;
	}

}
