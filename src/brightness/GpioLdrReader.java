package brightness;


import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import helper.Helper;


public class GpioLdrReader implements IBrightnessSensorReader {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	final GpioController _gpio = GpioFactory.getInstance();
	private GpioPinDigitalMultipurpose _ldrPin;
	private int _lastValue = 0;
	private int _maxCapacitorUnloadNs;

	public GpioLdrReader(String pinNumber, int maxCapacitorUnloadNs) {
		String pinName = "GPIO " + pinNumber;
		System.out.println("Using Pin " + pinName + " for LDR");
		_ldrPin = _gpio.provisionDigitalMultipurposePin(RaspiPin.getPinByName(pinName), "LDR", PinMode.DIGITAL_OUTPUT);
		_maxCapacitorUnloadNs = maxCapacitorUnloadNs;
	}
	
	@Override
	public int getLastBrightnessValue() {
		return _lastValue;
	}

	@Override
	public void updateBrightnessValue() {
		int time = measureCurrentTime();
		
		time = time / _maxCapacitorUnloadNs;
		
		if (time > 100)
			time = 100;
		
		time = 100 - time;	
		time = time * time;
		time = time / 100;
		
		//System.out.println(time);

		_lastValue = time;
	}

	private int measureCurrentTime() {
		_ldrPin.setMode(PinMode.DIGITAL_OUTPUT);
		_ldrPin.setState(PinState.LOW);
		
		// Load capacitor
		Helper.waitms(100);
		
		_ldrPin.setMode(PinMode.DIGITAL_INPUT);
		
		long time = System.nanoTime();
		
		while (_ldrPin.isLow())
			Helper.waitms(1);
		
		time = System.nanoTime() - time;		

		return (int) time;
	}


}
