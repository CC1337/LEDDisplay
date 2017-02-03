package configuration;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class CycleableModeConfiguration extends DisplayConfiguration implements Observer {

	private static final String CYCLEABLEMODECONFIGURATION_PROPERTIES = "cycleablemodeconfiguration.properties";
	
	private DisplayConfiguration _cycleConfig;
	private String _modeName;
	private String _lastFileName;

	public CycleableModeConfiguration(String modeName, boolean enableAutoReload) {
		super(modeName + ".properties", enableAutoReload);
		
		_modeName = modeName;
		_cycleConfig = new DisplayConfiguration(CYCLEABLEMODECONFIGURATION_PROPERTIES, true);
		_cycleConfig.addObserver(this);
		
		restoreCycleValueFromConfig();
	}

	private void restoreCycleValueFromConfig() {
		String fileName = getFileName(getCurrentCycleValueFromConfig());
		if (fileName.equals(_lastFileName))
			return;
		if (!fileExists(fileName)) {
			System.err.println("Configuration file " + fileName + " does not exist! Trying another cycle value...");
			nextConfiguration();
			return;
		}
		_lastFileName = fileName;
		super.changeConfigFile(fileName);
	}

	public void nextConfiguration() {
		int nextCycleValue = getCurrentCycleValueFromConfig() + 1;
		String nextFileName = getFileName(nextCycleValue);
		if (!fileExists(nextFileName)) {
			nextCycleValue = 0;
			nextFileName = getFileName(nextCycleValue);
		}
		if (!fileExists(nextFileName)) {
			System.err.println("Configuration file " + nextFileName + " MUST exist! Please create.");
			System.out.println("Additional files for cycling with the naming pattern <modeName>.<int>.properties are optional. <int> has to start at 1 and should not contain gaps.");
			return;
		}
		_cycleConfig.setString(getCycleValueConfigKey(), String.valueOf(nextCycleValue));
		restoreCycleValueFromConfig(); // TODO get rid of this line together of making update on cfg change work, see update() method
	}

	private String getFileName(int cycleValue) {
		return _modeName + (cycleValue == 0 ? "" : "." + cycleValue) + ".properties";
	}
	
	private String getCycleValueConfigKey() {
		return _modeName + ".cyclevalue";
	}
	
	private int getCurrentCycleValueFromConfig() {
		return _cycleConfig.getInt(getCycleValueConfigKey(), 0);
	}
	
	private boolean fileExists(String fileName) {
		File f = new File(fileName);
		return f.exists() && !f.isDirectory();
	}

	@Override
	public void update(Observable observable, Object arg1) {
		if (observable instanceof IDisplayConfiguration) {
			System.out.println(CYCLEABLEMODECONFIGURATION_PROPERTIES + " updated");
			// restoreCycleValueFromConfig();	
			// TODO make config changes work, currently only updates last registered mode's config!
		}
	}
}
