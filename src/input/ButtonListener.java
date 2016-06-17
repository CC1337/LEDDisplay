package input;

import java.util.concurrent.Callable;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;

public class ButtonListener implements IButtonListener {

	final GpioController _gpio = GpioFactory.getInstance();
	final GpioPinDigitalInput _button;
	
	public ButtonListener(String pinNumber) {
		String pinName = "GPIO " + pinNumber;
		System.out.println("Using Pin " + pinName + " for ButtonListener");
        _button = _gpio.provisionDigitalInputPin(RaspiPin.getPinByName(pinName), PinPullResistance.PULL_UP);
        _button.setDebounce(100);
	}

	@Override
	public void setSingleTriggerCallback(Callable<Void> callback) {
		_button.addTrigger(new GpioCallbackTrigger(PinState.LOW, callback));
	}
}
