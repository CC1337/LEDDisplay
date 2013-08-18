package modes;

public interface IModeSelector extends Runnable {
		
	/** Should be called by every IMode "often" to enable program to react quickly if state change requested **/
	void modeCheck();
	
	/** Called by active mode if nothing to display -> requests mode selector to start another **/
	void modeEnded();
}
