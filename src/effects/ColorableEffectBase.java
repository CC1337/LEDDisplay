package effects;

import led.ILEDArray;

public abstract class ColorableEffectBase implements IColorableEffect{

	protected int _posX = 0;
	protected int _posY = 0;
	protected IColor _color;
	protected byte[][] _data;	

	public abstract void apply(ILEDArray leds);
	
	@Override
	public void setPosX(int x) {
		_posX = x;
	}

	@Override
	public void setPosY(int y) {
		_posY = y;
	}

	@Override
	public int getPosX() {
		return _posX;
	}

	@Override
	public int getPosY() {
		return _posY;
	}

	@Override
	public IColor getColor() {
		return _color;
	}

	@Override
	public void setColor(IColor color) {
		_color = color;
	}
	
	public byte[][] getEffectData() {
		return _data;
	}
}
