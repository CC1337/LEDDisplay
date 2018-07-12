package output;

import led.*;

public interface IDisplayAdaptor extends IOverlay{

	void show(ILEDArray leds);
	
	void closeSerialPort();
}
