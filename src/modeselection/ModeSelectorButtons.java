package modeselection;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import configuration.IDisplayConfiguration;
import input.ButtonFeedbackLed;
import input.ButtonListener;
import input.IButtonListener;

public class ModeSelectorButtons {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private IDisplayConfiguration _config;
	private ModeSelector _modeSelector;

	protected ModeSelectorButtons(ModeSelector modeSelector, IDisplayConfiguration config) {
		_config = config;
		_modeSelector = modeSelector;
	}
	
	public void initNextModeButton() {
		String gpioPinNumber = _config.getString(ModeSelector.MODE_NEXT_GPIOPINNUMBER, "");
		
		if (gpioPinNumber.isEmpty()) {
			LOGGER.severe("No valid \"next mode\" button configured, set " + ModeSelector.MODE_NEXT_GPIOPINNUMBER + " in " + ModeSelector.MODELESECTOR_PROPERTIES + " and restart the application in order to switch modes by a button.");
			return;
		}
		
		LOGGER.info("Next Mode button init on Pin GPIO " + gpioPinNumber);
		IButtonListener nextModeButton = new ButtonListener(gpioPinNumber, true);
		
		nextModeButton.setSingleTriggerCallback(new Callable<Void>() {
			public Void call() throws Exception {
				LOGGER.info("Next mode button pressed short");
				_modeSelector.nextMode();
				ButtonFeedbackLed.getInstance().blinkOnce();
				return null;
			}
		});
		
		nextModeButton.setLongTriggerCallback(new Callable<Void>() {
			public Void call() throws Exception {
				LOGGER.info("Next mode button pressed long");
				_modeSelector.toggleModeScheduler();
				if (_modeSelector.isModeSchedulerStarted())
					ButtonFeedbackLed.getInstance().blinkTwice();
				else
					ButtonFeedbackLed.getInstance().blinkLong();
				return null;
			}
		});
	}
	
	public void initCycleModeConfigurationButton() {
		String gpioPinNumber = _config.getString(ModeSelector.MODE_CYCLECONFIG_GPIOPINNUMBER, "");
		
		if (gpioPinNumber.isEmpty()) {
			LOGGER.severe("No valid \"cycle mode configuration\" button configured, set " + ModeSelector.MODE_CYCLECONFIG_GPIOPINNUMBER + " in " + ModeSelector.MODELESECTOR_PROPERTIES + " and restart the application in order to switch mode config by a button.");
			return;
		}
		
		LOGGER.info("Cycle Mode Configuration button init on Pin GPIO " + gpioPinNumber);
		IButtonListener nextModeConfigButton = new ButtonListener(gpioPinNumber, true);
		nextModeConfigButton.setSingleTriggerCallback(new Callable<Void>() {
			public Void call() throws Exception {
				LOGGER.info("Cycle mode configuration button pressed");
				_modeSelector.nextModeConfig();
				ButtonFeedbackLed.getInstance().blinkOnce();
				return null;
			}
		});
	}
	
	public void initModeInternalButton() {
		String gpioPinNumber = _config.getString(ModeSelector.MODE_INTERNAL_GPIOPINNUMBER, "");
		
		if (gpioPinNumber.isEmpty()) {
			LOGGER.severe("No valid \"mode internal\" button configured, set " + ModeSelector.MODE_INTERNAL_GPIOPINNUMBER + " in " + ModeSelector.MODELESECTOR_PROPERTIES + " and restart the application in order to switch modes by a button.");
			return;
		}
		
		LOGGER.info("Mode internal button init on Pin GPIO " + gpioPinNumber);
		IButtonListener nextModeButton = new ButtonListener(gpioPinNumber, true);
		
		nextModeButton.setSingleTriggerCallback(new Callable<Void>() {
			public Void call() throws Exception {
				LOGGER.info("Mode internal button pressed short");
				_modeSelector._currentMode.buttonPressedShort();
				ButtonFeedbackLed.getInstance().blinkOnce();
				return null;
			}
		});
		
		nextModeButton.setLongTriggerCallback(new Callable<Void>() {
			public Void call() throws Exception {
				LOGGER.info("Mode internal button pressed long");
				_modeSelector._currentMode.buttonPressedLong();
				ButtonFeedbackLed.getInstance().blinkLong();
				return null;
			}
		});
	}
	
}
