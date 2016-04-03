package helper;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;

public class FpsController {

	private static FpsController _instance;
	private IDisplayConfiguration _config = new DisplayConfiguration("global.properties", false);
	private long _lastFrameStartTime;
	private int _msBetweenFrames;

	private FpsController() {
		_lastFrameStartTime = System.currentTimeMillis();
		_msBetweenFrames = _config.getInt("fps.MsBetweenFrames", 34);
	}
	
	public static FpsController getInstance() {
		if (_instance == null) {
			_instance = new FpsController();
		}
		return FpsController._instance;
	}
	
	public void waitForNextFrame() {
		long timediff = System.currentTimeMillis() - _lastFrameStartTime;
		
		if (timediff < _msBetweenFrames) {
			try {
				Thread.sleep(_msBetweenFrames - timediff);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		_lastFrameStartTime = System.currentTimeMillis();
	}
	
}
