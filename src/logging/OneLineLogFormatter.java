package logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
 
public class OneLineLogFormatter extends Formatter {
	
	private static final DateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
	private static final String separator = System.getProperty("line.separator");

	public String format(LogRecord record) {
		String loggerName = record.getLoggerName();
		if(loggerName == null) {
			loggerName = "root";
		}
		StringBuilder output = new StringBuilder()
			.append(getStringWithLength(loggerName, 35))
			.append(" ")
			.append(getStringWithLength(Thread.currentThread().getName(), 12))
			.append(" ")
			.append(getStringWithLength(record.getLevel().toString(), 6))
			.append(" ")
			.append(format.format(new Date(record.getMillis())))
			.append(" | ")
			.append(record.getMessage())
			.append(separator);
		return output.toString();		
	}
	
	private String getStringWithLength(String string, int length) {
		while (string.length() < length)
			string += " ";
		return string.substring(Math.max(string.length() - length, 0));
	}
 
}