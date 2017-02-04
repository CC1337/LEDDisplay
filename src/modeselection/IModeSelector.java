package modeselection;

public interface IModeSelector extends Runnable {
	
	/** Called to trigger check if current mode should stay or change **/
	void modeCheck();
	
	/** Called by active mode if nothing to display -> requests mode selector to start another **/
	void modeEnded();
	
	/** Call to force end current and start next mode configured in mode.cycle **/
	void nextMode();
	
	/** Call to force next config (modename.<int>.properties) for the current active mode */
	void nextModeConfig();
	
	/** End all modes and don't start any new one, terminate ModeSelector **/
	void shutdown();
}