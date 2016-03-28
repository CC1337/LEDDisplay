package brightness;

import java.util.Observable;
import java.util.Observer;

import configuration.DisplayConfiguration;

public class BrightnessCorrection implements IBrightnessCorrection, Observer {
	
	private static IBrightnessCorrection _instance;
	private DisplayConfiguration _config;
	private int _currentBrightness;
	private int _newBrightness;
	
	private BrightnessCorrection () {
		_config = new DisplayConfiguration("brightness.properties", true);
		_config.addObserver(this);
		_currentBrightness = _config.getInt("brightness", 100);
		_newBrightness = _currentBrightness;
		System.out.println("Initial brightness: " + _currentBrightness);
	}

	public static IBrightnessCorrection getInstance() {
		if (_instance == null) {
			_instance = new BrightnessCorrection();
		}
		return BrightnessCorrection._instance;
	  }

	public int getBrightnessPercentage() {
		if (_currentBrightness != 0)
			return _currentBrightness;
		return getAutoBrightness();
	}
	
	private int getAutoBrightness() {
		// TODO
		return 0;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		System.out.println("Brightness config updated");
		reloadConfig();
	}
	
	private void reloadConfig() {
		_newBrightness = _config.getInt("brightness", 100);
		if (_currentBrightness != _newBrightness)
			System.out.println("New brightness: " + _newBrightness);
	}

	public void doDimmingStep() {
		if (_newBrightness == _currentBrightness)
			return;
		
		if (_newBrightness > _currentBrightness)
			_currentBrightness++;
		else
			_currentBrightness--;
	}

}
