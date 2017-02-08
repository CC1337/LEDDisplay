package logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggingConfigurer {
	
	public void readConfigFile(String loggingPropertiesFileName) {
		try {
			File file = new File(loggingPropertiesFileName);
			InputStream inputStream = new FileInputStream(file);
		    LogManager.getLogManager().readConfiguration(inputStream);
		    
		    Logger rootLogger = LogManager.getLogManager().getLogger("");
		    Handler[] handlers = rootLogger.getHandlers();
		    for (Handler h : handlers) {
		        h.setFormatter(new OneLineLogFormatter());
		    }
		    
		} catch (IOException ex)
		{
		    System.err.println("ERROR: Could not open configuration file " + loggingPropertiesFileName);
		    System.err.println("ERROR: Logging not configured!");
		}
	}
}
