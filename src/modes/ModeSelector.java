package modes;

import java.util.HashMap;

import led.ILEDArray;
import output.IDisplayAdaptor;

public class ModeSelector implements IModeSelector {

	private IDisplayAdaptor _display;
	private ILEDArray _leds;
	private HashMap<String, IMode> _modes = new HashMap<String, IMode>();
	
	public ModeSelector(IDisplayAdaptor display, ILEDArray leds) {
		_display = display;
		_leds = leds;
		_modes.put("ClockMode", new ClockMode(_display, _leds, this));
	}
	
	@Override
	public void modeCheck() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void modeEnded() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		new Thread(_modes.get("ClockMode")).start();
	}

}
