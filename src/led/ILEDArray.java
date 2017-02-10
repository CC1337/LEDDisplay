package led;

import effects.IEffect;

public interface ILEDArray {

	int sizeX();
	
	int sizeY();
		
	ILED led(int x, int y);
	
	void setLed(int x, int y, int r, int g, int b);
	
	void blendLed(int x, int y, int r, int g, int b, double alpha);
	
	void reset();
	
	void applyEffect(IEffect effect);
	
}
