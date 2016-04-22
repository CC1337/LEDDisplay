package modes;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.configuration.ConfigurationException;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;
import helper.Helper;
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
	private boolean _modeCheckInProgress = false;
	private Thread _currentModeThread;
	private boolean _shouldShutdown = false;
	private Timer _modeCheckTimer;
	
	private ModeSelector(IDisplayAdaptor display, ILEDArray leds) {
		System.out.println("ModeSelector Init");
		_display = display;
		_leds = leds;
		
		_config = new DisplayConfiguration("modeselector.properties", true);

		try {
			initConfiguredModes();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		_lastConfiguredMode = getModeFromConfig();
	}
	
	private void initConfiguredModes() throws ConfigurationException {
		String[] availableModesConfigured = _config.getStringArray("mode.available");
		
		if (availableModesConfigured.length == 0) {
			throw new ConfigurationException("no mode.available configured in modeselector.properties! Can't launch. Please specify at least one full class name, e.g. modes.MyMode. Separate by ','.");
		}
		
		for (String modeName : availableModesConfigured) {
			try {
				modeName = modeName.trim();
				IMode newMode = (IMode) Class.forName(modeName).getConstructor(IDisplayAdaptor.class, ILEDArray.class, IModeSelector.class).newInstance(_display, _leds, this);
				_modes.put(modeName, newMode);
				
				if (_defaultMode == null)
					_defaultMode = newMode;
				
				System.out.println("Successfully registered mode: '" + newMode.modeName() + "'");
			} catch (Exception exception) {
				exception.printStackTrace();
			} 
		}
	}
	
	public static ModeSelector getInstance(IDisplayAdaptor display, ILEDArray leds) {
		if (__instance == null)
			__instance = new ModeSelector(display, leds);
		return __instance;
	}

	public void modeCheck() {
		if (_modeCheckInProgress)
			return;
		
		_modeCheckInProgress = true;
		
		IMode configuredMode = getModeFromConfig();
		if (configuredMode.getClass() != _lastConfiguredMode.getClass()) {
			_currentMode.end();

			for (int i=0; i<50; i++) {
				if (_modeEnded)
					break;
				Helper.waitms(100);
			}

			_currentMode.abort();
			if (_currentModeThread != null)
				try {
					_currentModeThread.join(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			//_currentMode.notify();

			startMode(configuredMode);
			_lastConfiguredMode = configuredMode;
		}
		_modeCheckInProgress = false;
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
		_modeCheckTimer = new Timer(true);
		_modeCheckTimer.schedule(new TimerTask() {
			public void run() {
				if (_shouldShutdown)
					return;
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

	@Override
	public void shutdown() {
		System.out.println("ModeSelector Shutdown start");
		_shouldShutdown = true;
		_modeCheckTimer.cancel();
		if (_currentMode != null)
			_currentMode.abort();
		if (_currentModeThread != null)
			try {
				_currentModeThread.join(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		System.out.println("ModeSelector Shutdown complete");
	}

}
