package modes;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.print.DocFlavor.STRING;

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
		_aborted = false;
		_end = false;
		
		//ColoringSolid bgColor = new ColoringSolid(5, 5, 5);
		ColoringPlasma bgColor = new ColoringPlasma(_leds.sizeX(), _leds.sizeY(), 5);
		SolidBackgroundEffect bg = new SolidBackgroundEffect(bgColor); 
		
		IPixelatedFont font = new PixelatedFont(new FontDefault7px());
		ColoringSolid timeTextColor = new ColoringSolid(155, 155, 155);
		//ColoringPlasma timeTextColor = new ColoringPlasma(60, 7);
		ColoringSolid pvTextColor = new ColoringSolid(155, 155, 155);
		//ColoringPlasma pvTextColor = new ColoringPlasma(60, 7);
		TextEffect timetext = new TextEffect(font, timeTextColor, "1337 ALTA!", 7, 0);
		TextEffect pvtext = new TextEffect(font, pvTextColor, "1337 ALTA!", 1, 8);
		String currentTime;
		Calendar calendar = Calendar.getInstance();
		int currentMinute;
		
		int lastPvUpdate = 1337;
		pvtext.setText(getPvText());
		
		while (!_aborted && !_end) {
			currentTime = new SimpleDateFormat("H:mm:ss").format(new Date());
			currentMinute = calendar.get(Calendar.MINUTE);
			timetext.setText(currentTime);
			
			if (currentMinute % 5 == 1 && currentMinute != lastPvUpdate) {
				pvtext.setText(getPvText());
				lastPvUpdate = currentMinute;
			}
			
			//timeTextColor.nextFrame();
			//pvTextColor.nextFrame();
			bgColor.nextFrame();
			
			_leds.applyEffect(bg);
			_leds.applyEffect(timetext);
			_leds.applyEffect(pvtext);
			_display.show(_leds);
			
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				break;
			}
			
		}
		_modeSelector.modeEnded();
		System.out.println("ClockMode exit");
	}

	private String getPvText() {
		String result = "";
		try
		{
		    URL url = new URL("http://lcars/netio/mcsolar.php?giball=1");
		
		    URLConnection urlConn = url.openConnection(); 
		    urlConn.setDoInput(true); 
		    urlConn.setUseCaches(false);
		
		    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream())); 
		    String s = reader.readLine(); 
		    reader.close(); 
		    
		    String[] data = s.split(";");
		    result = String.format("%dW%s%.1f", Integer.parseInt(data[1]), getSpaces(4-data[1].length() + 5-data[2].length()) , Float.parseFloat(data[2]));
	    }
	    catch (MalformedURLException mue) {}
	    catch (IOException ioe) {}
		
		return result;
	}

	private String getSpaces(int count) {
		String result = "";
		for (int i = 0; i < count; i++)
			result += " ";
		return result;
	}

}
