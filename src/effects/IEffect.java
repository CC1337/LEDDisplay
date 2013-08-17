package effects;

import led.ILEDArray;

public interface IEffect {

	public void apply(ILEDArray leds);
	
	public void setPosX(int x);
	
	public void setPosY(int y);
	
	public int getPosX();
	
	public int getPosY();
}
