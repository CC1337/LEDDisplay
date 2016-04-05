package effects.info;

import java.util.ArrayList;
import java.util.List;

import net.PvData;
import led.ILEDArray;
import effects.ColorableEffectBase;
import effects.IColor;

public class PvDayChartEffect extends ColorableEffectBase {

	private int _width;
	private int _height;
	PvData _pvData = PvData.getInstance();
	
	public PvDayChartEffect(int posX, int posY, int width, int height, IColor color) {
		_posX = posX;
		_posY = posY;
		_width = width;
		_height = height;
		_color = color;
		updateData();
	}
	
	@Override
	public void apply(ILEDArray leds) {
		_color.apply(leds, this);
	}
	
	public void updateData() {
		int[] bars = getBars();
		if (_data == null)
			_data = new byte[_width][_height];
		for (int x=0; x<_width; x++) {
			for (int y=0; y<_height; y++) {
				if (bars[x] >= _height-y)
					_data[x][y] = 1;
				else
					_data[x][y] = 0;
			}
		}
	}
		
	private int[] getBars() {
		int[] data = _pvData.getPacValuesDay();
		List<Integer> pacValues = new ArrayList<Integer>();
		for (int i=0; i<data.length; i++) {
			if (data[i] != 0)
				pacValues.add(data[i]);
		}
		int[] result = new int[_width];
		for (int i=0; i<_width; i++) {
			int dataSlot = (int) Math.floor(((double)pacValues.size() / (double)_width) * (double)i);
			int slotsPerBar = (int) Math.floor(pacValues.size() / _width);
			result[i] = (int) (1 + Math.floor((getMeanValue(pacValues, dataSlot, slotsPerBar) / (double)_pvData.getMaxPossiblePac()) * (_height-1)));
			//System.out.println("i: " + i + "  slot: " + dataSlot + "   val: "+ pacValues.get(dataSlot) + "  res: " + result[i] + "   perbar: " + slotsPerBar);
		}
		return result;
	}

	private double getMeanValue(List<Integer> pacValues, int startIndex, int count) {
		double result = 0;
		for (int i=0; i<count; i++) {
			result += pacValues.get(startIndex+i);
		}
		return result/count;
	}
	
}
