package modes;

import java.lang.invoke.MethodHandles;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

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
import modeselection.IModeSelector;
import net.PvData;


public class PvInfoMode implements IMode, Observer {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private IModeSelector _modeSelector;
	private boolean _aborted = false;
	private boolean _end = false;
	private IDisplayConfiguration _config;
	private FpsController _fpsController = FpsController.getInstance();
	private PvData _pvData = PvData.getInstance();
	
	private IColor _bgColor = null;
	private IColor _pvDayChartColor = null;
	private IColor _evDayChartColor = null;
	private IColor _pacTextColor = null;
	private IColor _kwhTextColor = null;
	private IColorableEffect _bg = null;
	private IPixelatedFont _font = new PixelatedFont(new FontDefault7px());
	PvChartEffect _pvDayChart = null;
	PvChartEffect _evDayChart = null;
	TextEffect _pacText = null;
	TextEffect _kwhText = null;
	Date _lastUpdate = null;
	int _updateIntervalMinutes = 5;
	
	
	public PvInfoMode(IDisplayAdaptor display, ILEDArray leds, IModeSelector modeSelector, String configFileName) {
		_display = display;
		_leds = leds;
		_modeSelector = modeSelector;
		_config = new DisplayConfiguration(configFileName, true);
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
			_leds.applyEffect(_pacText);
			_leds.applyEffect(_kwhText);
			_leds.applyEffect(_pvDayChart);
			_leds.applyEffect(_evDayChart);
			
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
		updatePvDayChart();
		updateEvDayChart();
		_lastUpdate = currentTime;
	}
	
	private void updatePvDayChart() {
		_pvDayChart.updateData();
	}
	
	private void updateEvDayChart() {
		_evDayChart.updateData();
	}

	private void updateKwhText() {
		_kwhText.setText(Helper.printWithSpacePrefix(String.valueOf(_pvData.getKwhDay()), 4));
	}

	private void updatePacText() {
		_pacText.setText(_pvData.getCurrentPac() + "W");
	}

	private void reloadConfig() {
		System.out.println(modeName() + " config reload");
		try {
			_updateIntervalMinutes = _config.getInt("updateIntervalMinutes", 5);
			
			String newBgColor = _config.getString("bg.Coloring", "effects.coloring.ColoringSolid");
			String newBgEffect = _config.getString("bg.Effect", "effects.background.SolidBackgroundEffect");
			String newPvDayChartColor = _config.getString("pvdaychart.Coloring", "effects.coloring.ColoringSolid");
			String newEvDayChartColor = _config.getString("evdaychart.Coloring", "effects.coloring.ColoringSolid");
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
			if (_pvDayChartColor == null || !newPvDayChartColor.endsWith(_pvDayChartColor.getClass().getCanonicalName())) {
				_pvDayChartColor = (IColor) Class.forName(newPvDayChartColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "pvdaychart.");
				_pvDayChart = new PvChartEffect(0, 0, 60, 16, _pvDayChartColor, PvChartEffect.RenderData.PRODUCTION, true);
			}
			if (_evDayChartColor == null || !newEvDayChartColor.endsWith(_evDayChartColor.getClass().getCanonicalName())) {
				_evDayChartColor = (IColor) Class.forName(newEvDayChartColor).getConstructor(IDisplayConfiguration.class, String.class).newInstance(_config, "evdaychart.");
				_evDayChart = new PvChartEffect(0, 0, 60, 16, _evDayChartColor, PvChartEffect.RenderData.SELFCONSUMPTION, true);
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
	public void changeConfig(String newConfigFileName) {
		_config.changeConfigFile(newConfigFileName);
	}
}
