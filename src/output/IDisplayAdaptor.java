package output;

import effects.IColorableEffect;
import led.*;

public interface IDisplayAdaptor {

	void show(ILEDArray leds);
	
	void addOverlay(IColorableEffect overlayEffect);
	
	void addOverlay(IColorableEffect overlayEffect, int overlayDuratrionMs);
	
	void closeSerialPort();
}
