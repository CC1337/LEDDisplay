package led;

import brightness.BrightnessCorrection;
import brightness.IBrightnessCorrection;

public class LED implements ILED{

	private int r = 0;
	private int g = 0;
	private int b = 0;
	private int rPrev = 0;
	private int gPrev = 0;
	private int bPrev = 0;
	private int rWithBrightnessCorrection = 0;
	private int gWithBrightnessCorrection = 0;
	private int bWithBrightnessCorrection = 0;
	private IBrightnessCorrection brightnessCorrection = BrightnessCorrection.getInstance();
	private int brightnessPercentage = -1;
	
	// Get
	public int r() {
		return r;
	}
	
	public int g() {
		return g;
	}
	
	public int b() {
		return b;
	}
	
	// Get with brightness correction
	public int rWithBrightnessCorrection() {
		updateBrightnessCorrection();
		return rWithBrightnessCorrection;
	}
	
	public int gWithBrightnessCorrection() {
		updateBrightnessCorrection();
		return gWithBrightnessCorrection;
	}
	
	public int bWithBrightnessCorrection() {
		updateBrightnessCorrection();
		return bWithBrightnessCorrection;
	}
	
	private void updateBrightnessCorrection() {
		if (brightnessPercentage != brightnessCorrection.getBrightnessPercentage()) {
			brightnessPercentage = brightnessCorrection.getBrightnessPercentage();
			
			updateRBrightnessCorrection();
			updateGBrightnessCorrection();
			updateBBrightnessCorrection();
			return;
		}
		
		if (r != rPrev)
			updateRBrightnessCorrection();
		
		if (g != gPrev)
			updateGBrightnessCorrection();
		
		if (b != bPrev)
			updateBBrightnessCorrection();
	}
	
	private void updateRBrightnessCorrection() {
		rPrev = r;
		rWithBrightnessCorrection = getBrightnessCorrectedValue(r, brightnessPercentage);
	}
	
	private void updateGBrightnessCorrection() {
		gPrev = g;
		gWithBrightnessCorrection = getBrightnessCorrectedValue(g, brightnessPercentage);
	}
	
	private void updateBBrightnessCorrection() {
		bPrev = b;
		bWithBrightnessCorrection = getBrightnessCorrectedValue(b, brightnessPercentage);
	}
	
	private int getBrightnessCorrectedValue(int value, int brightnessPercentage) {
		int result = (int) (((double)value * (double)brightnessPercentage)/100.0);
		if (value > 0 && result == 0)
			result = 1;
		return result;
	}
	
	// Set
	public void r(int r) {
		if (isValidValue(r)) {
			this.r = r;	
		}
	}
	
	public void g(int g) {
		if (isValidValue(g)) {
			this.g = g;	
		}
	}
	
	public void b(int b) {
		if (isValidValue(b)) {
			this.b = b;		
		}
	}
	
	public void w(int w) {
		r(w);
		g(w);
		b(w);
	}
	
	public void off() {
		w(0);
	}
	
	// Validation
	private boolean isValidValue(int val) {
		return (val >= 0 && val <= 255); 
	}
	
}
