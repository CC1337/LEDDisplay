package net;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;

public class NetIO1OG {

	private static NetIO1OG _instance = null;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private IDisplayConfiguration _config = new DisplayConfiguration("global.properties", false);
	private volatile String[] _lastData = new String[0];
	private final AtomicBoolean _loading = new AtomicBoolean();
	private volatile long _nextAttempt;
	private long _retryDelay = 10000;

	public static synchronized NetIO1OG getInstance() {
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
		return "1".equals(getDataByIndex(21));
	}

	public boolean getStateLichtGarderobe() {
		return "1".equals(getDataByIndex(22));
	}

	public boolean getStateLichtFlur() {
		return "1".equals(getDataByIndex(23));
	}

	public boolean getStateLedWz() {
		return "1".equals(getDataByIndex(24));
	}

	public boolean getStateLedEz() {
		return "1".equals(getDataByIndex(25));
	}

	public boolean getStateLedRgb() {
		return "1".equals(getDataByIndex(26));
	}

	public boolean getStateWarnAq() {
		return "1".equals(getDataByIndex(27));
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
		String[] data = _lastData;
		if (data.length < index + 1)
			return "";

		return data[index];
	}

	public int getMaxPossiblePac() {
		return _config.getInt("net.pvdata.maxPossiblePac", 8000);
	}

	/** One snapshot for both values, never fetches on the render thread. */
	public BatteryReading getBatteryReading() {
		updateData();
		return BatteryReading.parse(_lastData, System.currentTimeMillis() / 1000);
	}

	private void updateData() {
		if (System.nanoTime() < _nextAttempt || !_loading.compareAndSet(false, true))
			return;
		if (System.nanoTime() < _nextAttempt) {
			_loading.set(false);
			return;
		}
		Thread thread = new Thread(() -> {
			try {
				URLConnection connection = new URL(_config.getString("net.netio_1og.url")).openConnection();
				connection.setConnectTimeout(2000);
				connection.setReadTimeout(2000);
				connection.setUseCaches(false);
				StringBuilder text = new StringBuilder();
				long deadline = System.nanoTime() + 5000000000L;
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
					int ch;
					while ((ch = reader.read()) != -1) {
						if (System.nanoTime() > deadline || text.length() >= 65536)
							throw new IOException("NETIO response too slow or too large");
						if (ch != '\r' && ch != '\n') text.append((char) ch);
					}
				}
				String[] data = text.toString().split(";", -1);
				if (data.length < 31) throw new IOException("Incomplete NETIO response");
				_lastData = data;
				_retryDelay = 10000;
			} catch (Exception exception) {
				LOGGER.warning("NETIO skipped: " + exception.getMessage());
				_retryDelay = Math.min(60000, _retryDelay * 2);
			} finally {
				_nextAttempt = System.nanoTime() + _retryDelay * 1000000L;
				_loading.set(false);
			}
		}, "NETIOUpd");
		thread.setDaemon(true);
		thread.start();
	}
}
