package net;

import java.io.IOException;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;
import helper.Helper;

public class PvData {
	
	private static PvData _instance = null;
	private IDisplayConfiguration _config = new DisplayConfiguration("global.properties", false);
	private Calendar _lastDayDataUpdate;
	private String[] _lastDayData;
	private Calendar _lastD0DayDataUpdate;
	private String[] _lastD0DayData;
	 
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
    	
    	int[] result = new int[0];
    	for (String line : _lastDayData) {
			if (line.contains("wr0_pac_vals=[")) {
				result = getIntValueArrayFromLine(line);
				break;
			}
		}
    	return result;
    }
        
    public float getSuppliedKwh() {
    	updateD0DayData();
    	
    	for (String line : _lastD0DayData) {
			if (line.contains("var d0_gkdy=[")) {
				return Float.parseFloat(line.substring(line.indexOf('[') + 1, line.indexOf(']')));
			}
		}
    	return 0;
    }
    
    public float getDrawnKwh() {
    	updateD0DayData();
    	
    	for (String line : _lastD0DayData) {
			if (line.contains("var d0_bkdy=[")) {
				return Float.parseFloat(line.substring(line.indexOf('[') + 1, line.indexOf(']')));
			}
		}
    	return 0;
    }
    
    public int getCurrentD0Pac() {
    	updateD0DayData();
    	
    	for (String line : _lastD0DayData) {
			if (line.contains("var d0_pac=[")) {
				return Integer.parseInt(line.substring(line.indexOf('[') + 1, line.indexOf(']')));
			}
		}
    	return 0;
    }
    
    public int getCurrentSelfConsumption() {
    	for (String line : _lastDayData) {
			if (line.contains("var d0_ev_pac_vals=[")) {
				int[] data = getIntValueArrayFromLine(line);
				return data[data.length-1];
			}
		}
    	return 0;
    }
    
    public int getCurrentOverallConsumption() {
    	return getCurrentSelfConsumption() - getCurrentD0Pac();
    }
  
    public int getMaxPossiblePac() {
    	return _config.getInt("net.pvdata.maxPossiblePac", 8000);
    }
    
    private String[] getStringValueArrayFromLine(String line) {
    	String data = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
    	if (data != null) {
    		return data.split(",");
    	}    
    	return null;
    }
    
    private int[] getIntValueArrayFromLine(String line) {
    	String[] data = getStringValueArrayFromLine(line);
    	int[] result = null;
    	if (data != null) {
    		result = new int[data.length];
    		for(int i=0; i<data.length; i++) {
    			result[i] = Integer.parseInt(data[i]);
    		}
    	}    
    	return result;
    }
    
    private float[] getFloatValueArrayFromLine(String line) {
    	String[] data = getStringValueArrayFromLine(line);
    	float[] result = null;
    	if (data != null) {
    		result = new float[data.length];
    		for(int i=0; i<data.length; i++) {
    			result[i] = Float.parseFloat(data[i]);
    		}
    	}    
    	return result;
    }
    
    private void updateDayData() {
    	if (_lastDayData != null && lastResultValid(_lastDayDataUpdate, 1))
    		return;

		try
		{
			Calendar cal = Calendar.getInstance();
	        URLConnection urlConn = Helper.getUrlConnection(getDayDataUrl(cal.getTime()));
		    if (urlConn == null) {
		    	// Try yesterday
		    	cal.add(Calendar.DAY_OF_MONTH,-1);
		    	urlConn = Helper.getUrlConnection(getDayDataUrl(cal.getTime()));
		    	if (urlConn == null)
		    		throw new IOException("No PV data for today and yesterday :( giving up...");
		    }

		    List<String> resultList = Helper.getFileAsList(urlConn);

		    if (resultList != null && resultList.size() > 0)
		    	_lastDayData = resultList.toArray(new String[0]);
		    else
		    	_lastDayData = new String[0];
		    _lastDayDataUpdate = Calendar.getInstance();
		}
	    catch (IOException exception) {
	    	exception.printStackTrace();
	    }

    }
    
    private void updateD0DayData() {
    	if (_lastD0DayData != null && lastResultValid(_lastD0DayDataUpdate, 1))
    		return;

		try
		{
			Calendar cal = Calendar.getInstance();
	        URLConnection urlConn = Helper.getUrlConnection(getD0DayDataUrl(cal.getTime()));
		    if (urlConn == null) {
		    	// Try yesterday
		    	cal.add(Calendar.DAY_OF_MONTH,-1);
		    	urlConn = Helper.getUrlConnection(getD0DayDataUrl(cal.getTime()));
		    	if (urlConn == null)
		    		throw new IOException("No d0 day data for today and yesterday :( giving up...");
		    }

		    List<String> resultList = Helper.getFileAsList(urlConn);

		    if (resultList != null && resultList.size() > 0)
		    	_lastD0DayData = resultList.toArray(new String[0]);
		    else
		    	_lastD0DayData = new String[0];
		    _lastD0DayDataUpdate = Calendar.getInstance();
		}
	    catch (IOException exception) {
	    	exception.printStackTrace();
	    }

    }
     
    private String getDayDataUrl(Date date) {
    	SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    	return _config.getString("net.pvdata.url") + format.format(date) + ".js";
    }
    
    private String getD0MonthDataUrl(Date date) {
    	SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
    	return _config.getString("net.d0data.url") + "d0_" + format.format(date) + ".js";
    }
    
    private String getD0DayDataUrl(Date date) {
    	SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    	return _config.getString("net.d0data.url") + "d0_" + format.format(date) + ".js";
    }
  
	private boolean lastResultValid(Calendar cacheDate, int cacheTimeout) {
		if (cacheDate == null)
			return false;
		Calendar now = Calendar.getInstance();
		return (Math.abs(now.getTime().getTime() - cacheDate.getTime().getTime()) / (1000*60)) < 5;
	}

}
