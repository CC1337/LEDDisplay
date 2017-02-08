package input;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;

import helper.Helper;

public class ButtonListener implements IButtonListener {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	final GpioController _gpio = Helper.isWindows() ? null : GpioFactory.getInstance();
	final GpioPinDigitalInput _button;
	
	public ButtonListener(String pinNumber) {
		String pinName = "GPIO " + pinNumber;
		
		if (_gpio == null) {
			System.out.println("NOT using Pin " + pinName + " for ButtonListener because running on Windows w/o pi4j");
			_button = null;
		} else {
			System.out.println("Using Pin " + pinName + " for ButtonListener");
	        _button = _gpio.provisionDigitalInputPin(RaspiPin.getPinByName(pinName), PinPullResistance.PULL_UP);
	        _button.setDebounce(100);
		}
	}

	@Override
	public void setSingleTriggerCallback(Callable<Void> callback) {
		if (_button == null)
			return;
		_button.addTrigger(new GpioCallbackTrigger(PinState.LOW, callback));
	}
}
