import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import helper.Helper;
import led.ILEDArray;
import modeselection.IModeSelector;
import output.IDisplayAdaptor;
import output.ISerial;


public class ShutdownHook extends Thread {
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private ISerial _serial;
	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private IModeSelector _modeSelector;
	
	public ShutdownHook(ISerial serial, IDisplayAdaptor display, ILEDArray leds, IModeSelector modeSelector) {
		_serial = serial;
		_display = display;
		_leds = leds;
		_modeSelector = modeSelector;
	}

	@Override
	public void run() {
		LOGGER.info("Shutting down...");
		shutDownModeSelector();
		shutdownSerial();
		if (!Helper.isWindows())
			shutdownGpio();
		LOGGER.info("Shutdown complete.");
	}
	
	private void shutDownModeSelector() {
		_modeSelector.shutdown();
		Helper.waitms(100);
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
