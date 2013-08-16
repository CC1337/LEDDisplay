import effects.background.*;
import effects.text.*;
import output.*;
import led.*;

public class LEDDisplay implements Runnable {

	public LEDDisplay() {
		
	}
	
	@Override
	public void run() {
		
		IDisplayAdaptor display;
		ISerial serial = null;
				
		if (isWindows()) {
			serial = new SerialFake();
			display = new GuiDisplayAdaptor(new OctoWS2811DisplayAdaptor(serial));
		} else {
			serial = new Serial(115200, "/dev/ttyACM0");
			display = new OctoWS2811DisplayAdaptor(serial, 2);
		}
		
		serial.openSerialPort();
				
		
		LEDArray leds = new LEDArray(60, 16);
		
		SolidBackgroundEffect bg = new SolidBackgroundEffect(); 
		bg.setColor(5, 5, 30);
		
		leds.applyEffect(bg);

		// Flash Test
		for (int i=0; i<10; i++) {
			
			bg.setColor(250, 15, 0);
			leds.applyEffect(bg);
			display.show(leds);
			bg.setColor(0, 0, 0);
			leds.applyEffect(bg);
			display.show(leds);
		}
		
		
		// Pfeil Test
		for (int i=0; i<55; i++) {
			bg.setColor(5, 5, 30);
			leds.applyEffect(bg);
			
			int pos = i%55;	
			leds.setLed(pos, 3, 255, 255, 255);
			leds.setLed(pos+1, 3, 255, 255, 255);
			leds.setLed(pos+2, 3, 255, 255, 255);
			leds.setLed(pos+3, 3, 255, 255, 255);
			leds.setLed(pos+4, 3, 255, 255, 255);
			leds.setLed(pos+3, 2, 255, 255, 255);
			leds.setLed(pos+3, 4, 255, 255, 255);
			leds.setLed(pos+2, 1, 255, 255, 255);
			leds.setLed(pos+2, 5, 255, 255, 255);
			
			display.show(leds);
			//waitms(200);
			
		}

		
		//Text Test
		bg.setColor(0, 30, 0);
		leds.applyEffect(bg);
		IPixelatedFont font = new PixelatedFont(new FontDefault7px());
		TextEffect text = new TextEffect(font, "1337 ALTA!", 1, 1, 80, 80, 140);
		
		for (int i=0; i<60; i++) {
			
			bg.setColor(2*(60-i), 120-i, 60-i);
			leds.applyEffect(bg);
			text.setPosX(61-i);
			leds.applyEffect(text);
			display.show(leds);
		}
		
		waitms(1000);
		
		bg.setColor(1, 1, 1);
		leds.applyEffect(bg);
		text.setR(20);
		text.setG(0);
		text.setB(0);
		leds.applyEffect(text);
		display.show(leds);
		
		
		
		/*
		bg.setColor(1, 1, 1);
		leds.applyEffect(bg);
		display.show(leds);
		*/
		serial.closeSerialPort();
		
		
		if (isWindows()) {
			waitms(2000);
		}
		
	}
	
	public static void main(String[] args)
	{
		Runnable runnable = new LEDDisplay();
		new Thread(runnable).start();
		System.out.println("LEDDisplay starting...");
	}
	
	private boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}
	
	private void waitms(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
