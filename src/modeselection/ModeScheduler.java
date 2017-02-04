package modeselection;

import java.io.File;
import java.io.FileNotFoundException;

import helper.Helper;
import it.sauronsoftware.cron4j.Scheduler;

public class ModeScheduler {

	private Scheduler _scheduler;
	private File _config;
	private String _configFileName = "modeschedule.txt";

	public ModeScheduler (IModeSelector modeSelector) {
		try {
			createScheduler();
			startScheduler();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	private void startScheduler() {
		_scheduler.start();
	}
	
}
