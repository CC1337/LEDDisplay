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
	private String[] _yesterdayD0DayData = new String[0];
	private int _yesterdayD0DayDataFromDayOfWeek = -1;
	 
    public static PvData getInstance() {
        if (_instance == null) {
            _instance = new PvData();
        }
        return _instance;
    }
    

    public int getCurrentPac() {
    	updateDayData();
    	
    	for (String line : _lastDayData) {
			if (line.contains("var wr_pac=[")) {
				return Integer.parseInt(line.substring(line.indexOf('[') + 1, line.indexOf(',')));
			}
		}
    	return 0;
    }
    
    public int[] getPacValuesDay() {
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
 
    public int getCurrentD0Pac() {
    	updateD0DayData();
    	
    	for (String line : _lastD0DayData) {
			if (line.contains("var d0_pac=[")) {
				return Integer.parseInt(line.substring(line.indexOf('[') + 1, line.indexOf(']')));
			}
		}
    	return 0;
    }
    
    /**
     * Returns todays and yesterdays d0 PAC Values up to current dataset, starting at oldest dataset.
     * @return int array of d0 pac values
     */
    public int[] getD0Values() {
    	return getValuesFromD0Data("d0_pac_vals=[");
    }
    
    /**
     * Returns todays and yesterdays self consumption pac Values up to current dataset, starting at oldest dataset.
     * @return int array of self consumption pac values
     */
    public int[] getSelfConsumptionValues() {
    	return getValuesFromD0Data("d0_ev_pac_vals=[");
    }
    
    /**
     * Returns todays and yesterdays overall consumption pac Values up to current dataset, starting at oldest dataset.
     * @return int array of overall consumption pac values
     */
    public int[] getOverallConsumptionValues() {
    	int[] d0Values = getD0Values();
    	int[] selfConsumptionValues = getSelfConsumptionValues();
    	int[] result = new int[d0Values.length];
    	
    	for (int i=0; i< d0Values.length; i++) {
    		result[i] = selfConsumptionValues[i] - (d0Values[i] < 0 ? d0Values[i] : 0);
    	}
    	
    	return result;
    }
    
    private int[] getValuesFromD0Data(String linePrefix) {
    	updateD0DayData();
    	   
    	int[] yesterdayData = new int[0];
    	for (String line : _yesterdayD0DayData) {
			if (line.contains(linePrefix)) {
				yesterdayData = getIntValueArrayFromLine(line);
				break;
			}
		}

    	int[] todayData = new int[0];
    	for (String line : _lastD0DayData) {
			if (line.contains(linePrefix)) {
				todayData = getIntValueArrayFromLine(line);
				break;
			}
		}

    	int[] result = new int[yesterdayData.length + todayData.length];
    	for (int i = 0; i < yesterdayData.length; i++)
    		result[i] = yesterdayData[i];
    	for (int i = yesterdayData.length; i < yesterdayData.length + todayData.length; i++)
    		result[i] = yesterdayData[i - yesterdayData.length];
    	
    	return result;
    }
    
    public int getCurrentSelfConsumption() {
		int[] data = getSelfConsumptionValues();
		return data[data.length-1];
    }
    
    public int getCurrentOverallConsumption() {
    	return getCurrentSelfConsumption() - (getCurrentD0Pac() < 0 ? getCurrentD0Pac() : 0);
    }
    
    public int getMaxPacDay() {
    	int maxValue = 0;
    	
    	for (int value : getPacValuesDay())
    		if (value > maxValue)
    			maxValue = value;
    	
    	return maxValue;
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
    
    public double getMonthExpected() {
    	updateDayData();
    	
    	for (String line : _lastDayData) {
			if (line.contains("var month_expected=[")) {
				return Integer.parseInt(line.substring(line.indexOf('[') + 1, line.indexOf(',')));
			}
		}
    	return 0;
    }
    
    public double getDayExpected() {
    	return getMonthExpected()/Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    
    public double getDayExpectedDiff() {
    	return getKwhDay() - getDayExpected();
    }
       
    public double getSuppliedKwhDay() {
    	updateD0DayData();
    	
    	for (String line : _lastD0DayData) {
			if (line.contains("var d0_gkdy=[")) {
				return Double.parseDouble(line.substring(line.indexOf('[') + 1, line.indexOf(']')));
			}
		}
    	return 0;
    }
    
    public double getSelfConsumedKwhDay() {
    	return getKwhDay() - getSuppliedKwhDay();
    }
    
    public double getDrawnKwhDay() {
    	updateD0DayData();
    	
    	for (String line : _lastD0DayData) {
			if (line.contains("var d0_bkdy=[")) {
				return Double.parseDouble(line.substring(line.indexOf('[') + 1, line.indexOf(']')));
			}
		}
    	return 0;
    }
    
    public double getOverallConsumedKwhDay() {
    	return getDrawnKwhDay() + getSelfConsumedKwhDay();
    }
    
    public int getSelfConsumptionRatioDay() {
    	return (int) Math.round((getSelfConsumedKwhDay()/getKwhDay())*100);
    }
    
    public int getConsumptionPvCoverageDay() {
    	return (int) Math.round((getKwhDay()/getOverallConsumedKwhDay())*100);
    }
    
    public Date getLastPvUpdateTime() {
    	updateDayData();
    	
    	return getLastUpdateTimeFromData(_lastDayData, "Zeit"); 	
    }
    
    public Date getLastD0TodayUpdateTime() {
    	updateDayData();
    	
    	return getLastUpdateTimeFromData(_lastD0DayData, "d0_Zeit");
    }
  
    public Date getLastD0YesterdayUpdateTime() {
    	updateDayData();
    	
    	return getLastUpdateTimeFromData(_yesterdayD0DayData, "d0_Zeit");
    }
    
    private Date getLastUpdateTimeFromData(String[] data, String prefix) {
    	for (String line : data) {
			if (line.contains("var "+ prefix +"='")) {
				String date = line.replace("var "+ prefix +"='", "").replace("';", "");
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
    
    private void updateDayData() {
    	if (_lastDayData != null && lastResultValid(_lastDayDataUpdate, 1))
    		return;

		try
		{
	        URLConnection urlConn = Helper.getUrlConnection(getDayDataUrl(getDateNow()));
		    if (urlConn == null) {
		    	// Try yesterday
		    	urlConn = Helper.getUrlConnection(getDayDataUrl(getDateYesterday()));
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
	        URLConnection urlConn = Helper.getUrlConnection(getD0DayDataUrl(getDateNow()));
		    if (urlConn == null) {
		    	// Try yesterday and reset yesterday data
		    	_yesterdayD0DayData = new String[0];
		    	urlConn = Helper.getUrlConnection(getD0DayDataUrl(getDateYesterday()));
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
		updateYesterdayD0DayData();
    }
    
    private void updateYesterdayD0DayData() {
    	if (_yesterdayD0DayData != null && _yesterdayD0DayData.length > 0 && getCurrentDayOfWeek() != _yesterdayD0DayDataFromDayOfWeek)
    		return;

		try
		{
	        URLConnection urlConn = Helper.getUrlConnection(getD0DayDataUrl(getDateYesterday()));
	    	if (urlConn == null)
	    		throw new IOException("No d0 day data for yesterday :(");

		    List<String> resultList = Helper.getFileAsList(urlConn);

		    if (resultList != null && resultList.size() > 0) {
		    	_yesterdayD0DayData = resultList.toArray(new String[0]); 
			    _yesterdayD0DayDataFromDayOfWeek = getCurrentDayOfWeek();
		    } else {
		    	_yesterdayD0DayData = new String[0];
		    }
		}
	    catch (IOException exception) {
	    	exception.printStackTrace();
	    }

    }

    private int getCurrentDayOfWeek() {
    	return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }
    
    private Date getDateNow() {
    	return Calendar.getInstance().getTime();
    }

    private Date getDateYesterday() {
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DAY_OF_MONTH,-1);
    	return cal.getTime();
    }

    private String getDayDataUrl(Date date) {
    	SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    	return _config.getString("net.pvdata.url") + format.format(date) + ".js";
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
