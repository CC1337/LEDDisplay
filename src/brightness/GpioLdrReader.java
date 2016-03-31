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
		int time = measureCurrentTime();
		
		time = time / 900000;
		
		if (time > 100)
			time = 100;
		
		time = 100 - time;	
		time = time * time;
		time = time / 100;
		
		//System.out.println(time);

		_lastValue = time;
	}

	private int measureCurrentTime() {
		long time = 40;
		try {
			_ldrPin.setMode(PinMode.DIGITAL_OUTPUT);
			_ldrPin.setState(PinState.LOW);
			// Load capacitor
			Thread.sleep(100);
			
			_ldrPin.setMode(PinMode.DIGITAL_INPUT);
			
			time = System.nanoTime();
			
			while (_ldrPin.isLow())
				Thread.sleep(1);
			
			time = System.nanoTime() - time;		
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return (int) time;
	}


}
