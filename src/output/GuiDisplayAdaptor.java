package output;
import java.awt.*;
import javax.swing.*;

import brightness.BrightnessCorrection;
import brightness.IBrightnessCorrection;
import led.*;


public class GuiDisplayAdaptor implements IDisplayAdaptor {
	
	JFrame frame;
	IDisplayAdaptor parentAdaptor;
	private IBrightnessCorrection _brightnessCorrection;
	
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
		frame.setVisible(true); 
	}
	
	@Override
	public void show(ILEDArray leds) {
		_brightnessCorrection.doDimmingStep();
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

}
