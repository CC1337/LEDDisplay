package brightness;

public class DummyBrightnessSensorReader implements IBrightnessSensorReader {

	private int _valueToReturn;

	public DummyBrightnessSensorReader(int valueToReturn) {
		_valueToReturn = valueToReturn;
	}
	
	@Override
	public int getLastBrightnessValue() {
		return _valueToReturn;
	}

	@Override
	public void updateBrightnessValue() {
	}

}
