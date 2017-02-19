package modeselection;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;
import helper.Helper;
import led.ILEDArray;
import modes.IMode;
import output.IDisplayAdaptor;

public class ModeSelector implements IModeSelector, Observer {

	protected static final String MODELESECTOR_PROPERTIES = "modeselector.properties";
	protected static final String MODE_NEXT_GPIOPINNUMBER = "mode.next.buttonGpioPinNumber";
	protected static final String MODE_CYCLECONFIG_GPIOPINNUMBER = "mode.cycleConfig.GpioPinNumber";
	protected static final String MODE_SCHEDULERACTIVE = "mode.schedulerActive";
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private static ModeSelector __instance = null;
	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private IModeConfigSelector _configSelector;
	private LinkedHashMap<String, IMode> _modes = new LinkedHashMap<String, IMode>();
	private IMode _defaultMode;
	private IMode _currentMode;
	private IMode _lastConfiguredMode;
	private IDisplayConfiguration _config;
	private ModeSelectorButtons _modeSelectorButtons;
	private boolean _modeEnded = false;
	private boolean _modeCheckInProgress = false;
	private Thread _currentModeThread;
	private boolean _shouldShutdown = false;
	private Timer _modeCheckTimer = new Timer(true);
	private ModeScheduler _modeScheduler;

	private ModeSelector(IDisplayAdaptor display, ILEDArray leds, IModeConfigSelector configSelector) {
		LOGGER.info("ModeSelector Init");
		_display = display;
		_leds = leds;
		_configSelector = configSelector;
		_configSelector.addObserver(this);

		_config = new DisplayConfiguration(MODELESECTOR_PROPERTIES, true);

		try {
			initConfiguredModes();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		_lastConfiguredMode = getModeFromConfig();

		_modeSelectorButtons = new ModeSelectorButtons(this, _config);
		_modeSelectorButtons.initNextModeButton();
		_modeSelectorButtons.initCycleModeConfigurationButton();

		_modeScheduler = ModeScheduler.getInstance(this);
		restoreModeSchedulerStateFromConfig();
	}

	public static ModeSelector getInstance(IDisplayAdaptor display, ILEDArray leds,
			IModeConfigSelector configSelector) {
		if (__instance == null)
			__instance = new ModeSelector(display, leds, configSelector);
		return __instance;
	}

	@Override
	public void run() {
		LOGGER.info("ModeSelector Start");
		startMode(getModeFromConfig());
		autoTriggerModeCheck();
	}

	private void initConfiguredModes() throws ConfigurationException {
		String[] availableModesConfigured = _config.getStringArray("mode.available");

		if (availableModesConfigured.length == 0) {
			throw new ConfigurationException(
					"no mode.available configured in modeselector.properties! Can't launch. Please specify at least one full class name, e.g. modes.MyMode. Separate by ','.");
		}

		for (String modeName : availableModesConfigured) {
			try {
				modeName = modeName.trim();
				Class<?> newModeClass = Class.forName(modeName);
				IMode newMode = (IMode) newModeClass
						.getConstructor(IDisplayAdaptor.class, ILEDArray.class, IModeSelector.class, String.class)
						.newInstance(_display, _leds, this, _configSelector.getCurrentConfigFileName(newModeClass));
				_modes.put(modeName, newMode);

				if (_defaultMode == null)
					_defaultMode = newMode;

				LOGGER.info("Successfully registered mode: '" + newMode.modeName() + "'");
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	@Override
	public String getCurrentMode() {
		return _currentMode.getClass().getName().toString();
	}

	@Override
	public void modeCheck() {
		if (_modeCheckInProgress)
			return;

		_modeCheckInProgress = true;

		IMode configuredMode = getModeFromConfig();
		if (configuredMode.getClass() != _lastConfiguredMode.getClass()) {
			_currentMode.end();

			for (int i = 0; i < 50; i++) {
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
			// _currentMode.notify();

			startMode(configuredMode);
			_lastConfiguredMode = configuredMode;
		}
		_modeCheckInProgress = false;
	}

	private IMode getModeFromConfig() {
		String modeCurrent = _config.getString("mode.current");
		if (modeCurrent != null && _modes.containsKey(modeCurrent)) {
			return _modes.get(modeCurrent);
		} else {
			return _defaultMode;
		}
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
		LOGGER.info("Starting Mode: " + mode.getClass().getName());
		_currentMode = mode;
		_currentModeThread = new Thread(_currentMode, _currentMode.modeName().replace("Mode", "M"));
		_currentModeThread.start();
	}

	@Override
	public void modeEnded() {
		_modeEnded = true;
		modeCheck();
	}

	@Override
	public boolean modeExists(String fullModeName) {
		return _modes.containsKey(fullModeName);
	}

	@Override
	public void startMode(String fullModeName) {
		if (!modeExists(fullModeName))
			LOGGER.severe("Cannot switch to new mode because it does not exist: " + fullModeName);
		_config.setString("mode.current", fullModeName);
	}

	@Override
	public void nextMode() {
		String[] cycleModesConfigured = _config.getStringArray("mode.cycle");
		String newMode = _currentMode.getClass().getName();

		for (int i = 0; i < cycleModesConfigured.length; i++) {
			if (cycleModesConfigured[i].equals(_currentMode.getClass().getName())) {
				newMode = cycleModesConfigured[(i + 1) % cycleModesConfigured.length];
				break;
			}
		}

		_config.setString("mode.current", newMode);
	}

	@Override
	public void nextModeConfig() {
		_configSelector.nextConfig(_currentMode.modeName());
		tryUpdateCurrentModeConfig();
	}

	private void tryUpdateCurrentModeConfig() {
		try {
			_currentMode.changeConfig(_configSelector.getCurrentConfigFileName(_currentMode.modeName()));
		} catch (FileNotFoundException e) {
			LOGGER.severe(
					"Failed switching config for " + _currentMode.modeName() + " because no valid config file found.");
			e.printStackTrace();
		}
	}

	/**
	 * Triggered if modeconfigselector config changed externally
	 */
	@Override
	public void update(Observable observable, Object arg1) {
		if (observable instanceof IModeConfigSelector) {
			LOGGER.info("Triggering config change for current mode: " + _currentMode.modeName());
			tryUpdateCurrentModeConfig();
		}
	}

	@Override
	public void startModeScheduler() {
		_modeScheduler.start();
		_config.setString(MODE_SCHEDULERACTIVE, "1");
	}

	@Override
	public void stopModeScheduler() {
		_modeScheduler.stop();
		_config.setString(MODE_SCHEDULERACTIVE, "0");
	}
	
	@Override
	public void toggleModeScheduler() {
		_modeScheduler.toggle();
	}
	
	private void restoreModeSchedulerStateFromConfig() {
		if (_config.getInt(MODE_SCHEDULERACTIVE, 1) == 1)
			_modeScheduler.start();
	}

	@Override
	public boolean isModeSchedulerStarted() {
		return _modeScheduler.isStarted();
	}
	
	@Override
	public void shutdown() {
		LOGGER.info("ModeSelector Shutdown start");
		_shouldShutdown = true;
		_modeCheckTimer.cancel();
		stopModeScheduler();
		if (_currentMode != null)
			_currentMode.abort();
		if (_currentModeThread != null)
			try {
				_currentModeThread.join(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		LOGGER.info("ModeSelector Shutdown complete");
	}
}
