package effects.coloring;

import configuration.IDisplayConfiguration;
import led.ILEDArray;
import effects.IColorableEffect;

public class ColoringWarpcore extends ColoringPlasma {
	
	public ColoringWarpcore(int cols, int rows, int brightnessPercent) {
		super(cols, rows, brightnessPercent);
	}
	
	public ColoringWarpcore(IDisplayConfiguration config, String configPrefix) {
		super(config, configPrefix);
	}

	@Override
	public void apply(ILEDArray leds, IColorableEffect effect) {
		applyConfig();
		nextFrame();
		byte[][] effectData = effect.getEffectData();
		for(int x=0; x<effectData.length; x++) {
			for(int y=0; y<effectData[0].length; y++) {
				if (effectData[x][y] == 1 && x < _cols && x >= 0 && y < _rows && y >= 0)
					leds.setLed(
							x+effect.getPosX(), 
							y+effect.getPosY(), 
							applyBrightness((int)(_r[x][y]*0.2)), 
							applyBrightness((int)((_g[x][y]+_r[x][y])*0.3)), 
							applyBrightness((int)(_b[x][y]+_r[x][y]))
					);
			}
		}		
	}

}
