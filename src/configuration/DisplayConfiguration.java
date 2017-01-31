package configuration;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


public class DisplayConfiguration extends Observable implements JNotifyListener, IDisplayConfiguration{

	private String _filename;
	private boolean _enableAutoReload;
	private boolean _configHasChanged = false;
	private PropertiesConfiguration _configuration;
	private int _fileWatchId = -1;
	private Timer debouncedFileModifiedTimer = new Timer(true);

	public DisplayConfiguration(String filename, boolean enableAutoReload) {
		_filename = filename;
		_enableAutoReload = enableAutoReload;
		
		System.loadLibrary("jnotify");
		reload();
		if (_enableAutoReload) {
			try {
				_fileWatchId = JNotify.addWatch(_configuration.getPath(), JNotify.FILE_MODIFIED, false, this);
			} catch (JNotifyException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void reload() {
		try {
			_configuration = new PropertiesConfiguration(_filename);
		} catch (ConfigurationException e) {
			e.printStackTrace();
			System.err.println("Error reading config from " + _filename);
		}
		_configuration.setListDelimiter(',');
		_configuration.setAutoSave(true);
		setChanged();
		notifyObservers();
		_configHasChanged = false;
	}
	
	public boolean hasChanged() {
		return _configHasChanged;
	}
	
	public void stopWatching() {
		if (_fileWatchId != -1) {
			try {
				JNotify.removeWatch(_fileWatchId);
				_fileWatchId = -1;
			} catch (JNotifyException e) {
				e.printStackTrace();
			}
		}
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

	@Override
	public void fileCreated(int wd, String rootPath, String name) {

	}

	@Override
	public void fileDeleted(int wd, String rootPath, String name) {
	}

	@Override
	public void fileModified(int wd, String rootPath, String name) {
		if (rootPath.endsWith(_filename)) {
			debouncedFileModified();
		}
	}
	
	private void debouncedFileModified() {
		try {
			debouncedFileModifiedTimer.cancel();
			debouncedFileModifiedTimer = new Timer(true);
			debouncedFileModifiedTimer.schedule(new TimerTask() {
				public void run() {
					System.out.println(_filename + " changed. Reloading...");
					reload();	
					debouncedFileModifiedTimer.cancel();
				}
			}, 50);
		} 
		catch (java.lang.IllegalStateException e) {}
	}
	

	@Override
	public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
	}

	@Override
	public void setString(String key, String newValue) {
		_configuration.setProperty(key, newValue);
	}
	
}
