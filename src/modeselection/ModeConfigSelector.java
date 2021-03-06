package modeselection;

import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;
import helper.FileHelper;


public class ModeConfigSelector extends Observable implements IModeConfigSelector, Observer {

	private static final String MODECONFIGSELECTOR_PROPERTIES = "modeconfigselector.properties";
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private static ModeConfigSelector __instance;
	
	private DisplayConfiguration _modeConfigSelectorConfig;

	private ModeConfigSelector() {
		_modeConfigSelectorConfig = new DisplayConfiguration(MODECONFIGSELECTOR_PROPERTIES, true);
		_modeConfigSelectorConfig.addObserver(this);
	}
	
	public static ModeConfigSelector getInstance() {
		if (__instance == null)
			__instance = new ModeConfigSelector();
		return __instance;
	}
	
	
	public String getCurrentConfigFileName(String modeName) throws FileNotFoundException {
		return getCurrentConfigFileName(modeName, false); 
	}
	
	public String getCurrentConfigFileName(Class<?> modeClass) throws FileNotFoundException {
		return getCurrentConfigFileName(modeClass.getSimpleName());
	}

	private String getCurrentConfigFileName(String modeName, boolean secondTry) throws FileNotFoundException {
		String fileName = getFileName(modeName, getCurrentCycleValueFromConfig(modeName));
		if (!FileHelper.fileExists(fileName)) {
			if (secondTry)
				throw new FileNotFoundException("No valid config found for " + modeName + ", last Try was: " + fileName);
			LOGGER.severe("Configuration file " + fileName + " does not exist! Trying another cycle value...");
			nextConfig(modeName);
			return getCurrentConfigFileName(modeName, true);
		}
		return fileName;
	}

	public void nextConfig(String modeName) {
		int nextCycleValue = getCurrentCycleValueFromConfig(modeName) + 1;
		String nextFileName = getFileName(modeName, nextCycleValue);
		if (!FileHelper.fileExists(nextFileName)) {
			nextCycleValue = 0;
			nextFileName = getFileName(modeName, nextCycleValue);
		}
		if (!FileHelper.fileExists(nextFileName)) {
			LOGGER.severe("Configuration file " + nextFileName + " MUST exist! Please create.");
			LOGGER.info("Additional files for cycling with the naming pattern <modeName>.<int>.properties are optional. <int> has to start at 1 and should not contain gaps.");
			return;
		}
		LOGGER.info("Switching config of " + modeName + " to " + getFileName(modeName, nextCycleValue));
		_modeConfigSelectorConfig.setString(getCycleValueConfigKey(modeName), String.valueOf(nextCycleValue));
	}

	private String getFileName(String modeName, int cycleValue) {
		return modeName.toLowerCase() + (cycleValue == 0 ? "" : "." + cycleValue) + ".properties";
	}
	
	private String getCycleValueConfigKey(String modeName) {
		return modeName + ".cyclevalue";
	}
	
	private int getCurrentCycleValueFromConfig(String modeName) {
		return _modeConfigSelectorConfig.getInt(getCycleValueConfigKey(modeName), 0);
	}
	
	@Override
	public void update(Observable observable, Object arg1) {
		if (observable instanceof IDisplayConfiguration) {
			LOGGER.info(MODECONFIGSELECTOR_PROPERTIES + " updated");
			setChanged();
			notifyObservers();
		}
	}
}
