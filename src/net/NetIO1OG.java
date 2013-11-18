package net;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;

public class NetIO1OG {
	
	private static NetIO1OG _instance = null;
	private IDisplayConfiguration _config = new DisplayConfiguration("global.properties", false);
	private Calendar _lastUpdate;
	private String[] _lastData;
	 
    public static NetIO1OG getInstance() {
        if (_instance == null) {
            _instance = new NetIO1OG();
        }
        return _instance;
    }
    
    
    public String getTempAussenKueche() {
    	return getDataByIndex(0);
    }
    
    public String getTempAussenKuecheMin() {
    	return getDataByIndex(1);
    }
    
    public String getTempAussenKuecheMax() {
    	return getDataByIndex(2);
    }
    
    public String getTempBalkon() {
    	return getDataByIndex(3);
    }
    
    public String getTempBalkonMin() {
    	return getDataByIndex(4);
    }
    
    public String getTempBalkonMax() {
    	return getDataByIndex(5);
    }
    
    public String getTempSchlafzimmer() {
    	return getDataByIndex(6);
    }
    
    public String getTempSchlafzimmerMin() {
    	return getDataByIndex(7);
    }
    
    public String getTempSchlafzimmerMax() {
    	return getDataByIndex(8);
    }
    
    public String getTempWohnzimmer() {
    	return getDataByIndex(9);
    }
    
    public String getTempWohnzimmerMin() {
    	return getDataByIndex(10);
    }
    
    public String getTempWohnzimmerMax() {
    	return getDataByIndex(11);
    }
    
    public String getTempAquarium() {
    	return getDataByIndex(12);
    }
    
    public String getTempAquariumMin() {
    	return getDataByIndex(13);
    }
    
    public String getTempAquariumMax() {
    	return getDataByIndex(14);
    }
    
    public String getTempKeller() {
    	return getDataByIndex(15);
    }
    
    public String getTempKellerMin() {
    	return getDataByIndex(16);
    }
    
    public String getTempKellerMax() {
    	return getDataByIndex(17);
    }
    
    public String getTempSchaltschrank() {
    	return getDataByIndex(18);
    }
    
    public String getTempSchaltschrankMin() {
    	return getDataByIndex(19);
    }
    
    public String getTempSchaltschrankMax() {
    	return getDataByIndex(20);
    }
    
    public boolean getStateLichtBad() {
    	return getDataByIndex(21) == "1";
    }

    public boolean getStateLichtGarderobe() {
    	return getDataByIndex(22) == "1";
    }
    
    public boolean getStateLichtFlur() {
    	return getDataByIndex(23) == "1";
    }
    
    public boolean getStateLedWz() {
    	return getDataByIndex(24) == "1";
    }
    
    public boolean getStateLedEz() {
    	return getDataByIndex(25) == "1";
    }
    
    public boolean getStateLedRgb() {
    	return getDataByIndex(26) == "1";
    }
    
    public boolean getStateWarnAq() {
    	return getDataByIndex(27) == "1";
    }
    
    public String getNetioTime() {
    	return getDataByIndex(28);
    }
    
    public String getUptime() {
    	return getDataByIndex(29);
    }
    
    public String getPagecounter() {
    	return getDataByIndex(30);
    }

    private String getDataByIndex(int index) {
    	updateDayData();
    	if (_lastData[0].length() == 0)
    		return "";
    	
    	return _lastData[index];
    }
    
    public int getMaxPossiblePac() {
    	return _config.getInt("net.pvdata.maxPossiblePac", 8000);
    }
    
    private void updateDayData() {
    	if (_lastData != null && lastResultValid(_lastUpdate, 1))
    		return;

		try
		{
	        URLConnection urlConn = getDataConnection();
		   	if (urlConn == null)
		   		throw new IOException("Cannot load data for NETIO 1.OG - giving up...");

		    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream())); 
		    String result = "";
		    String line = reader.readLine();
		    while (line != null) {
		    	result += line.replace("\r", "").replace("\n", "");
		    	line = reader.readLine();
		    }
		    
		    _lastData = result.split(";");
		    _lastUpdate = Calendar.getInstance();
		    System.out.println("ok");
		    
		    reader.close(); 
	    }
	    catch (MalformedURLException mue) {
	    	mue.printStackTrace();
	    }
	    catch (IOException ioe) {
	    	_lastData = new String[0];
		    _lastUpdate = Calendar.getInstance();
	    }
    }
    
    private URLConnection getDataConnection() {
    	String address = _config.getString("net.netio_1og.url");
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
