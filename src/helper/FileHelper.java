package helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class FileHelper {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private FileHelper() {
		
	}

	public static List<String> getFileAsList(URLConnection urlConn) {
		List<String> list = new ArrayList<String>();
		try { 
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream())); 
			String line = reader.readLine();
			while (line != null) {
				list.add(line);
				line = reader.readLine();
			}
			reader.close();
		}
	    catch (MalformedURLException exception) {
	    	exception.printStackTrace();
	    }
	    catch (IOException exception) {
	    	exception.printStackTrace();
	    }
	
	    return list;
	}

	public static List<String> getFileAsList(String address) {
		return getFileAsList(FileHelper.getUrlConnection(address));
	}

	public static boolean fileExists(String fileName) {
		File f = new File(fileName);
		return f.exists() && !f.isDirectory();
	}

	public static URLConnection getUrlConnection(String address) {
		LOGGER.info("Loading data from " + address + "...");
		
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
		    	   LOGGER.info("HTTP Error: " + httpConnection.getResponseCode());
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
