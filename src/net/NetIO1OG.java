package net;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;
import helper.Helper;

public class NetIO1OG {

	private static NetIO1OG _instance = null;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private IDisplayConfiguration _config = new DisplayConfiguration("global.properties", false);
	private Calendar _lastUpdate;
	private String[] _lastData = new String[0];

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
		updateData();
		if (_lastData.length < index + 1)
			return "";

		return _lastData[index];
	}

	public int getMaxPossiblePac() {
		return _config.getInt("net.pvdata.maxPossiblePac", 8000);
	}

	private void updateData() {
		if (_lastData != null && lastResultValid(_lastUpdate, 1))
			return;

		Thread thread = new Thread(() -> {
			tryFetchData();	
		}, "NETIOUpd");
		thread.setDaemon(true);
		// Run synchronous if app start / first run
		if (_lastUpdate == null)
			thread.run();
		else
			thread.start();
	}

	private void tryFetchData() {
		try
		{
			List<String> resultList = Helper.getFileAsList(_config.getString("net.netio_1og.url"));
			if (resultList.size() == 0)
				throw new IOException("Cannot load data for NETIO 1.OG - giving up...");

			String result = "";
			for (String line: resultList) {
				result += line.replace("\r", "").replace("\n", "");
			}

			_lastData = result.split(";");
			_lastUpdate = Calendar.getInstance();
		}
		catch (IOException exception) {
			LOGGER.severe(exception.getMessage());
			exception.printStackTrace();
			_lastData = new String[0];
			_lastUpdate = Calendar.getInstance();
		}
	}

	private boolean lastResultValid(Calendar cacheDate, int cacheTimeout) {
		if (cacheDate == null)
			return false;
		Calendar now = Calendar.getInstance();
		return (Math.abs(now.getTime().getTime() - cacheDate.getTime().getTime()) / (1000*60)) < 5;
	}

}
