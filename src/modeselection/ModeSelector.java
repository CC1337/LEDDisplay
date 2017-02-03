package modeselection;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import org.apache.commons.configuration.ConfigurationException;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;
import helper.Helper;
import input.ButtonListener;
import input.IButtonListener;
import led.ILEDArray;
import modes.IMode;
import output.IDisplayAdaptor;

public class ModeSelector implements IModeSelector {

	private static final String MODELESECTOR_PROPERTIES = "modeselector.properties";
	private static final String MODE_NEXT_GPIOPINNUMBER = "mode.next.buttonGpioPinNumber";
	private static final String MODE_CYCLECONFIG_GPIOPINNUMBER = "mode.cycleConfig.GpioPinNumber";
	
	private static ModeSelector __instance = null;
	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private LinkedHashMap<String, IMode> _modes = new LinkedHashMap<String, IMode>();
	private IMode _defaultMode;
	private IMode _currentMode;
	private IMode _lastConfiguredMode;
	private IDisplayConfiguration _config;
	private boolean _modeEnded = false;
	private boolean _modeCheckInProgress = false;
	private Thread _currentModeThread;
	private boolean _shouldShutdown = false;
	private Timer _modeCheckTimer = new Timer(true);
	private boolean krassesFlag = true;
	
	private ModeSelector(IDisplayAdaptor display, ILEDArray leds) {
		System.out.println("ModeSelector Init");
		_display = display;
		_leds = leds;
		
		_config = new DisplayConfiguration(MODELESECTOR_PROPERTIES, true);

		try {
			initConfiguredModes();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		_lastConfiguredMode = getModeFromConfig();
		initNextModeButton();
		initCycleModeConfigurationButton();
	}


	public static ModeSelector getInstance(IDisplayAdaptor display, ILEDArray leds) {
		if (__instance == null)
			__instance = new ModeSelector(display, leds);
		return __instance;
	}
	
	@Override
	public void run() {
		System.out.println("ModeSelector Start");
		startMode(getModeFromConfig());
		autoTriggerModeCheck();
	}

	private void initNextModeButton() {
		String gpioPinNumber = _config.getString(MODE_NEXT_GPIOPINNUMBER, "");
		
		if (gpioPinNumber.isEmpty()) {
			System.err.println("No valid \"next mode\" button configured, set " + MODE_NEXT_GPIOPINNUMBER + " in " + MODELESECTOR_PROPERTIES + " and restart the application in order to switch modes by a button.");
			return;
		}
		
		System.out.println("Next Mode button init on Pin GPIO " + gpioPinNumber);
		IButtonListener nextModeButton = new ButtonListener(gpioPinNumber);
		nextModeButton.setSingleTriggerCallback(new Callable<Void>() {
			public Void call() throws Exception {
				System.out.println("Next mode button pressed");
				nextMode();
				return null;
			}
		});
	}
	
	private void initCycleModeConfigurationButton() {
		String gpioPinNumber = _config.getString(MODE_CYCLECONFIG_GPIOPINNUMBER, "");
		
		if (gpioPinNumber.isEmpty()) {
			System.err.println("No valid \"cycle mode configuration\" button configured, set " + MODE_CYCLECONFIG_GPIOPINNUMBER + " in " + MODELESECTOR_PROPERTIES + " and restart the application in order to switch mode config by a button.");
			return;
		}
		
		System.out.println("Cycle Mode Configuration button init on Pin GPIO " + gpioPinNumber);
		IButtonListener nextModeConfigButton = new ButtonListener(gpioPinNumber);
		nextModeConfigButton.setSingleTriggerCallback(new Callable<Void>() {
			public Void call() throws Exception {
				System.out.println("Cycle mode configuration button pressed");
				System.out.println(_currentMode.getClass());
				_currentMode.nextConfig();
				return null;
			}
		});
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
	
	public void modeCheck() {
		if (_modeCheckInProgress)
			return;

		_modeCheckInProgress = true;
		
		// Hacky BDayMode switch - TODO make clean
		String currentTime = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date());
		if (currentTime.equals("19.06.2016 00:00") && krassesFlag) {
			krassesFlag = false;
			_currentMode.abort();
			_config.setString("mode.current", "modes.BDayMode");
			startMode(_modes.get("modes.BDayMode"));
		}
		
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

	private void autoTriggerModeCheck() {
		_modeCheckTimer.schedule(new TimerTask() {
			public void run() {
				if (_shouldShutdown)
					return;
				modeCheck();
				autoTriggerModeCheck();
			}
		}, 1000);
	}
	
	private void startMode(IMode mode) {
		System.out.println("Starting Mode: " + mode.getClass().getName()); 
		_currentMode = mode;
		_currentModeThread = new Thread(_currentMode);
		_currentModeThread.start();
	}
	
	public void nextMode() {
		String[] cycleModesConfigured = _config.getStringArray("mode.cycle");
		String newMode = _currentMode.getClass().getName();
		
		for (int i=0; i<cycleModesConfigured.length; i++) {
			if (cycleModesConfigured[i].equals(_currentMode.getClass().getName())) {
				newMode = cycleModesConfigured[(i+1)%cycleModesConfigured.length];
				break;
			}
		}
		
		_config.setString("mode.current", newMode);
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
