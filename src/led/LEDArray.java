package led;

import effects.IEffect;

public class LEDArray implements ILEDArray{

	private LED[][] ledArray;
	private int sizeX;
	private int sizeY;
	
	public LEDArray(int x, int y) {
		sizeX = x;
		sizeY = y;
		initArray();
	}
	
	private void initArray() {
		ledArray = new LED[sizeX][sizeY];
		for(int x=0; x<sizeX; x++) {
			for(int y=0; y<sizeY; y++) {
				ledArray[x][y] = new LED();
			}
		}
	}
	
	public int sizeX() {
		return sizeX;
	}
	
	public int sizeY() {
		return sizeY;
	}
		
	public LED led(int x, int y) {
		if (x >= sizeX || y >= sizeY || x < 0 || y < 0) {
			return null;
		}
		return ledArray[x][y];
	}
	
	public void setLed(int x, int y, int r, int g, int b) {
		if (x >= sizeX || y >= sizeY || x < 0 || y < 0) {
			return;
		}
		ledArray[x][y].r(r);
		ledArray[x][y].g(g);
		ledArray[x][y].b(b);
	}
	
	public void reset() {
		for(int x=0; x<sizeX; x++) {
			for(int y=0; y<sizeY; y++) {
				ledArray[x][y].off();
			}
		}
	}

	public void applyEffect(IEffect effect) {
		effect.apply(this);
	}
				
}
