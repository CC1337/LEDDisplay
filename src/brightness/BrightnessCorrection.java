package brightness;

import java.lang.invoke.MethodHandles;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;
import helper.Helper;
import input.ButtonListener;
import input.IButtonListener;


public class BrightnessCorrection implements IBrightnessCorrection, Observer {
	
	private static final String BRIGHTNESS_PROPERTIES = "brightness.properties";
	private static final String BRIGHTNESS_BUTTON_CYCLEVALUES = "Brightness.Button.CycleValues";
	private static final String BRIGHTNESS_BUTTON_GPIOPINNUMBER = "Brightness.Button.GpioPinNumber";
	private static final String BRIGHTNESS_AUTOBRIGHTNESS_LDRGPIOPINNUMBER = "Brightness.AutoBrightness.LdrGpioPinNumber";

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static IBrightnessCorrection _instance;
	private DisplayConfiguration _config;
	private int _currentBrightness;
	private int _newBrightness;
	private int _configuredBrightness;
	private IBrightnessSensorReader _brightnessSensorReader;
	private BrightnessReaderThread _brightnessReaderThread;
	private int _configuredAutoBrightnessNotificationThreshold = 0;
	private int _configuredAutoBrightnessMsBetweenUpdates = 5000;
	private String _configuredAutoBrightnessPin;
	private int _configuredAutoBrightnessMinimalValue;
	private int _configuredAutoBrightnessNumValuesForAverage;
	private int _configuredAutoBrightnessCapacitorUnloadNs;
	private String _configuredBrightnessCycleButtonPin;
	private String[] _configuredBrightnessCycleValues;
	
	private BrightnessCorrection () {
		_config = new DisplayConfiguration(BRIGHTNESS_PROPERTIES, true);
		_config.addObserver(this);
		reloadConfig();
		_currentBrightness = _configuredBrightness;
		_newBrightness = _configuredBrightness == 0 ? _configuredAutoBrightnessMinimalValue : _configuredBrightness;
		LOGGER.info("Initial brightness: " + _currentBrightness);
		initBrightnessSensorReader();
		initCycleBrightnessButton();
	}
	
	public static IBrightnessCorrection getInstance() {
		if (_instance == null) {
			_instance = new BrightnessCorrection();
		}
		return BrightnessCorrection._instance;
	}
	
	private void initBrightnessSensorReader() {
		if (_brightnessReaderThread != null)
			return;
		
		if (Helper.isWindows() || _configuredAutoBrightnessPin == null) {
			_brightnessSensorReader = new DummyBrightnessSensorReader(100);
			if (_configuredAutoBrightnessPin == null)
				LOGGER.warning("Warning: " + BRIGHTNESS_PROPERTIES + " " + BRIGHTNESS_AUTOBRIGHTNESS_LDRGPIOPINNUMBER + " invalid, falling back to dummy auto-brightness");
		} else {
			_brightnessSensorReader = new GpioLdrReader(_configuredAutoBrightnessPin, _configuredAutoBrightnessCapacitorUnloadNs);
		}
		
		_brightnessReaderThread = new BrightnessReaderThread(_brightnessSensorReader, _configuredAutoBrightnessNotificationThreshold, _configuredAutoBrightnessMsBetweenUpdates, _configuredAutoBrightnessNumValuesForAverage);
		_brightnessReaderThread.addObserver(this);
		
		new Thread(_brightnessReaderThread, "BrReader").start();
	}
	
	private void initCycleBrightnessButton() {
		
		if (_configuredBrightnessCycleButtonPin.isEmpty()) {
			LOGGER.severe("No valid \"cycle brightness\" button configured, set " + BRIGHTNESS_BUTTON_GPIOPINNUMBER + " in " + BRIGHTNESS_PROPERTIES + " and restart the application in order to switch modes by a button.");
			return;
		}
		
		LOGGER.info("Cycle Brightness button init on Pin GPIO " + _configuredBrightnessCycleButtonPin);
		IButtonListener nextModeButton = new ButtonListener(_configuredBrightnessCycleButtonPin, true);
		
		nextModeButton.setSingleTriggerCallback(new Callable<Void>() {
			public Void call() throws Exception {
				LOGGER.info("Cycle brightness button pressed short");
				nextCycleBrightnessValue();
				return null;
			}
		});
		
		nextModeButton.setLongTriggerCallback(new Callable<Void>() {
			public Void call() throws Exception {
				LOGGER.info("Cycle brightness button pressed long");
				if (_configuredBrightness == 0)
					setConfiguredBrightness("100");
				else
					setConfiguredBrightness("0");
				return null;
			}
		});
	}
	
