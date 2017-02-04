package modeselection;

import java.io.File;
import java.io.FileNotFoundException;

import helper.Helper;
import it.sauronsoftware.cron4j.Scheduler;

public class ModeScheduler {

	private static ModeScheduler __instance;
	private Scheduler _scheduler;
	private File _config;
	private String _configFileName = "modeschedule.txt";

	private ModeScheduler () {
		try {
			System.out.println("Creating ModeScheduler using config file " + _configFileName);
			createScheduler();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ModeScheduler getInstance(IModeSelector modeSelector) {
		if (__instance == null)
			__instance = new ModeScheduler();
		return __instance;
	}
	
	
	private void createScheduler() throws FileNotFoundException {
		if (_scheduler != null)
			return;
		if (!Helper.fileExists(_configFileName))
			throw new FileNotFoundException("ModeSchedulter init aborted, config file not found: " + _configFileName);
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
}
