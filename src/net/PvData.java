package net;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;
import helper.FileHelper;
import helper.Helper;

public class PvData {

	private static PvData _instance = null;
	private IDisplayConfiguration _config = new DisplayConfiguration("global.properties", false);
	private volatile String[] _lastDayData = new String[0];
	private volatile String[] _lastD0DayData = new String[0];
	private volatile String[] _yesterdayD0DayData = new String[0];

	public static synchronized PvData getInstance() {
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

		int[] pacValues = new int[0];
		for (String line : _lastDayData) {
			if (line.contains("wr0_pac_vals=[")) {
				pacValues = getIntValueArrayFromLine(line);
				break;
			}
		}

		int startOffset = getD0ToPvDatasetsStartOffset();
		int endOffset = getD0ToPvDatasetsEndOffset();
		int[] result = new int[pacValues.length + startOffset + endOffset];

		for (int i=0; i<startOffset; i++)
			result[i] = 0;

		for (int i=startOffset; i<startOffset+pacValues.length; i++)
			result[i] = pacValues[i-startOffset];

		for (int i=startOffset+pacValues.length; i<result.length; i++)
			result[i] = 0;

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
			result[i] = (i < selfConsumptionValues.length ? selfConsumptionValues[i] : 0) - (d0Values[i] < 0 ? d0Values[i] : 0);
			//LOGGER.info("overall " + i + ": " + result[i] + "  d0: " + d0Values[i] + "  self: " + selfConsumptionValues[i]);
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
			result[i] = todayData[i - yesterdayData.length];

		return result;
	}

	public int getCurrentSelfConsumption() {
		int[] data = getSelfConsumptionValues();
		return data.length == 0 ? 0 : data[data.length-1];
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

	public Date getPvStartTime() {
		updateDayData();

		return getStartStopTimeFromData(_lastDayData, "t_start");
	}

	public Date getPvEndTime() {
		updateDayData();

		return getStartStopTimeFromData(_lastDayData, "t_start");
	}

	public Date getLastD0TodayUpdateTime() {
		updateD0DayData();

		return getLastUpdateTimeFromData(_lastD0DayData, "d0_Zeit");
	}

	public Date getLastD0YesterdayUpdateTime() {
		updateD0DayData();

		return getLastUpdateTimeFromData(_yesterdayD0DayData, "d0_Zeit");
	}

	public int getD0TodayDatasetsCount() {
		return getNumDatasetsForTimeframe(Helper.getMillisecondsSinceMidnight());
	}

	public int getD0ToPvDatasetsEndOffset() {
		if (getLastD0TodayUpdateTime() == null || getLastPvUpdateTime() == null)
			return 0;

		long offsetTime = getLastD0TodayUpdateTime().getTime() - getLastPvUpdateTime().getTime();

		return getNumDatasetsForTimeframe(offsetTime);
	}

	public int getD0ToPvDatasetsStartOffset() {
		if (getPvStartTime() == null || getDateNow() == null)
			return 0;

		long offsetTime = Math.min(
				Helper.getMillisecondsSinceMidnight(getPvStartTime().getTime()), 
				Helper.getMillisecondsSinceMidnight(getDateNow().getTime())
				);

		return getNumDatasetsForTimeframe(offsetTime);
	}

	public int getNowToPvEndTimeDatasetsOffset() {
		if (getPvEndTime() == null || getDateNow() == null)
			return 0;

		long offsetTime = getPvEndTime().getTime() - getDateNow().getTime();

		return getNumDatasetsForTimeframe(offsetTime);
	}

	private int getNumDatasetsForTimeframe(long milliseconds) {
		if (milliseconds < 0)
			return 0;
		return Math.round(milliseconds / (1000 * 60 * 5));
	}

	private Date getLastUpdateTimeFromData(String[] data, String prefix) {
		for (String line : data) {
			if (line.contains("var "+ prefix +"='")) {
				String date = line.replace("var "+ prefix +"='", "").replace("';", "");
				DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
				try {
					return df.parse(date);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private Date getStartStopTimeFromData(String[] data, String prefix) {
		for (String line : data) {
			if (line.contains("var "+ prefix +"='")) {
				String date = line.replace("var "+ prefix +"='", "").replace("';", "");
				date = getFormattedDatafileDate(getDateNow()) + " " + date;
				DateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm");
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

	private final java.util.concurrent.atomic.AtomicBoolean _loading = new java.util.concurrent.atomic.AtomicBoolean();
	private volatile long _nextAttempt;
	private long _retryDelay = 10000;
	private int _yesterdayLoadedOn = -1;

	private void updateDayData() { updateData(); }
	private void updateD0DayData() { updateData(); }

	private void updateData() {
		if (System.nanoTime() < _nextAttempt || !_loading.compareAndSet(false, true)) return;
		if (System.nanoTime() < _nextAttempt) {
			_loading.set(false);
			return;
		}
		Thread worker = new Thread(() -> {
			boolean success = true;
			try {
				String[] data = fetch(getDayDataUrl(getDateNow()));
				if (data.length == 0) data = fetch(getDayDataUrl(getDateYesterday()));
				if (data.length > 0) _lastDayData = data; else success = false;
				data = fetch(getD0DayDataUrl(getDateNow()));
				if (data.length == 0) data = fetch(getD0DayDataUrl(getDateYesterday()));
				if (data.length > 0) _lastD0DayData = data; else success = false;
				int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
				if (_yesterdayLoadedOn != today) {
					data = fetch(getD0DayDataUrl(getDateYesterday()));
					if (data.length > 0) {
						_yesterdayD0DayData = data;
						_yesterdayLoadedOn = today;
					} else success = false;
				}
			} catch (Exception exception) {
				success = false;
				java.util.logging.Logger.getLogger(PvData.class.getName()).warning("PV refresh skipped: " + exception.getMessage());
			} finally {
				_retryDelay = success ? 10000 : Math.min(60000, _retryDelay * 2);
				_nextAttempt = System.nanoTime() + _retryDelay * 1000000L;
				_loading.set(false);
			}
		}, "PVDataUpd");
		worker.setDaemon(true);
		worker.start();
	}

	private String[] fetch(String address) {
		List<String> result = FileHelper.getFileAsList(address);
		return result.toArray(new String[0]);
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
		return _config.getString("net.pvdata.url") + getFormattedDatafileDate(date) + ".js";
	}

	private String getD0DayDataUrl(Date date) {
		return _config.getString("net.d0data.url") + "d0_" + getFormattedDatafileDate(date) + ".js";
	}

	private String getFormattedDatafileDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		return format.format(date);
	}

}
