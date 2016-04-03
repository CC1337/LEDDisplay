package brightness;

import java.util.Observable;

import helper.Helper;

public class BrightnessReaderThread extends Observable implements Runnable {

	private IBrightnessSensorReader _brightnessReader;
	private int _brightnessDiffNotificationThreshold;
	private int _lastNotifiedBrightnessValue;
	private int _msBetweenUpdates;
	private int[] _lastValues;
	private int _numValuesForAverage;
	private int _numValuesForAverageCurrentIndex;

	public BrightnessReaderThread(IBrightnessSensorReader brightnessReader, int brightnessDiffNotificationThreshold, int msBetweenUpdates, int numValuesForAverage) {
		_brightnessReader = brightnessReader;
		_brightnessDiffNotificationThreshold = brightnessDiffNotificationThreshold;
		_msBetweenUpdates = msBetweenUpdates;
		_lastNotifiedBrightnessValue = 0;
		_numValuesForAverageCurrentIndex = 0;
		setNumValuesForAverage(numValuesForAverage);
	}
	
	@Override
	public void run() {
		System.out.println("BrightnessReaderThread started.");
		while (42 < 1337) {
			checkAndNotifyBrightness();
			
			Helper.waitms(_msBetweenUpdates);
		}
	}
	
	public void setBrightnessDiffNotificationThreshold(int percent) {
		_brightnessDiffNotificationThreshold = percent;
	}
	
	public void setMsBetweenUpdates(int ms) {
		_msBetweenUpdates = ms;
	}
	
	public void setNumValuesForAverage(int _configuredAutoBrightnessNumValuesForAverage) {
		_numValuesForAverage = _configuredAutoBrightnessNumValuesForAverage;	
		_lastValues = new int[_numValuesForAverage];
	}
	
	private void checkAndNotifyBrightness() {
		_brightnessReader.updateBrightnessValue();
		_lastValues[_numValuesForAverageCurrentIndex++] = _brightnessReader.getLastBrightnessValue();
		_numValuesForAverageCurrentIndex %= _lastValues.length;
		
		int average = Helper.getArrayAverage(_lastValues);
		
		if (Math.abs((average - _lastNotifiedBrightnessValue)) <= _brightnessDiffNotificationThreshold)
			return;
		
		_lastNotifiedBrightnessValue = average;		
		setChanged();
		notifyObservers();
	}
}
