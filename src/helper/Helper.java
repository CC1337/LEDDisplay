package helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

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
	
	public static URLConnection getUrlConnection(String address) {
		System.out.println("Loading data from " + address + "...");
		
		URLConnection urlConn = null;
		try {
	    	URL url = new URL(address);
		    urlConn = url.openConnection();
		    urlConn.setDoInput(true); 
		    urlConn.setUseCaches(false);

		    if (urlConn instanceof HttpURLConnection)
		    {
		       HttpURLConnection httpConnection = (HttpURLConnection) urlConn;
		       if (httpConnection.getResponseCode() != 200) {
		    	   System.out.println("HTTP Error: " + httpConnection.getResponseCode());
		    	   return null;
		       }
		    }
		    	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return urlConn;
    }


}
