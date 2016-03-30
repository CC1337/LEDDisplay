package brightness;

import java.util.Observable;

public class BrightnessReaderThread extends Observable implements Runnable {

	private IBrightnessSensorReader _brightnessReader;
	private int _brightnessDiffNotificationThreshold;
	private int _lastNotifiedBrightnessValue;
	private int _currentBrightnessValue;
	private int _msBetweenUpdates;

	public BrightnessReaderThread(IBrightnessSensorReader brightnessReader, int brightnessDiffNotificationThreshold, int msBetweenUpdates) {
		_brightnessReader = brightnessReader;
		_brightnessDiffNotificationThreshold = brightnessDiffNotificationThreshold;
		_msBetweenUpdates = msBetweenUpdates;
		_lastNotifiedBrightnessValue = 0;
		_currentBrightnessValue = 0;
	}
	
	@Override
	public void run() {
		System.out.println("BrightnessReaderThread started.");
		while (42 < 1337) {
			checkAndNotifyBrightness();
			
			try {
				Thread.sleep(_msBetweenUpdates);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setBrightnessDiffNotificationThreshold(int percent) {
		_brightnessDiffNotificationThreshold = percent;
	}
	
	public void setMsBetweenUpdates(int ms) {
		_msBetweenUpdates = ms;
	}
	
	private void checkAndNotifyBrightness() {
		_brightnessReader.updateBrightnessValue();
		_currentBrightnessValue = _brightnessReader.getLastBrightnessValue();
		
		if (Math.abs((_currentBrightnessValue - _lastNotifiedBrightnessValue)) <= _brightnessDiffNotificationThreshold)
			return;
		
		_lastNotifiedBrightnessValue = _currentBrightnessValue;		
		setChanged();
		notifyObservers();
	}

}
