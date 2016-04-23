package effects.info;

import java.util.ArrayList;

import java.util.List;

import net.PvData;
import led.ILEDArray;
import effects.ColorableEffectBase;
import effects.IColor;

public class PvChartEffect extends ColorableEffectBase {
	
	public static enum RenderData {
		PRODUCTION,
		SELFCONSUMPTION,
		CONSUMPTION
	}

	private int _width;
	private int _height;
	private RenderData _renderData;
	private int _renderLastNumDataset;
	private int _dataSetOffset;
	PvData _pvData = PvData.getInstance();
	
	/**
	 * Renders last "renderLastNumDataset" data items (or the maximum available if lower) skipping the last num 'dataSetOffset' to a chart with given dimensions
	 */
	public PvChartEffect(int posX, int posY, int width, int height, IColor color, RenderData renderData, int renderLastNumDataset, int dataSetOffset) {
		_posX = posX;
		_posY = posY;
		_width = width;
		_height = height;
		_color = color;
		_renderData = renderData;
		_renderLastNumDataset = renderLastNumDataset;
		_dataSetOffset = dataSetOffset;
		updateData();
	}
	
	/**
	 * Renders all available data to a chart with given dimensions
	 */
	public PvChartEffect(int posX, int posY, int width, int height, IColor color, RenderData renderData) {
		this(posX, posY, width, height, color, renderData, 0, 0);
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
		List<Integer> values = getValuesListToRender();
		int[] result = new int[_width];
		int slotsPerBar = (int) Math.floor(values.size() / _width);
		for (int i=0; i<_width; i++) {
			int dataSlot = (int) Math.floor(((double)values.size() / (double)_width) * (double)i);
			result[i] = (int) (Math.floor((getMeanValue(values, dataSlot, slotsPerBar) / (double)_pvData.getMaxPossiblePac()) * (_height)));
			//System.out.println("i: " + i + "  slot: " + dataSlot + "   val: "+ values.get(dataSlot) + "  res: " + result[i] + "   perbar: " + slotsPerBar);
		}
		return result;
	}
	
	private List<Integer> getValuesListToRender() {
		int[] data = getDataForCurrentRenderMode();
		List<Integer> values = new ArrayList<Integer>();

		int endDataset = data.length - _dataSetOffset;
		int startDataset = 0;
		if (_renderLastNumDataset != 0)
			startDataset = Math.max(0, endDataset - _renderLastNumDataset);

		for (int i=startDataset; i<endDataset; i++) {
			values.add(data[i]);
		}

		return values;
	}
	
	private int[] getDataForCurrentRenderMode() {
		switch (_renderData) {
			case CONSUMPTION:
				return _pvData.getOverallConsumptionValues();
			case SELFCONSUMPTION:
				return _pvData.getSelfConsumptionValues();
			case PRODUCTION:
			default:
				return _pvData.getPacValuesDay();
		}
	}

	private double getMeanValue(List<Integer> pacValues, int startIndex, int count) {
		double result = 0;
		for (int i=0; i<count; i++) {
			result += pacValues.get(startIndex+i);
		}
		return result/count;
	}
	
}
