package output;
import java.awt.*;
import java.time.Instant;

import javax.swing.*;

import brightness.BrightnessCorrection;
import brightness.IBrightnessCorrection;
import configuration.DisplayConfiguration;
import configuration.IDisplayConfiguration;
import effects.IColorableEffect;
import effects.IEffect;
import led.*;


public class GuiDisplayAdaptor implements IDisplayAdaptor {
	
	JFrame frame;
	IDisplayAdaptor parentAdaptor;
	private IBrightnessCorrection _brightnessCorrection;
	private IDisplayConfiguration _config = new DisplayConfiguration("global.properties", false);
	final int DEFAULT_OVERLAY_DURATION_MS = _config.getInt("output.DefaultOverlayDurationMs", 1000);
	private IEffect _currentOverlayEffect;
	private long _currentOverlayStartedMs;
	private int _currentOverlayDurationMs;
	
	public GuiDisplayAdaptor(IDisplayAdaptor parentAdaptor) {
		this.parentAdaptor = parentAdaptor;
		initWindow();
		_brightnessCorrection = BrightnessCorrection.getInstance();
	}
	
	public GuiDisplayAdaptor() {
		initWindow();
		_brightnessCorrection = BrightnessCorrection.getInstance();
	}
	
	private void initWindow() {
		frame = new JFrame("LED Display Preview");
		JLabel textLabel = new JLabel("Waiting for Data to visualize...",SwingConstants.CENTER); 
		textLabel.setPreferredSize(new Dimension(1330, 350)); 
		frame.getContentPane().add(textLabel, BorderLayout.CENTER); 
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setAutoRequestFocus(false);
		frame.setVisible(true); 
	}
	
	@Override
	public void show(ILEDArray leds) {
		_brightnessCorrection.doDimmingStep();
		applyOverlay(leds);
		frame.getContentPane().removeAll();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Color color;
		for(int x=0; x<leds.sizeX(); x++) {
			for(int y=0; y<leds.sizeY(); y++) {
				color = new Color(leds.led(x, y).rWithBrightnessCorrection(), leds.led(x, y).gWithBrightnessCorrection(), leds.led(x, y).bWithBrightnessCorrection());
				JLabel textLabel = new JLabel("."); 
				textLabel.setPreferredSize(new Dimension(20, 20)); 
				textLabel.setOpaque(true);
				textLabel.setBackground(color);
				textLabel.setForeground(color);
				textLabel.setBounds(x*22, y*22, 20, 20);
				frame.getContentPane().add(textLabel);
			}
		}

		frame.setVisible(true); 
		
		if (parentAdaptor != null) {
			parentAdaptor.show(leds);
		}
	}
	
	@Override
	public void addOverlay(IEffect overlayEffect) {
		addOverlay(overlayEffect, DEFAULT_OVERLAY_DURATION_MS);
	}

	@Override
	public void addOverlay(IEffect overlayEffect, int overlayDuratrionMs) {
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
	}

}
