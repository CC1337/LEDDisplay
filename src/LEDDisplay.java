import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import effects.animation.RandomDotEffect;
import effects.coloring.ColoringImage;
import effects.coloring.ColoringVideo;
import effects.text.*;
import helper.Helper;
import input.ButtonFeedbackLed;
import input.ButtonListener;
import input.IButtonListener;
import output.*;
import led.*;
import logging.LoggingConfigurer;

import modes.LightMode;
import modes.YayMode;
import modeselection.IModeConfigSelector;
import modeselection.IModeSelector;
import modeselection.ModeConfigSelector;
import modeselection.ModeSelector;

public class LEDDisplay implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	public LEDDisplay() {

	}

	@Override
	public void run() {
	
		IDisplayAdaptor display = DisplayAdaptorBuilder.getInstance().getDisplayAdaptor();
		ILEDArray leds = new LEDArray(60, 16);
		

		// Quickfix to compile it
		LightMode.class.getName();
		YayMode.class.getName();
		FontBold10px.class.getName();
		RandomDotEffect.class.getName();
		ButtonListener.class.getName();
		IButtonListener.class.getName();
		ColoringVideo.class.getName();
		ColoringImage.class.getName();

		// Foce-init to set LED low if still on
		ButtonFeedbackLed.getInstance();
		
		
		IModeConfigSelector configSelector = ModeConfigSelector.getInstance();
		final IModeSelector modeSelector = ModeSelector.getInstance(display, leds, configSelector);
		Thread modeSelectorThread = new Thread(modeSelector, "ModeSel");
		modeSelectorThread.start();

		Thread shutdownHook = new ShutdownHook(display, leds, modeSelector);
		Runtime.getRuntime().addShutdownHook(shutdownHook);

		if (Helper.isWindows()) {
			Helper.waitms(2000);
		}
		
	}

	public static void main(String[] args) {
		new LoggingConfigurer().readConfigFile("logging.properties");
		
		Runnable runnable = new LEDDisplay();
		new Thread(runnable, "Display").start();
		LOGGER.info("LEDDisplay starting...");
	}

}
