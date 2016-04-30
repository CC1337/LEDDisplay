package led;

import effects.IEffect;

public interface ILEDArray {

	int sizeX();
	
	int sizeY();
		
	ILED led(int x, int y);
	
	void setLed(int x, int y, int r, int g, int b);
	
	void reset();
	
	void applyEffect(IEffect effect);
	
}
