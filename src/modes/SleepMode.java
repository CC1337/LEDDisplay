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
		ColoringSolid bgColor = new ColoringSolid(0, 10, 0);
		SolidBackgroundEffect bg = new SolidBackgroundEffect(bgColor); 
		_leds.applyEffect(bg);
		
		while (!_aborted && !_end) {
			
			_display.show(_leds);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
		_modeSelector.modeEnded();
	}

}
