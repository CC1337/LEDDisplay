package effects.animation;

import configuration.IDisplayConfiguration;
import effects.*;
import led.ILEDArray;

public class RandomDotEffect implements IColorableEffect {
	
	IColor _color;
	double _probability;
	byte[][] _data;
	private IDisplayConfiguration _config;
	private String _configPrefix = "";
	

	public RandomDotEffect(IColor color) {
		this(color, 0.02);
	}
	
	public RandomDotEffect(IColor color, double probability) {
		_color = color;
		_probability = probability;
	}
	
	public RandomDotEffect(IColor color, IDisplayConfiguration config, String configPrefix) {
		_color = color;
		_config = config;
		_configPrefix = configPrefix;
	}
	
	@Override
	public void setColor(IColor color) {
		_color = color;
	}
	
	@Override
	public IColor getColor() {
		return _color;
	}
	
	@Override
	public void apply(ILEDArray leds) {
		applyConfig();
		_data = new byte[leds.sizeX()][leds.sizeY()];
		for(int x=0; x<leds.sizeX(); x++) {
			for(int y=0; y<leds.sizeY(); y++) {
				_data[x][y] = (byte) (Math.random() < _probability ? 1 : 0);
			}
		}
		_color.apply(leds, this);
	}

	@Override
	public void setPosX(int x) {
		
	}

	@Override
	public void setPosY(int y) {
		
	}

	@Override
	public int getPosX() {

		return 0;
	}

	@Override
	public int getPosY() {

		return 0;
	}

	@Override
	public byte[][] getEffectData() {
		return _data;
	}
	
	protected void applyConfig() {
		if (_config != null) {
			_probability = _config.getDouble(getConfigKey("probability"), 0.05);
		}
	}
	
	protected String getConfigKey(String param) {
		return _configPrefix + this.getClass().getName() + "." + param;
	}

	
}
