package output;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;
import helper.Helper;

public class DisplayAdaptorBuilder implements IDisplayAdaptorBuilder {

	private static IDisplayAdaptorBuilder _instance;
	private IDisplayAdaptor _display;
	private IDisplayConfiguration _config = new DisplayConfiguration("global.properties", false);

	private DisplayAdaptorBuilder() {
		ISerial serial = null;
		
		if (Helper.isWindows()) {
			serial = new SerialFake();
			_display = new GuiDisplayAdaptor(new OctoWS2811DisplayAdaptor(serial));
		} else {
			serial = new Serial(
					_config.getInt("output.serialBaud", 115200), 
					_config.getString("output.serialPort1", "/dev/ttyACM0"), 
					_config.getString("output.serialPort2", "/dev/ttyACM1")
					);
			_display = new OctoWS2811DisplayAdaptor(serial, 2);
		}
	}
	
	public static IDisplayAdaptorBuilder getInstance() {
		if (_instance == null) {
			_instance = new DisplayAdaptorBuilder();
		}
		return DisplayAdaptorBuilder._instance;
	}

	@Override
	public IDisplayAdaptor getDisplayAdaptor() {
		return _display;
	}

}
