package input;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;
import helper.Helper;

public class ButtonListener implements IButtonListener {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private IDisplayConfiguration _config = new DisplayConfiguration("global.properties", false);
	final GpioController _gpio = Helper.isWindows() ? null : GpioFactory.getInstance();
	final GpioPinDigitalInput _button;
	final int SINGLE_PRESS_DURATION_MIN = _config.getInt("input.ButtonListener.SinglePressDuration", 70);
	final int LONG_PRESS_DURATION_MIN = _config.getInt("input.ButtonListener.LongPressDuration", 1000);
	final int DEBOUNCE_MS = _config.getInt("input.ButtonListener.DebounceMs", 50);
	Callable<Void> _singleTriggerCallback;
	Callable<Void> _longTriggerCallback;
	long _lastButtonPressStart = 0;
	long _lastButtonPressEnd = 0;
	PinState _buttonPressed = PinState.LOW;
	PinState _buttonReleased = PinState.HIGH;

	public ButtonListener(String pinNumber) {
		this(pinNumber, false);
	}

	public ButtonListener(String pinNumber, boolean buttonStateIsHighWhenPressed) {
		String pinName = "GPIO " + pinNumber;
		
		if (_gpio == null) {
			LOGGER.info("NOT using Pin " + pinName + " for ButtonListener because running on Windows w/o pi4j");
			_button = null;
		} else {
			if (buttonStateIsHighWhenPressed) {
				_buttonPressed = PinState.HIGH;
				_buttonReleased = PinState.LOW;
			}
			
			LOGGER.info("Using Pin " + pinName + " for ButtonListener, pressed when state is " + _buttonPressed.toString());
			
			_button = _gpio.provisionDigitalInputPin(RaspiPin.getPinByName(pinName), buttonStateIsHighWhenPressed ? PinPullResistance.PULL_DOWN : PinPullResistance.PULL_UP);
	        _button.setDebounce(DEBOUNCE_MS);
	        _button.addTrigger(new GpioCallbackTrigger(_buttonPressed, buttonPressCallback()));
	        _button.addTrigger(new GpioCallbackTrigger(_buttonReleased, buttonReleaseCallback()));
		}
	}
	
	private Callable<Void> buttonPressCallback() {
		return new Callable<Void>() {
			public Void call() {
				LOGGER.fine("Button PRESSED " + _button.getName());
				_lastButtonPressStart = Instant.now().toEpochMilli();
				return null;
			}
		};
	}
	
	private Callable<Void> buttonReleaseCallback() {
		return new Callable<Void>() {
			public Void call() {
				LOGGER.fine("Button released " + _button.getName());
				_lastButtonPressEnd = Instant.now().toEpochMilli();
				checkIfCallbackNeeded();
				return null;
			}
		};
	}
	
	private void checkIfCallbackNeeded() {
		if (_lastButtonPressStart == 0 || _lastButtonPressEnd == 0)
			return;
		
		long pressDuration = _lastButtonPressEnd - _lastButtonPressStart;
		
		if (pressDuration > LONG_PRESS_DURATION_MIN) {
			if (_longTriggerCallback != null)
				try {
					_longTriggerCallback.call();
				} catch (Exception e) {
					e.printStackTrace();
				}
		} else if (pressDuration > SINGLE_PRESS_DURATION_MIN) {
			if (_singleTriggerCallback != null)
				try {
					_singleTriggerCallback.call();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		
		resetButtonPressedState();
	}
	
	private void resetButtonPressedState() {
		_lastButtonPressEnd = 0;
		_lastButtonPressStart = 0;
	}

	@Override
	public void setSingleTriggerCallback(Callable<Void> callback) {
		if (_button == null)
			return;
		_singleTriggerCallback = callback;
	}

	@Override
	public void setLongTriggerCallback(Callable<Void> callback) {
		if (_button == null)
			return;
		_longTriggerCallback = callback;
	}
}
