package effects.coloring;

import led.ILEDArray;
import effects.*;

public class ColoringSolid implements IColor {
	
	private int _r;
	private int _g;
	private int _b;
	
	public ColoringSolid(int r, int g, int b) {
		setColor(r, g, b);
	}
	
	public ColoringSolid() {
		setColor(0, 0, 0);
	}

	@Override
	public void apply(ILEDArray leds, IColorableEffect effect) {
		byte[][] effectData = effect.getEffectData();
		for(int x=0; x<effectData.length; x++) {
			for(int y=0; y<effectData[0].length; y++) {
				if (effectData[x][y] == 1)
					leds.setLed(x+effect.getPosX(), y+effect.getPosY(), _r, _g, _b);
			}
		}		
	}
	
	public void setColor(int r, int g, int b) {
		_r = r;
		_g = g;
		_b = b;
	}
	
	public void setR(int r) {
		_r = r;
	}
	
	public void setG(int g) {
		_g = g;
	}
	
	public void setB(int b) {
		_b = b;
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

}
