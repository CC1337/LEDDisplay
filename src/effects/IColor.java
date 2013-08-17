package effects;

import led.ILEDArray;

public interface IColor {
	
	public void apply(ILEDArray leds, IColorableEffect effect);
	
}
