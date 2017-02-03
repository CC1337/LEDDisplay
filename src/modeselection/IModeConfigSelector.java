package modeselection;

import java.io.FileNotFoundException;
import interfaces.IObservable;

public interface IModeConfigSelector extends IObservable {


	/**
	 * gets config name for given mode with current cycle value
	 * @throws FileNotFoundException if no valid config file found
	 * @param modeName
	 * @return properties filename
	 */
	public String getCurrentConfigFileName(String modeName) throws FileNotFoundException;
	
	/**
	 * gets config name for given mode with current cycle value
	 * @throws FileNotFoundException if no valid config file found
	 * @param class of a mode
	 * @return properties filename
	 */
	public String getCurrentConfigFileName(Class<?> forName) throws FileNotFoundException;
	
	/**
	 * cycles config file to next available one
	 * @param modeName
	 */
	public void nextConfig(String modeName);
}
