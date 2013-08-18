package modes;

import java.text.SimpleDateFormat;
import java.util.Date;

import effects.background.SolidBackgroundEffect;
import effects.coloring.*;
import effects.text.*;
import output.IDisplayAdaptor;
import led.ILEDArray;

public class ClockMode implements IMode {

	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private IModeSelector _modeSelector;
	private boolean _aborted = false;
	private boolean _end = false;
	
	public ClockMode(IDisplayAdaptor display, ILEDArray leds, IModeSelector modeSelector) {
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
		ColoringSolid bgColor = new ColoringSolid(5, 5, 5);
		SolidBackgroundEffect bg = new SolidBackgroundEffect(bgColor); 
		
		IPixelatedFont font = new PixelatedFont(new FontDefault7px());
		ColoringSolid textColor = new ColoringSolid(80, 0, 0);
		TextEffect text = new TextEffect(font, textColor, "1337 ALTA!", 1, 1);
		String currentTime;
		
		while (!_aborted && !_end) {
			currentTime = new SimpleDateFormat("H:mm:ss").format(new Date());
			text.setText(currentTime);
			
			_leds.applyEffect(bg);
			_leds.applyEffect(text);
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
