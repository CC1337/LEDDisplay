package net;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sun.security.krb5.Config;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;

public class PvData {
	
	private static PvData _instance = null;
	private IDisplayConfiguration _config = new DisplayConfiguration("global.properties", false);
	private Calendar _lastDayDataUpdate;
	private String[] _lastDayData;
	 
    public static PvData getInstance() {
        if (_instance == null) {
            _instance = new PvData();
        }
        return _instance;
    }
    
    
    public int getPac() {
    	updateDayData();
    	
    	for (String line : _lastDayData) {
			if (line.contains("var wr_pac=[")) {
				return Integer.parseInt(line.substring(line.indexOf('[') + 1, line.indexOf(',')));
			}
		}
    	return 0;
    }
    
    public int getMaxPac() {
    	updateDayData();
    	
    	for (String line : _lastDayData) {
			if (line.contains("var wr_pac_max=[")) {
				return Integer.parseInt(line.substring(line.indexOf('[') + 1, line.indexOf(',')));
			}
		}
    	return 0;
    }
    
    public Date getLastUpdateTime() {
    	updateDayData();
    	
    	for (String line : _lastDayData) {
			if (line.contains("var Zeit='")) {
				String date = line.replace("var Zeit='", "").replace("';", "");
				DateFormat df = new SimpleDateFormat("dd.mm.yyyy hh:mm:ss");
				try {
					return df.parse(date);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
    	return null;   	
    }
    
    public double getKwhDay() {
    	updateDayData();
    	
    	for (String line : _lastDayData) {
			if (line.contains("var wr_kdy=[")) {
				return Double.parseDouble(line.substring(line.indexOf('[') + 1, line.indexOf(',')));
			}
		}
    	return 0;
    }
    
    public int getMonthExpected() {
    	updateDayData();
    	
    	for (String line : _lastDayData) {
			if (line.contains("var month_expected=[")) {
				return Integer.parseInt(line.substring(line.indexOf('[') + 1, line.indexOf(',')));
			}
		}
    	return 0;
    }
        
    public int[] getPacValues() {
    	updateDayData();
    	
    	String data = null;
    	int[] result = new int[0];
    	for (String line : _lastDayData) {
			if (line.contains("wr0_pac_vals=[")) {
				data = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
				break;
			}
		}
    	if (data != null) {
    		String[] tmp = data.split(",");
    		result = new int[tmp.length];
    		for(int i=0; i<tmp.length; i++) {
    			result[i] = Integer.parseInt(tmp[i]);
    		}
    	}    	
    	return result;
    }
    
    public int getMaxPossiblePac() {
    	return _config.getInt("net.pvdata.maxPossiblePac", 8000);
    }
    
    private void updateDayData() {
    	if (_lastDayData != null && lastResultValid(_lastDayDataUpdate, 1))
    		return;

		try
		{
			Calendar cal = Calendar.getInstance();
	        URLConnection urlConn = getDayDataConnection(cal.getTime());
		    if (urlConn == null) {
		    	// Try yesterday
		    	cal.add(Calendar.DAY_OF_MONTH,-1);
		    	urlConn = getDayDataConnection(cal.getTime());
		    	if (urlConn == null)
		    		throw new IOException("No PV Data for today and yesterday :( giving up...");
		    }

		    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream())); 
		    List<String> list = new ArrayList<String>();
		    String line = reader.readLine();
		    while (line != null) {
		    	list.add(line);
		    	line = reader.readLine();
		    }
		    
		    _lastDayData = list.toArray(new String[0]);
		    _lastDayDataUpdate = Calendar.getInstance();
		    System.out.println("ok");
		    
		    reader.close(); 
	    }
	    catch (MalformedURLException mue) {
	    	mue.printStackTrace();
	    }
	    catch (IOException ioe) {
	    	_lastDayData = new String[0];
		    _lastDayDataUpdate = Calendar.getInstance();
	    }
    }
    
    private URLConnection getDayDataConnection(Date date) {
    	SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    	String address = _config.getString("net.pvdata.url") + format.format(date) + ".js";
		System.out.print("Loading data from " + address + "...");
		
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

	private boolean lastResultValid(Calendar cacheDate, int cacheTimeout) {
		if (cacheDate == null)
			return false;
		Calendar now = Calendar.getInstance();
		return (Math.abs(now.getTime().getTime() - cacheDate.getTime().getTime()) / (1000*60)) < 5;
	}

}
