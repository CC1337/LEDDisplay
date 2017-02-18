package helper;

import java.lang.invoke.MethodHandles;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

public class DebouncedFileModifiedWatch implements JNotifyListener {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private int _fileWatchId = -1;
	private Timer debouncedFileModifiedTimer = new Timer(true);
	private String _filePath;
	private IDebounceFileWatchListener _listener;
	private int _debounceMs = 50;
	
	public DebouncedFileModifiedWatch(String filePath, IDebounceFileWatchListener listener) {
		_filePath = filePath;
		_listener = listener;
		addFileWatch();
	}
	
	public DebouncedFileModifiedWatch(String filePath, IDebounceFileWatchListener listener, int debounceMs) {
		this(filePath, listener);
		_debounceMs = debounceMs;
	}

	private void addFileWatch() {
		if (_fileWatchId >= 0)
			return;
		try {
			_fileWatchId = JNotify.addWatch(_filePath, JNotify.FILE_MODIFIED, false, this);
			LOGGER.fine("New DebouncedFileModifiedWatch: " + _filePath + " / ID: " + _fileWatchId);
		} catch (JNotifyException e) {
			e.printStackTrace();
		}
	}
	
	private void removeFileWatch() {
		if (_fileWatchId <= 0)
			return;
		try {
			JNotify.removeWatch(_fileWatchId);
			_fileWatchId = -1;
		} catch (JNotifyException e) {
			e.printStackTrace();
		}
	}
	
	public void changeFile(String filePath) {
		removeFileWatch();
		_filePath = filePath;
		addFileWatch();
	}
	
	public void destroy() {
		removeFileWatch();
	}

	@Override
	public void fileCreated(int wd, String rootPath, String name) {
	}

	@Override
	public void fileDeleted(int wd, String rootPath, String name) {
	}

	@Override
	public void fileModified(int wd, String rootPath, String name) {
		if (rootPath.endsWith(_filePath)) {
			debouncedFileModified();
		}
	}
	
	private void debouncedFileModified() {
		try {
			debouncedFileModifiedTimer.cancel();
			debouncedFileModifiedTimer = new Timer(true);
			debouncedFileModifiedTimer.schedule(new TimerTask() {
				public void run() {
					LOGGER.info(_filePath + " changed.");
					_listener.fileChanged(_filePath);
					debouncedFileModifiedTimer.cancel();
				}
			}, _debounceMs);
		} 
		catch (java.lang.IllegalStateException e) {}
	}

	@Override
	public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
	}

}
