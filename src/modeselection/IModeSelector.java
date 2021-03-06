package modeselection;

public interface IModeSelector extends Runnable {
	
	/** returns full class name of current mode **/
	String getCurrentMode();
	
	/** Called to trigger check if current mode should stay or change **/
	void modeCheck();
	
	/** Called by active mode if nothing to display -> requests mode selector to start another **/
	void modeEnded();
	
	/** returns true if the mode exists. Full class name containing package required as parameter. **/
	boolean modeExists(String fullModeName);
	
	/** Switches to a specific mode with the given name. Full class name with package needed. **/
	void startMode(String fullModeName);
	
	/** Call to force end current and start next mode configured in mode.cycle 
	 * @return full classpath of next mode **/
	String nextMode();
	
	/** Call to force next config (modename.<int>.properties) for the current active mode */
	void nextModeConfig();
	
	/** Start automatic scheduling of modes **/
	void startModeScheduler();
	
	/** Stop automatic scheduling of modes **/
	void stopModeScheduler();
	
	/** Toggle automatic scheduling of modes **/
	void toggleModeScheduler();
	
	/** Returns true if ModeScheduler is active **/
	boolean isModeSchedulerStarted();
	
	/** End all modes and don't start any new one, terminate ModeSelector **/
	void shutdown();
}
