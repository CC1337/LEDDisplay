package effects.text;

import net.PvData;
import led.ILEDArray;

import java.util.Calendar;

import effects.*;
import helper.Helper;

public class CurrentD0PacTextEffect implements IColorableEffect {
	
	private IPixelatedFont _font;
	private IColor _colorPositive;
	private IColor _colorNegative;
	private byte[][] _textArray;
	private int _posX;
	private int _posY;
	private PvData _pvData = PvData.getInstance();
	private int _lastD0PacUpdate;
	private int _currentD0Pac;
	
	public CurrentD0PacTextEffect(IPixelatedFont font, IColor colorPositive, IColor colorNegative, int posX, int posY) {
		_font = font;
		setPosX(posX);
		setPosY(posY);
		setColor(colorPositive);
		setColorNegative(colorNegative);
	}
	
	public CurrentD0PacTextEffect(IPixelatedFont font, IColor colorPositive, IColor colorNegative) {
		this(font, colorPositive, colorNegative, 0, 0);
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
		updateCurentD0Pac();
		updateTextArray();
		if (_currentD0Pac >= 0)
			_colorPositive.apply(leds, this);
		else
			_colorNegative.apply(leds, this);
	}
	
	private void updateCurentD0Pac() {
		int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
		if (_textArray != null && !(currentMinute % 5 == 0 && currentMinute != _lastD0PacUpdate))
			return;
		_currentD0Pac = _pvData.getCurrentD0Pac();
	}
	
	private void updateTextArray() {
		_textArray = _font.toPixels(getCurrentD0PacText());
	}
	
	private String getCurrentD0PacText() {
		String currentD0PacAbsoluteString = String.valueOf(Math.abs(_currentD0Pac));
		return Helper.getSpaces(4-currentD0PacAbsoluteString.length()) + currentD0PacAbsoluteString + "W";
	}

	@Override
	public IColor getColor() {
		return _colorPositive;
	}
	
	public IColor getColorNegative() {
		return _colorNegative;
	}

	@Override
	public void setColor(IColor colorPositive) {
		_colorPositive = colorPositive;
	}
	
	public void setColorNegative(IColor colorNegative) {
		_colorNegative = colorNegative;
	}

	@Override
	public byte[][] getEffectData() {
		return _textArray;
	}

}
