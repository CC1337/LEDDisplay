package modes;

import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import configuration.CycleableModeConfiguration;
import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;

import effects.IColor;
import effects.IColorableEffect;
import effects.info.PvChartEffect;
import effects.text.*;
import helper.FpsController;
import helper.Helper;
import output.IDisplayAdaptor;
import led.ILEDArray;
import net.PvData;


public class D0InfoMode implements IMode, Observer {

	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private IModeSelector _modeSelector;
	private boolean _aborted = false;
	private boolean _end = false;
	private CycleableModeConfiguration _config;
	private FpsController _fpsController = FpsController.getInstance();
	private PvData _pvData = PvData.getInstance();
	
	private IColor _bgColor = null;
	private IColor _d0ChartColor = null;
	private IColor _evChartColor = null;
	private IColor _pvChartColor = null;
	private IColor _pacTextColor = null;
	private IColor _kwhTextColor = null;
	private IColorableEffect _bg = null;
	private IPixelatedFont _font = new PixelatedFont(new FontDefault7px());
	PvChartEffect _d0Chart = null;
	PvChartEffect _evChart = null;
	PvChartEffect _pvChart = null;
	TextEffect _pacText = null;
	TextEffect _kwhText = null;
	Date _lastUpdate = null;
	int _updateIntervalMinutes = 5;
	
	
	public D0InfoMode(IDisplayAdaptor display, ILEDArray leds, IModeSelector modeSelector) {
		_display = display;
		_leds = leds;
		_modeSelector = modeSelector;
		_config = new CycleableModeConfiguration(modeName().toLowerCase(), true);
		((Observable) _config).addObserver(this);
	}
	
	@Override
	public String modeName() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public void abort() {
		_aborted = true;
		System.out.println(modeName() + " abort() called");
	}

	@Override
	public void end() {
		_end = true;
		System.out.println(modeName() + " end() called");
	}

	@Override
	public void run() {
		_aborted = false;
		_end = false;

		reloadConfig();

		while (!_aborted && !_end) {
			updateData();

			_leds.applyEffect(_bg);			
			_leds.applyEffect(_pvChart);
			_leds.applyEffect(_d0Chart);
			_leds.applyEffect(_evChart);
			_leds.applyEffect(_pacText);
			_leds.applyEffect(_kwhText);
			
			_display.show(_leds);

			_fpsController.waitForNextFrame();
		}
		_modeSelector.modeEnded();
		System.out.println(modeName() + " exit");
	}
	
	private void updateData() {
		Date currentTime = Calendar.getInstance().getTime();
		if (_lastUpdate != null && (Math.abs(currentTime.getTime() - _lastUpdate.getTime()) / (1000*60)) < _updateIntervalMinutes) 
			return;
		updatePacText();
		updateKwhText();
		updateD0Chart();
		updateEvChart();
		updatePvChart();
		_lastUpdate = currentTime;
	}
	
	private void updateD0Chart() {
		_d0Chart.updateData();
	}
	
	private void updateEvChart() {
		_evChart.updateData();
	}
	
	private void updatePvChart() {
		_pvChart.updateData();
	}

	private void updateKwhText() {
		_kwhText.setText(Helper.printWithSpacePrefix(String.valueOf(_pvData.getOverallConsumedKwhDay()), 4));
	}

	private void updatePacText() {
		_pacText.setText(_pvData.getCurrentOverallConsumption() + "W");
	}

	private void reloadConfig() {
		System.out.println(modeName() + " config reload");
		try {
			_updateIntervalMinutes = _config.getInt("updateIntervalMinutes", 5);
			
			String newBgColor = _config.getString("bg.Coloring", "effects.coloring.ColoringSolid");
			String newBgEffect = _config.getString("bg.Effect", "effects.background.SolidBackgroundEffect");
			String newD0ChartColor = _config.getString("d0chart.Coloring", "effects.coloring.ColoringSolid");
			String newEvChartColor = _config.getString("evchart.Coloring", "effects.coloring.ColoringSolid");
			String newPvChartColor = _config.getString("pvchart.Coloring", "effects.coloring.ColoringSolid");
			String newPacTextColor = _config.getString("pactext.Coloring", "effects.coloring.ColoringSolid");
			String newKwhTextColor = _config.getString("kwhtext.Coloring", "effects.coloring.ColoringSolid");

			if (_bgColor == null || !newBgColor.endsWith(_bgColor.getClass().getCanonicalName()))
				_bgColor = (IColor) Class.forName(newBgColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "bg.");
				_bg = (IColorableEffect) Class.forName(newBgEffect).getConstructor(IColor.class).newInstance(_bgColor);
			if (_bg == null || !newBgEffect.endsWith(_bg.getClass().getCanonicalName()))
				_bg = (IColorableEffect) Class.forName(newBgEffect).getConstructor(IColor.class).newInstance(_bgColor); 
			if (_pacTextColor == null || !newPacTextColor.endsWith(_pacTextColor.getClass().getCanonicalName())) {
				_pacTextColor = (IColor) Class.forName(newPacTextColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "pactext.");
				_pacText = new TextEffect(_font, _pacTextColor, _pacText != null ? _pacText.getText() : "", 1, 0);
			}
			if (_kwhTextColor == null || !newKwhTextColor.endsWith(_kwhTextColor.getClass().getCanonicalName())) {
				_kwhTextColor = (IColor) Class.forName(newKwhTextColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "kwhtext.");
				_kwhText = new TextEffect(_font, _kwhTextColor, _kwhText != null ? _kwhText.getText() : "", 37, 0);
			}
			if (_d0ChartColor == null || !newD0ChartColor.endsWith(_d0ChartColor.getClass().getCanonicalName())) {
				_d0ChartColor = (IColor) Class.forName(newD0ChartColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "d0chart.");
				_d0Chart = new PvChartEffect(0, 0, 60, 16, _d0ChartColor, PvChartEffect.RenderData.CONSUMPTION, 60);
			}
			if (_evChartColor == null || !newEvChartColor.endsWith(_evChartColor.getClass().getCanonicalName())) {
				_evChartColor = (IColor) Class.forName(newEvChartColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "evchart.");
				_evChart = new PvChartEffect(0, 0, 60, 16, _evChartColor, PvChartEffect.RenderData.SELFCONSUMPTION, 60);
			}
			if (_pvChartColor == null || !newPvChartColor.endsWith(_pvChartColor.getClass().getCanonicalName())) {
				_pvChartColor = (IColor) Class.forName(newEvChartColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "pvchart.");
				_pvChart = new PvChartEffect(0, 0, 60, 16, _pvChartColor, PvChartEffect.RenderData.PRODUCTION, 60);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable observable, Object arg1) {
		if (observable instanceof IDisplayConfiguration) {
			System.out.println(modeName() + " config updated");
			reloadConfig();		
		}
	}

	@Override
	public void nextConfig() {
		_config.nextConfiguration();
	}
}
