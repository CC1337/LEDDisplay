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
							applyBrightness((int)Math.min((_r[x][y]*0.4), 255)), 
							applyBrightness((int)Math.min(((_g[x][y]*0.7+_r[x][y]*2)*0.3), 255)), 
							applyBrightness((int)Math.min((_b[x][y]*1.5+_r[x][y]*2), 255))
					);
			}
		}		
	}

}
