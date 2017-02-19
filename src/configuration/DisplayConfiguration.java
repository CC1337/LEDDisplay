package configuration;

import java.lang.invoke.MethodHandles;
import java.util.Observable;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import helper.DebouncedFileModifiedWatch;
import helper.IDebounceFileWatchListener;


public class DisplayConfiguration extends Observable implements IDebounceFileWatchListener, IDisplayConfiguration{

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private String _filename;
	private boolean _enableAutoReload;
	private boolean _configHasChanged = false;
	private PropertiesConfiguration _configuration;
	private DebouncedFileModifiedWatch _fileWatch;

	public DisplayConfiguration(String filename, boolean enableAutoReload) {
		_filename = filename;
		_enableAutoReload = enableAutoReload;
		
		System.loadLibrary("jnotify");
		reload();
		if (_enableAutoReload)
			_fileWatch = new DebouncedFileModifiedWatch(_filename, this);
	}
	
	public void reload() {
		try {
			_configuration = new PropertiesConfiguration(_filename);
		} catch (ConfigurationException e) {
			e.printStackTrace();
			LOGGER.severe("Error reading config from " + _filename);
		}
		_configuration.setListDelimiter(',');
		_configuration.setAutoSave(true);
		setChanged();
		notifyObservers();
		_configHasChanged = false;
	}

	public void changeConfigFile(String newFileName) {
		if (_filename.equals(newFileName))
			return;
		_filename = newFileName;
		if (_enableAutoReload)
			_fileWatch.changeFile(_filename);
		reload();
	}
	
	@Override
	public String getConfigFileName() {
		return _filename;
	}
	
	@Override
	public void stopWatching() {
		_fileWatch.destroy();
		_fileWatch = null;
		_enableAutoReload = false;
	}

	public boolean hasChanged() {
		return _configHasChanged;
	}
	
	public String getString(String key) {
		return _configuration.getString(key);
	}
	
	public String getString(String key, String defaultValue) {
		return _configuration.getString(key, defaultValue);
	}
	
	public String[] getStringArray(String key) {
		return _configuration.getStringArray(key);
	}
	
	public int getInt(String key) {
		return _configuration.getInt(key);
	}
	
	public int getInt(String key, int defaultValue) {
		return _configuration.getInt(key, defaultValue);
	}

	public double getDouble(String key) {
		return _configuration.getDouble(key);
	}
	
	public double getDouble(String key, double defaultValue) {
		return _configuration.getDouble(key, defaultValue);
	}

	public void setString(String key, String newValue) {
		_configuration.setProperty(key, newValue);
	}

	@Override
	public void fileChanged(String fileName) {
		reload();
	}
}
