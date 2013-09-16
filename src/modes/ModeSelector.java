package modes;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;

import led.ILEDArray;
import output.IDisplayAdaptor;

public class ModeSelector implements IModeSelector {

	private static ModeSelector __instance = null;
	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private HashMap<String, IMode> _modes = new HashMap<String, IMode>();
	private IMode _defaultMode;
	private IMode _currentMode;
	private IMode _lastConfiguredMode;
	private IDisplayConfiguration _config;
	private boolean _modeEnded = false;
	
	private ModeSelector(IDisplayAdaptor display, ILEDArray leds) {
		System.out.println("ModeSelector Init");
		_display = display;
		_leds = leds;
		
		IMode clockMode = new ClockMode(_display, _leds, this);
		IMode sleepMode = new SleepMode(_display, _leds, this);
		_modes.put("ClockMode", clockMode);
		_modes.put("SleepMode", sleepMode);
		
		_defaultMode = clockMode;	
		_lastConfiguredMode = clockMode;
		
		_config = new DisplayConfiguration("modeselector.properties", true);
	}
	
	public static ModeSelector getInstance(IDisplayAdaptor display, ILEDArray leds) {
		if (__instance == null)
			__instance = new ModeSelector(display, leds);
		return __instance;
	}

	public void modeCheck() {

		IMode configuredMode = getModeFromConfig();
		if (configuredMode.getClass() != _lastConfiguredMode.getClass()) {
			_currentMode.end();
			try {
				for (int i=0; i<50; i++) {
					if (_modeEnded)
						break;
					Thread.sleep(100);
				}
			} catch (InterruptedException e) {

			}
			_currentMode.abort();
			//_currentMode.notify();

			startMode(configuredMode);
			_lastConfiguredMode = configuredMode;
		}
	}

	@Override
	public void modeEnded() {
		_modeEnded = true;
		modeCheck();
	}
	
	@Override
	public void run() {
		System.out.println("ModeSelector Start");
		startMode(getModeFromConfig());
		autoTriggerModeCheck();
	}
	
	private void autoTriggerModeCheck() {
		new Timer(true).schedule(new TimerTask() {
			public void run() {
				modeCheck();
				autoTriggerModeCheck();
			}
		}, 3000);
	}
	
	private void startMode(IMode mode) {
		System.out.println("Starting Mode: " + mode.getClass().getName()); 
		_currentMode = mode;
		new Thread(_currentMode).start();
	}
	
	
	private IMode getModeFromConfig() {
		String modeCurrent = _config.getString("mode.current");
		//System.out.println("get mode, currently "+ modeCurrent);
		if (modeCurrent != null && _modes.containsKey(modeCurrent)) {
			return _modes.get(modeCurrent);
		} else {
			return _defaultMode;
		}
	}

}
