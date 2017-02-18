package input;

import java.util.concurrent.Callable;

public interface IButtonListener {

	/**
	 * Register callable that is executed every time the button is triggered once
	 * @param callback
	 */
	public void setSingleTriggerCallback(Callable<Void> callback);
	
	/**
	 * Register callable that is executed every time the button is triggered with a long press
	 * @param callback
	 */
	public void setLongTriggerCallback(Callable<Void> callback);
}
