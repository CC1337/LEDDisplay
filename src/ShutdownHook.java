import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import helper.Helper;
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
		shutdownSerial();
		if (!Helper.isWindows())
			shutdownGpio();
	}
	
	private void shutdownSerial() {
		for (int x=0; x<_leds.sizeX(); x++) {
			for (int y=0; y<_leds.sizeY(); y++) {
				_leds.setLed(x, y, 0, 0, 1);
			}
		}
		_display.show(_leds);
		_serial.closeSerialPort();
	}
	
	private void shutdownGpio() {
		final GpioController gpio = GpioFactory.getInstance();
		gpio.shutdown();
	}
}
