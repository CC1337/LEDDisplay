package output;

import java.time.Instant;

import brightness.BrightnessCorrection;
import brightness.IBrightnessCorrection;
import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;
import effects.IColorableEffect;
import led.*;

public class OctoWS2811DisplayAdaptor implements IDisplayAdaptor {

	private ISerial _serial;
	private int _linesPerPin = 1;
	private IBrightnessCorrection _brightnessCorrection;
	private IDisplayConfiguration _config = new DisplayConfiguration("global.properties", false);
	final int DEFAULT_OVERLAY_DURATION_MS = _config.getInt("output.DefaultOverlayDurationMs", 1000);
	private IColorableEffect _currentOverlayEffect;
	private long _currentOverlayStartedMs;
	private int _currentOverlayDurationMs;

	public OctoWS2811DisplayAdaptor(ISerial serial) {
		this._serial = serial;
		serial.openSerialPort();
		_brightnessCorrection = BrightnessCorrection.getInstance();
	}

	public OctoWS2811DisplayAdaptor(ISerial serial, int linesPerPin) {
		this._serial = serial;
		serial.openSerialPort();
		this._linesPerPin = linesPerPin;
		_brightnessCorrection = BrightnessCorrection.getInstance();
	}

	@Override
	public void show(ILEDArray leds) {
		_brightnessCorrection.doDimmingStep();
		applyOverlay(leds);
		// long startTime = System.currentTimeMillis();
		byte[] transmitArray = image2data(leds);
		// LOGGER.info("transmitarray ready: " + (System.currentTimeMillis() -
		// startTime));
		_serial.send(transmitArray);
		// LOGGER.info("sent after: " + (System.currentTimeMillis() -
		// startTime));
	}

	// *** copied & modified from "processing" movie2serial example from
	// OctoWS2811 Lib. ***
	// image2data converts an image to OctoWS2811's raw data format.
	// The number of vertical pixels in the image must be a multiple
	// of 8. The data array must be the proper size for the image.
	byte[] image2data(ILEDArray leds) {
		int x, y, xbegin, xend, xinc, mask;
		int pixelColumn[] = new int[8];
		byte[] result = new byte[leds.sizeX() * 3 * 8 * _linesPerPin + 3];
		result[0] = (byte) 42;
		result[1] = (byte) 13;
		result[2] = (byte) 37;
		int resultPointer = 3;

		for (y = 0; y < _linesPerPin; y++) {
			if ((y & 1) == 0) {
				// even numbered rows are left to right
				xbegin = 0;
				xend = leds.sizeX();
				xinc = 1;
			} else {
				// odd numbered rows are right to left
				xbegin = leds.sizeX() - 1;
				xend = -1;
				xinc = -1;
			}
			for (x = xbegin; x != xend; x += xinc) {
				for (int i = 0; i < 8; i++) {
					// fetch 8 pixels from the image, 1 for each pin
					// pixelColumn[i] = image.pixels[x + (y + linesPerPin * i) *
					// image.width];
					int curLedYPosition = _linesPerPin * i + y;
					if (curLedYPosition >= leds.sizeY())
						break;
					ILED curLed = leds.led(x, curLedYPosition);
					pixelColumn[i] = (curLed.gWithBrightnessCorrection() << 16)
							| (curLed.rWithBrightnessCorrection() << 8) | curLed.bWithBrightnessCorrection(); // GRB
																												// LED
																												// Layout
				}
				// convert 8 pixels to 24 bytes
				for (mask = 0x800000; mask != 0; mask >>= 1) {
					byte b = 0;
					for (int i = 0; i < 8; i++) {
						if ((pixelColumn[i] & mask) != 0)
							b |= (1 << i);
					}
					result[resultPointer++] = b;
				}
			}
		}
		return result;
	}

	@Override
	public void addOverlay(IColorableEffect overlayEffect) {
		addOverlay(overlayEffect, DEFAULT_OVERLAY_DURATION_MS);
	}

	@Override
	public void addOverlay(IColorableEffect overlayEffect, int overlayDuratrionMs) {
		_currentOverlayEffect = overlayEffect;
		_currentOverlayStartedMs = Instant.now().toEpochMilli();
		_currentOverlayDurationMs = overlayDuratrionMs;
	}
	
	private void applyOverlay(ILEDArray leds) {
		if (_currentOverlayEffect == null)
			return;
		if (Instant.now().toEpochMilli() < _currentOverlayStartedMs + _currentOverlayDurationMs)
			leds.applyEffect(_currentOverlayEffect);
		else {
			_currentOverlayEffect = null;
			_currentOverlayStartedMs = 0;
			_currentOverlayDurationMs = 0;
		}
	}

	@Override
	public void closeSerialPort() {
		_serial.closeSerialPort();
	}

}
