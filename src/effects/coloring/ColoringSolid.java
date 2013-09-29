package effects.coloring;

import configuration.IDisplayConfiguration;
import led.ILEDArray;
import effects.*;

public class ColoringSolid implements IColor {
	
	private int _r;
	private int _g;
	private int _b;
	private IDisplayConfiguration _config;
	private String _configPrefix = "";
	
	public ColoringSolid(int r, int g, int b) {
		setColor(r, g, b);
	}
	
	public ColoringSolid(IDisplayConfiguration config, String configPrefix) {
		_config = config;
		_configPrefix = configPrefix;
	}
	
	public ColoringSolid() {
		setColor(0, 0, 0);
	}

	@Override
	public void apply(ILEDArray leds, IColorableEffect effect) {
		setColorFromConfig();
		byte[][] effectData = effect.getEffectData();
		for(int x=0; x<effectData.length; x++) {
			for(int y=0; y<effectData[0].length; y++) {
				if (effectData[x][y] == 1)
					leds.setLed(x+effect.getPosX(), y+effect.getPosY(), _r, _g, _b);
			}
		}		
	}
	
	public void setColor(int r, int g, int b) {
		_r = r;
		_g = g;
		_b = b;
	}
	
	public void setR(int r) {
		_r = r;
	}
	
	public void setG(int g) {
		_g = g;
	}
	
	public void setB(int b) {
		_b = b;
	}
	
	public int getR() {
		return _r;
	}

	public int getG() {
		return _g;
	}
	
	public int getB() {
		return _b;
	}
	
	public void setColorFromConfig() {
		if (_config != null) {
			_r = _config.getInt(getConfigKey("r"), 0);
			_g = _config.getInt(getConfigKey("g"), 0);
			_b = _config.getInt(getConfigKey("b"), 0);
		}
	}
	
	private String getConfigKey(String param) {
		return _configPrefix + this.getClass().getName() + "." + param;
	}
}
