package modes;

import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import effects.background.SolidBackgroundEffect;
import effects.coloring.*;
import helper.FpsController;
import output.IDisplayAdaptor;
import led.ILEDArray;
import modeselection.IModeSelector;

public class SleepMode implements IMode {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private IModeSelector _modeSelector;
	private boolean _aborted = false;
	private boolean _end = false;
	private FpsController _fpsController = FpsController.getInstance();
	
	public SleepMode(IDisplayAdaptor display, ILEDArray leds, IModeSelector modeSelector, String configFileName) {
		_display = display;
		_leds = leds;
		_modeSelector = modeSelector;
	}

	@Override
	public String modeName() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public void abort() {
		_aborted = true;
		LOGGER.fine(modeName() + " abort() called");
	}

	@Override
	public void end() {
		_end = true;
		LOGGER.fine(modeName() + " end() called");
	}

	@Override
	public void run() {
		_aborted = false;
		_end = false;
		
		ColoringWarpcore bgColor = new ColoringWarpcore(_leds.sizeX(), _leds.sizeY(), 50);
		SolidBackgroundEffect bg = new SolidBackgroundEffect(bgColor); 
		
		
		while (!_aborted && !_end) {
			bgColor.nextFrame();
			_leds.applyEffect(bg);
			
			_display.show(_leds);
			
			_fpsController.waitForNextFrame();
		}
		_modeSelector.modeEnded();
		LOGGER.info(modeName() + " exit");
	}

	@Override
	public void changeConfig(String newConfigFileName) {
	}

	@Override
	public void buttonPressedShort() {
	}

	@Override
	public void buttonPressedLong() {
	}
}
