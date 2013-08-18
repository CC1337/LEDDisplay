import led.ILEDArray;
import output.IDisplayAdaptor;
import output.ISerial;


public class ShutdownHook extends Thread {
	
	private ISerial _serial;
	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	
	public ShutdownHook(ISerial serial, IDisplayAdaptor display, ILEDArray leds) {
		_serial = serial;
		_display = display;
		_leds = leds;
	}

	@Override
	public void run() {
		for (int x=0; x<_leds.sizeX(); x++) {
			for (int y=0; y<_leds.sizeY(); y++) {
				_leds.setLed(x, y, 0, 0, 1);
			}
		}
		_display.show(_leds);
		_serial.closeSerialPort();
	}
}
