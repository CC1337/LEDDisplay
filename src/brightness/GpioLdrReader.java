package brightness;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;


public class GpioLdrReader implements IBrightnessSensorReader {

	final GpioController gpio = GpioFactory.getInstance();
	private GpioPinDigitalMultipurpose _ldrPin;
	private int _lastValue = 0;

	public GpioLdrReader(String pinNumber) {
		String pinName = "GPIO " + pinNumber;
		System.out.println("Using Pin " + pinName + " for LDR");
		_ldrPin = gpio.provisionDigitalMultipurposePin(RaspiPin.getPinByName(pinName), "LDR", PinMode.DIGITAL_OUTPUT);
	}
	
	@Override
	public int getLastBrightnessValue() {
		return _lastValue;
	}

	@Override
	public void updateBrightnessValue() {
		int value = measureCurrentValue();
		
		//System.out.print(value + " / ");
		
		if (value < 15)
			value *= 3;
		else
			value = value * 2 + 15;
		
		value = 100 - value;
		
		if (value < 10) value = 10;
		if (value > 100) value = 100;
		
		//System.out.println(value);

		_lastValue = value;
	}

	private int measureCurrentValue() {
		_ldrPin.setMode(PinMode.DIGITAL_OUTPUT);
		_ldrPin.setState(PinState.LOW);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		_ldrPin.setMode(PinMode.DIGITAL_INPUT);
		int count = 0;
		while (_ldrPin.isLow())
			count++;
	
		return count;
	}


}
