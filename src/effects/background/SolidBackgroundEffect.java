package effects.background;

import effects.*;
import led.ILEDArray;

public class SolidBackgroundEffect implements IColorableEffect {
	
	IColor _color;
	byte[][] _data;
	
	public SolidBackgroundEffect(IColor color) {
		_color = color;
	}
	
	@Override
	public void setColor(IColor color) {
		_color = color;
	}
	
	@Override
	public IColor getColor() {
		return _color;
	}
	
	@Override
	public void apply(ILEDArray leds) {
		_data = new byte[leds.sizeX()][leds.sizeY()];
		for(int x=0; x<leds.sizeX(); x++) {
			for(int y=0; y<leds.sizeY(); y++) {
				_data[x][y] = 1;
			}
		}
		_color.apply(leds, this);
	}

	@Override
	public void setPosX(int x) {
		
	}

	@Override
	public void setPosY(int y) {
		
	}

	@Override
	public int getPosX() {

		return 0;
	}

	@Override
	public int getPosY() {

		return 0;
	}

	@Override
	public byte[][] getEffectData() {
		return _data;
	}

	
}
