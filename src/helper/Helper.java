package helper;

import java.util.Calendar;

public final class Helper {
	
	private static final boolean IsWindows = System.getProperty("os.name").startsWith("Windows");

	private Helper() {
		
	}

	public static boolean isWindows() {
		return IsWindows;
	}
	
	public static int getArrayAverage(int[] array) {
		int sum = 0;
		for(int i=0; i < array.length ; i++)
			sum += array[i];
		return sum/array.length;
	}
	
	public static String getSpaces(int count) {
		String result = "";
		for (int i = 0; i < count; i++)
			result += " ";
		return result;
	}

	public static void waitms(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static String printWithSpacePrefix(String text, int targetLength) {
    	if (text == null)
    		text= "";
    	while (text.length() < targetLength)
    		text = " " + text;
		return text;
	}
    
    public static String printWithSpacePrefix(double value, int targetLength) {
    	return printWithSpacePrefix(String.valueOf(value), targetLength);
    }

    public static String printWithSpacePrefix(int value, int targetLength) {
    	return printWithSpacePrefix(String.valueOf(value), targetLength);
    }

	public static long getMillisecondsSinceMidnight() {
		return getMillisecondsSinceMidnight(Calendar.getInstance().getTimeInMillis());
	}
	
	public static long getMillisecondsSinceMidnight(long TimeInMilliseconds) {
		Calendar rightNow = Calendar.getInstance();

		// offset to add since we're not UTC
		long offset = rightNow.get(Calendar.ZONE_OFFSET) +
		    rightNow.get(Calendar.DST_OFFSET);
		return (TimeInMilliseconds + offset) % (24 * 60 * 60 * 1000);
	}
}
