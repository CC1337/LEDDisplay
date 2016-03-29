package brightness;

import java.util.Observable;
import java.util.Observer;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import configuration.DisplayConfiguration;

public class BrightnessCorrection implements IBrightnessCorrection, Observer {
	
	private static IBrightnessCorrection _instance;
	private DisplayConfiguration _config;
	private int _currentBrightness;
	private int _newBrightness;
	final GpioController gpio = GpioFactory.getInstance();
	
	private BrightnessCorrection () {
		_config = new DisplayConfiguration("brightness.properties", true);
		_config.addObserver(this);
		_currentBrightness = _config.getInt("brightness", 100);
		_newBrightness = _currentBrightness;
		System.out.println("Initial brightness: " + _currentBrightness);
	}

	public static IBrightnessCorrection getInstance() {
		if (_instance == null) {
			_instance = new BrightnessCorrection();
		}
		return BrightnessCorrection._instance;
	  }

	public int getBrightnessPercentage() {
		if (_currentBrightness != 0)
			return _currentBrightness;
		return getAutoBrightness();
	}
	
	private int getAutoBrightness() {
		// TODO
//		gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "LDR", PinState.LOW);
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		GpioPinDigitalInput ldr = gpio.provisionDigitalInputPin(RaspiPin.GPIO_07, "LDR");
//		int count = 0;
//		while (ldr.isLow())
//			count++;
//		
//		System.out.println(count);
		return 0;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		System.out.println("Brightness config updated");
		reloadConfig();
	}
	
	private void reloadConfig() {
		_newBrightness = _config.getInt("brightness", 100);
		if (_currentBrightness != _newBrightness)
			System.out.println("New brightness: " + _newBrightness);
	}

	public void doDimmingStep() {
		if (_newBrightness == _currentBrightness)
			return;
		
		if (_newBrightness > _currentBrightness)
			_currentBrightness++;
		else
			_currentBrightness--;
	}

}
