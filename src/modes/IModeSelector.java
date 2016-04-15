package modes;

public interface IModeSelector extends Runnable {
	
	/** Called to trigger check if current mode should stay or change **/
	void modeCheck();
	
	/** Called by active mode if nothing to display -> requests mode selector to start another **/
	void modeEnded();
	
	/** End all modes and don't start any new one, terminate ModeSelector **/
	void shutdown();
}
