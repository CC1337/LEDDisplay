package output;

import effects.IEffect;
import led.*;

public interface IDisplayAdaptor {

	void show(ILEDArray leds);
	
	void addOverlay(IEffect overlayEffect);
	
	void addOverlay(IEffect overlayEffect, int overlayDuratrionMs);
	
	void closeSerialPort();
}
