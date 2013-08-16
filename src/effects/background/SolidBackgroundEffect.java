package effects.background;

import effects.IEffect;
import led.ILEDArray;

public class SolidBackgroundEffect implements IEffect {
	
	int _r, _g, _b = 0;
	
	public void setColor(int r, int g, int b) {
		_r = r;
		_g = g;
		_b = b;
	}
	
	public void setR(int val) {
		_r = val;
	}
	
	public void setG(int val) {
		_g = val;
	}
	
	public void setB(int val) {
		_b = val;
	}
	
	public int getR() {
		return _r;
	}
	
	public int getG() {
		return _g;
	}
	
	public int getB() {
		return _b;
	}

	@Override
	public void apply(ILEDArray leds) {
		for(int x=0; x<leds.sizeX(); x++) {
			for(int y=0; y<leds.sizeY(); y++) {
				leds.setLed(x, y, _r, _g, _b);
			}
		}
	}
	
}
