package configuration;

public interface IDisplayConfiguration {
	
	/**
	 * Reloads the configuration, hasChanged will be false until next config change on disk/database...
	 */
	public void reload();
	
	/**
	 * Changes the underlying config file on the disk initially given in constructor. Will trigger a reload of course.
	 */
	public void changeConfigFile(String newFileName);
	
	/**
	 * Returns the name of the currently used config file in this instance
	 */
	public String getConfigFileName();
	
	/**
	 * Returns current config sync state
	 * @return true, if config changed but not yet reloaded (trigger reload() or set enableConfigReload to true at object creation)
	 */
	public boolean hasChanged();
	
	/**
	 * Stops watching config file if enableAutoReload = true
	 */
	public void stopWatching();
		
	
	public String getString(String key);
	
	public String getString(String key, String defaultValue);
	
	public String[] getStringArray(String key);
	
	public int getInt(String key);
	
	public int getInt(String key, int defaultValue);
	
	public double getDouble(String key);
	
	public double getDouble(String key, double defaultValue);

	public void setString(String key, String newValue);
}
