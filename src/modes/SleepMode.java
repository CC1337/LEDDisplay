package modes;

import java.text.SimpleDateFormat;
import java.util.Date;

import effects.background.SolidBackgroundEffect;
import effects.coloring.*;
import effects.text.*;
import output.IDisplayAdaptor;
import led.ILEDArray;

public class SleepMode implements IMode {

	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private IModeSelector _modeSelector;
	private boolean _aborted = false;
	private boolean _end = false;
	
	public SleepMode(IDisplayAdaptor display, ILEDArray leds, IModeSelector modeSelector) {
		_display = display;
		_leds = leds;
		_modeSelector = modeSelector;
	}
	
	@Override
	public void abort() {
		_aborted = true;
	}

	@Override
	public void end() {
		_end = true;
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
			/*
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				break;
			}
			*/
		}
		_modeSelector.modeEnded();
		System.out.println("SleepMode exit");
	}

}
