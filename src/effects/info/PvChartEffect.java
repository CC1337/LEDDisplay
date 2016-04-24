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

	private boolean _initialized = false;
	private int _width;
	private int _height;
	private RenderData _renderData;
	private int _renderLastNumDataset;
	private boolean _showOnlyPvActiveTime = false;
	private int _skipDatasetsAtStart;
	private int _addDatasetsAtEnd;
	private int _skipDatasetsAtEnd;
	PvData _pvData = PvData.getInstance();
	
	/**
	 * Renders data items covering for the time Pv is active to a chart with given dimensions
	 */
	public PvChartEffect(int posX, int posY, int width, int height, IColor color, RenderData renderData, boolean showOnlyPvActiveTime) {
		this(posX, posY, width, height, color, renderData);
		_showOnlyPvActiveTime = showOnlyPvActiveTime;
	}
	
	/**
	 * Renders last "renderLastNumDataset" data items (or the maximum available if lower) to a chart with given dimensions
	 */
	public PvChartEffect(int posX, int posY, int width, int height, IColor color, RenderData renderData, int renderLastNumDatasets) {
		this(posX, posY, width, height, color, renderData);
		_renderLastNumDataset = renderLastNumDatasets;
	}
	
	/**
	 * Renders all available data to a chart with given dimensions
	 */
	public PvChartEffect(int posX, int posY, int width, int height, IColor color, RenderData renderData) {
		_posX = posX;
		_posY = posY;
		_width = width;
		_height = height;
		_color = color;
		_renderData = renderData;
	}
	
	@Override
	public void apply(ILEDArray leds) {
		if (!_initialized)
			updateData();
		_color.apply(leds, this);
	}
	
	public void updateData() {
		_initialized = true;
		
		updateOffsetsForPvTimes();
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
	
	private void updateOffsetsForPvTimes() {
		if (!_showOnlyPvActiveTime)
			return;
		_skipDatasetsAtStart = _pvData.getD0ToPvDatasetsStartOffset();
		_addDatasetsAtEnd = _pvData.getNowToPvEndTimeDatasetsOffset();
		_skipDatasetsAtEnd = _pvData.getD0ToPvDatasetsEndOffset();
	}

	private int[] getBars() {
		List<Integer> values = getValuesListToRender();
		int[] result = new int[_width];
		int slotsPerBar = (int) Math.floor(values.size() / _width);
		for (int i=0; i<_width; i++) {
			int dataSlot = (int) Math.floor(((double)values.size() / (double)_width) * (double)i);
			result[i] = (int) (Math.ceil((getMeanValue(values, dataSlot, slotsPerBar) / (double)_pvData.getMaxPossiblePac()) * (_height)));
			//System.out.println("i: " + i + "  slot: " + dataSlot + "   val: "+ values.get(dataSlot) + "  mean:"  + getMeanValue(values, dataSlot, slotsPerBar) + "  divided " + (getMeanValue(values, dataSlot, slotsPerBar) / (double)_pvData.getMaxPossiblePac()) + "  result pre-round " + (getMeanValue(values, dataSlot, slotsPerBar) / (double)_pvData.getMaxPossiblePac()) * (_height) + "  res: " + result[i] + "   perbar: " + slotsPerBar);
		}
		return result;
	}
	
	private List<Integer> getValuesListToRender() {
		int[] data = getDataForCurrentRenderMode();
		List<Integer> values = new ArrayList<Integer>();

		int endDataset = data.length + _addDatasetsAtEnd - _skipDatasetsAtEnd;
		int startDataset = _skipDatasetsAtStart;
		if (_renderLastNumDataset != 0)
			startDataset = Math.max(0, endDataset - _renderLastNumDataset);

		for (int i=startDataset; i<endDataset; i++) {
			values.add(i < data.length ? data[i] : 0);
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
