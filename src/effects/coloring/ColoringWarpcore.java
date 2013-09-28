package effects.coloring;

import led.ILEDArray;
import effects.IColorableEffect;

public class ColoringWarpcore extends ColoringPlasma {
	
	public ColoringWarpcore(int cols, int rows, int brightnessPercent) {
		super(cols, rows, brightnessPercent);
	}

	@Override
	public void apply(ILEDArray leds, IColorableEffect effect) {
		byte[][] effectData = effect.getEffectData();
		for(int x=0; x<effectData.length; x++) {
			for(int y=0; y<effectData[0].length; y++) {
				if (effectData[x][y] == 1 && x < _cols && x >= 0 && y < _rows && y >= 0)
					leds.setLed(x+effect.getPosX(), y+effect.getPosY(), (int)(_r[x][y]*0.2*_brightnessPercent/100), (int)((_g[x][y]+_r[x][y])*0.3*_brightnessPercent/100), (int)((_b[x][y]+_r[x][y])*_brightnessPercent/100));
			}
		}		
	}

}
