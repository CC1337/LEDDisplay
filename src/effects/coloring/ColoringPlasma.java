package effects.coloring;

import configuration.IDisplayConfiguration;
import led.ILEDArray;
import effects.*;

public class ColoringPlasma implements IColor {
	
	private int[] cos_wave = new int[] { 
		0,0,0,0,1,1,1,2,2,3,4,5,6,6,8,9,10,11,12,14,15,17,18,20,22,23,25,27,29,31,33,35,38,40,42,
		45,47,49,52,54,57,60,62,65,68,71,73,76,79,82,85,88,91,94,97,100,103,106,109,113,116,119,
		122,125,128,131,135,138,141,144,147,150,153,156,159,162,165,168,171,174,177,180,183,186,
		189,191,194,197,199,202,204,207,209,212,214,216,218,221,223,225,227,229,231,232,234,236,
		238,239,241,242,243,245,246,247,248,249,250,251,252,252,253,253,254,254,255,255,255,255,
		255,255,255,255,254,254,253,253,252,252,251,250,249,248,247,246,245,243,242,241,239,238,
		236,234,232,231,229,227,225,223,221,218,216,214,212,209,207,204,202,199,197,194,191,189,
		186,183,180,177,174,171,168,165,162,159,156,153,150,147,144,141,138,135,131,128,125,122,
		119,116,113,109,106,103,100,97,94,91,88,85,82,79,76,73,71,68,65,62,60,57,54,52,49,47,45,
		42,40,38,35,33,31,29,27,25,23,22,20,18,17,15,14,12,11,10,9,8,6,6,5,4,3,2,2,1,1,1,0,0,0,0
	};
	protected long frameCount=2500;
	protected int _rows;
	protected int _cols;
	protected int _brightnessPercent;
	protected int _speed;
	protected int[][] _r;
	protected int[][] _g;
	protected int[][] _b;
	private IDisplayConfiguration _config;
	private String _configPrefix = "";


	public ColoringPlasma(int cols, int rows, int brightnessPercent) {
		_cols = cols;
		_rows = rows;
		_brightnessPercent = brightnessPercent;
		_speed = 5;
		_r = new int[_cols][_rows];
		_g = new int[_cols][_rows];
		_b = new int[_cols][_rows];
	}
	
	public ColoringPlasma(IDisplayConfiguration config, String configPrefix) {
		_config = config;
		_configPrefix = configPrefix;
	}
	
	public void setSpeed(int speed) {
		_speed = speed;
	}
	
	@Override
	public void apply(ILEDArray leds, IColorableEffect effect) {
		applyConfig();
		nextFrame();
		byte[][] effectData = effect.getEffectData();
		for(int x=0; x<effectData.length; x++) {
			for(int y=0; y<effectData[0].length; y++) {
				if (effectData[x][y] == 1 && x < _cols && x >= 0 && y < _rows && y >= 0)
					leds.setLed(x+effect.getPosX(), y+effect.getPosY(), applyBrightness(_r[x][y]), applyBrightness(_g[x][y]), applyBrightness(_b[x][y]));
			}
		}		
	}

	public void nextFrame() {
		frameCount += _speed; 
		if (frameCount > Long.MAX_VALUE - 2)
			frameCount = 2500;
		
		int t = fastCosineCalc(Math.round((42 * frameCount)/100));  //time displacement - fiddle with these til it looks good...
		int t2 = fastCosineCalc(Math.round((35 * frameCount)/100)); 
		int t3 = fastCosineCalc(Math.round((38 * frameCount)/100));
		
		for (int y = 0; y < _rows; y++) {
			for (int x = 0; x < _cols ; x++) {
			    //Calculate 3 seperate plasma waves, one for each color channel
			    _r[x][y] = fastCosineCalc(((x << 3) + (t >> 1) + fastCosineCalc((t2 + (y << 3)))));
			    _g[x][y] = fastCosineCalc(((y << 3) + t + fastCosineCalc(((t3 >> 2) + (x << 3)))));
			    _b[x][y] = fastCosineCalc(((y << 3) + t2 + fastCosineCalc((t + x + (_g[x][y] >> 2)))));
			}
		}
	}
		
	protected int fastCosineCalc(int preWrapVal)
	{
		int wrapVal = (preWrapVal % 255);
		if (wrapVal<0) wrapVal=255+wrapVal;
		//return (int)Math.round(Math.cos((wrapVal/255)*360)*255);
		return cos_wave[wrapVal]; 
	}
	
	
	protected void applyConfig() {
		if (_config != null) {
			_brightnessPercent = _config.getInt(getConfigKey("brightness"), 100);
			_speed =  _config.getInt(getConfigKey("speed"), 5);
			_cols =  _config.getInt(getConfigKey("cols"), 60);
			_rows =  _config.getInt(getConfigKey("rows"), 16);
			if (_r == null) {
				_r = new int[_cols][_rows];
				_g = new int[_cols][_rows];
				_b = new int[_cols][_rows];
			}
		}
	}
	
	protected int applyBrightness(int val) {
		return val*_brightnessPercent/100;
	}
	
	protected String getConfigKey(String param) {
		return _configPrefix + this.getClass().getName() + "." + param;
	}

}
