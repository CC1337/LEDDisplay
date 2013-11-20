package effects.shape;

import led.ILEDArray;
import effects.ColorableEffectBase;
import effects.IColor;

public class RectEffect extends ColorableEffectBase {

	private int _width;
	private int _height;
	
	public RectEffect(int posX, int posY, int width, int height, IColor color) {
		_posX = posX;
		_posY = posY;
		_width = width;
		_height = height;
		_color = color;
		reCalc();
	}
	
	@Override
	public void apply(ILEDArray leds) {
		_color.apply(leds, this);
	}
	
	public void setWidth(int width) {
		_width = width;
		reCalc();
	}
	
	public void SetHeight(int height) {
		_height = height;
		reCalc();
	}
	
	private void reCalc() {
		_data = new byte[_width][_height];
		for (int x=0; x<_width; x++) {
			for (int y=0; y<_height; y++) {
				_data[x][y] = 1;
			}
		}
	}
			
}
