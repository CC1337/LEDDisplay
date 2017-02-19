package input;

import java.lang.invoke.MethodHandles;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;
import helper.Helper;

public class ButtonFeedbackLed {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static ButtonFeedbackLed __instance;
	private IDisplayConfiguration _config = new DisplayConfiguration("global.properties", false);
	final GpioController _gpio = Helper.isWindows() ? null : GpioFactory.getInstance();
	final GpioPinDigitalOutput _led;
	final int LED_PIN = _config.getInt("input.ButtonFeedbackLed.GpioPinNumber", -1);

	private ButtonFeedbackLed() {
		String pinName = "GPIO " + LED_PIN;
		
		if (_gpio == null) {
			LOGGER.info("NOT using ButtonFeedbackLed at " + pinName + " because running on Windows w/o pi4j");
			_led = null;
		} else if (LED_PIN < 0) {
			LOGGER.info("NOT using ButtonFeedbackLed because no valid pin configured");
			_led = null;
		} else {
			LOGGER.info("Using Pin " + pinName + " for ButtonFeedbackLed");
			_led = _gpio.provisionDigitalOutputPin(RaspiPin.getPinByName(pinName));
			_led.setShutdownOptions(true, PinState.LOW);
			_led.low();
		}
	}
	
	public static ButtonFeedbackLed getInstance() {
		if (__instance == null)
			__instance = new ButtonFeedbackLed();
		return __instance;
	}

	public void blinkOnce() {
		if (_led == null)
			return;
		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				_led.blink(200, 300, PinState.HIGH);
			}
		}, 200);
	}

	public void blinkTwice() {
		if (_led == null)
			return;
		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				_led.blink(200, 700, PinState.HIGH);
			}
		}, 200);
	}
	
	public void blinkThreeTimes() {
		if (_led == null)
			return;
		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				_led.blink(200, 1100, PinState.HIGH);
			}
		}, 200);
	}
	
	public void blinkLong() {
		if (_led == null)
			return;
		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				_led.blink(800, 900, PinState.HIGH);
			}
		}, 200);
	}
}
