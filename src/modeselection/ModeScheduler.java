package modeselection;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;

import helper.Helper;
import it.sauronsoftware.cron4j.Scheduler;

public class ModeScheduler {

	private static ModeScheduler __instance;
	private static String __configFileName = "modeschedule.txt";
	private static String __lastMode = null;
	private IModeSelector _modeSelector;
	private Scheduler _scheduler;
	private File _config;

	private ModeScheduler (IModeSelector modeSelector) {
		_modeSelector = modeSelector;
		try {
			System.out.println("Creating ModeScheduler using config file " + __configFileName);
			System.out.println(this.getClass().getName());
			createScheduler();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ModeScheduler getInstance(IModeSelector modeSelector) {
		if (__instance == null)
			__instance = new ModeScheduler(modeSelector);
		return __instance;
	}
	
	
	private void createScheduler() throws FileNotFoundException {
		if (_scheduler != null)
			return;
		if (!Helper.fileExists(__configFileName))
			throw new FileNotFoundException("ModeScheduler init aborted, config file not found: " + __configFileName);
		_config = new File("modeschedule.txt");
		_scheduler = new Scheduler();
		_scheduler.setDaemon(true);
		_scheduler.scheduleFile(_config);
	}
	
	public void start() {
		_scheduler.start();
		System.out.println("ModeScheduler started");
	}
	
	public void stop() {
		_scheduler.stop();
		System.out.println("ModeScheduler stopped");
	}
	
	public static void setMode(String[] args) {
		String nextMode = null;
		boolean saveCurrentMode = true;
		int secondsDelay = 0;
		
		if (args.length >= 1)
			nextMode = args[0];
		
		if (args.length >= 2)
			saveCurrentMode = Boolean.valueOf(args[1]);
		
		if (args.length >= 3)
			secondsDelay = parseDelay(args[2]);
		
		if (!isValidMode(nextMode)) {
			System.err.println("ModeScheduler can't start new mode because it's not valid: " + nextMode);
			System.err.println("Please specify full class name like modes.SampleMode in correct casing.");
		}
		
		System.out.println("ModeScheduler Task: setMode " + nextMode + " / save current mode: " + saveCurrentMode + " / delay: " + secondsDelay + " seconds");
		
		delayedSetMode(nextMode, saveCurrentMode, secondsDelay);
	}
	
	private static void delayedSetMode(String nextMode, boolean saveCurrentMode, int secondsDelay) {
		try {
			new Timer(true).schedule(__instance.new ModeSchedulerSetModeTimerTask(nextMode, saveCurrentMode), secondsDelay * 1000);
		} 
		catch (java.lang.IllegalStateException e) {}
	}
	
	public static void restorePreviousMode(String[] args) {
		System.out.println("ModeScheduler Task: restorePreviousMode");
		if (__lastMode != null) {
			startModeAtModeSelector(__lastMode);
			__lastMode = null;
		} else
			System.out.println("ModeScheduler skipped restorePreviousMode because there is no previous mode saved or it has been restored already.");
	}
	
	private static boolean isValidMode(String modeName) {
		return __instance != null && __instance._modeSelector.modeExists(modeName);
	}
	
	private static String getCurrentMode() {
		if (__instance == null)
			return null;
		return __instance._modeSelector.getCurrentMode();
	}
	
	private static void startModeAtModeSelector(String modeName) {
		if (__instance == null)
			return;
		__instance._modeSelector.startMode(modeName);
	}
	
	private static int parseDelay(String delay) {
		int secondsDelay = 0;
		try {
			secondsDelay = Integer.valueOf(delay);
		} catch (NumberFormatException e) {
			System.err.println("Invalid delay (must be int) in " + __configFileName + ": " + delay);
			secondsDelay = 0;
		}
		return secondsDelay;
	}
	
	public class ModeSchedulerSetModeTimerTask extends TimerTask {

		private String _nextMode;
		private boolean _saveCurrentMode;

		public ModeSchedulerSetModeTimerTask(String nextMode, boolean saveCurrentMode) {
			_nextMode = nextMode;
			_saveCurrentMode = saveCurrentMode;
		}
		
		@Override
		public void run() {
			String currentMode = getCurrentMode();
			if (_saveCurrentMode && currentMode != null)
				__lastMode = currentMode;
			
			startModeAtModeSelector(_nextMode);
		}
	}
}