	private void nextCycleBrightnessValue() {
		if (_configuredBrightnessCycleValues.length == 0) {
			LOGGER.severe("Next brightness cycle value requested but nothing configured in " + BRIGHTNESS_PROPERTIES + " at " + BRIGHTNESS_BUTTON_CYCLEVALUES);
			return;
		}
		
		int nextValueIndex = 0;
		for (int i = 0; i < _configuredBrightnessCycleValues.length; i++) {
			try {
				if (Integer.parseInt(_configuredBrightnessCycleValues[i]) == _configuredBrightness) {
					nextValueIndex = (i+1) % _configuredBrightnessCycleValues.length;
					break;
				}
			} catch (NumberFormatException e) {
				LOGGER.severe("Invalid cycle brightness value in " + BRIGHTNESS_PROPERTIES + " at " + BRIGHTNESS_BUTTON_CYCLEVALUES + ": " + _configuredBrightnessCycleValues[i]);
				continue;
			}
		}

		setConfiguredBrightness(_configuredBrightnessCycleValues[nextValueIndex]);
	}

	public int getBrightnessPercentage() {
		return _currentBrightness;
	}
	
	private void setConfiguredBrightness(String brightness) {
		_config.setString("Brightness.Value", brightness);
	}
	
	private void updateAutoBrightness() {
		if (_configuredBrightness == 0) {
			_newBrightness = _brightnessSensorReader.getLastBrightnessValue();
			if (_newBrightness < _configuredAutoBrightnessMinimalValue)
				_newBrightness = _configuredAutoBrightnessMinimalValue;
			LOGGER.info("New AutoBrightness value: " + _newBrightness);
		}
	}

	@Override
	public void update(Observable observable, Object arg1) {
		if (observable instanceof IDisplayConfiguration) {
			LOGGER.info("Brightness config updated");
			reloadConfig();
		}
		if (observable instanceof BrightnessReaderThread) {
			updateAutoBrightness();
		}
	}
	
	private void reloadConfig() {
		_configuredBrightness = _config.getInt("Brightness.Value", 100);
		_configuredAutoBrightnessNotificationThreshold = _config.getInt("Brightness.AutoBrightness.NotificationThresholdPercent", 0);
		_configuredAutoBrightnessMsBetweenUpdates = _config.getInt("Brightness.AutoBrightness.MillisecondsBetweenUpdates", 0);
		_configuredAutoBrightnessMinimalValue = _config.getInt("Brightness.AutoBrightness.MinimalValue", 0);
		_configuredAutoBrightnessNumValuesForAverage = _config.getInt("Brightness.AutoBrightness.NumValuesForAverage", 0);
				
		String newPin = _config.getString(BRIGHTNESS_AUTOBRIGHTNESS_LDRGPIOPINNUMBER);
		if (_configuredAutoBrightnessPin != null && !_configuredAutoBrightnessPin.equals(newPin))
			LOGGER.warning("Warning: You changed LDR Pin. You have to restart the application that changes take effect.");
		_configuredAutoBrightnessPin = newPin;	
		
		newPin = _config.getString(BRIGHTNESS_BUTTON_GPIOPINNUMBER);
		if (_configuredBrightnessCycleButtonPin != null && !_configuredBrightnessCycleButtonPin.equals(newPin))
			LOGGER.warning("Warning: You changed Brightness cycle button Pin. You have to restart the application that changes take effect.");
		_configuredBrightnessCycleButtonPin = newPin;	
		_configuredBrightnessCycleValues = _config.getStringArray(BRIGHTNESS_BUTTON_CYCLEVALUES);
		
		int newCapacitorUnloadNs = _config.getInt("Brightness.AutoBrightness.MaxCapacitorUnloadNanoseconds");
		if (_configuredAutoBrightnessCapacitorUnloadNs != 0 && _configuredAutoBrightnessCapacitorUnloadNs != newCapacitorUnloadNs)
			LOGGER.warning("Warning: You changed LDR Capacitor Unload milliseconds. You have to restart the application that changes take effect.");
		_configuredAutoBrightnessCapacitorUnloadNs = newCapacitorUnloadNs;
				
		updateBrightnessReaderSettings();

		if (_configuredBrightness != 0)
			_newBrightness = _configuredBrightness;
		if (_currentBrightness != _newBrightness)
			LOGGER.info("New brightness: " + _newBrightness);
	}

	private void updateBrightnessReaderSettings() {
		if (_brightnessReaderThread == null)
			return;
		
		_brightnessReaderThread.setBrightnessDiffNotificationThreshold(_configuredAutoBrightnessNotificationThreshold);
		_brightnessReaderThread.setMsBetweenUpdates(_configuredAutoBrightnessMsBetweenUpdates);
		_brightnessReaderThread.setNumValuesForAverage(_configuredAutoBrightnessNumValuesForAverage);
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
