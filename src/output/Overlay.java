package output;

import java.time.Instant;

import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;
import effects.IEffect;
import led.ILEDArray;

class Overlay implements IOverlay {
	
	private IDisplayConfiguration _config = new DisplayConfiguration("global.properties", false);
	final int DEFAULT_OVERLAY_DURATION_MS = _config.getInt("output.DefaultOverlayDurationMs", 1000);
	private IEffect _currentOverlayEffect;
	private long _currentOverlayStartedMs;
	private int _currentOverlayDurationMs;

	public void addOverlay(IEffect overlayEffect) {
		addOverlay(overlayEffect, DEFAULT_OVERLAY_DURATION_MS);
	}

	public void addOverlay(IEffect overlayEffect, int overlayDuratrionMs) {
		_currentOverlayEffect = overlayEffect;
		_currentOverlayStartedMs = Instant.now().toEpochMilli();
		_currentOverlayDurationMs = overlayDuratrionMs;
	}
	
	protected void applyOverlay(ILEDArray leds) {
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
}
