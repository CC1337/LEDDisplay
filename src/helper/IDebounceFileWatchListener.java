package helper;

public interface IDebounceFileWatchListener {
	
	/**
	 * Called once per file modification within the configured amount of debouncing time, default 50ms.
	 * @param fileName of the modified file
	 */
	public void fileChanged(String fileName);
}
