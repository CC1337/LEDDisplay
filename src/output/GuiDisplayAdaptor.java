package output;
import java.awt.*;
import javax.swing.*;

import led.*;


public class GuiDisplayAdaptor implements IDisplayAdaptor {
	
	JFrame frame;
	IDisplayAdaptor parentAdaptor;
	
	public GuiDisplayAdaptor(IDisplayAdaptor parentAdaptor) {
		this.parentAdaptor = parentAdaptor;
		initWindow();
	}
	
	public GuiDisplayAdaptor() {
		initWindow();
	}
	
	private void initWindow() {
		frame = new JFrame("LED Display Preview");
		JLabel textLabel = new JLabel("Waiting for Data to visualize...",SwingConstants.CENTER); 
		textLabel.setPreferredSize(new Dimension(1330, 170)); 
		frame.getContentPane().add(textLabel, BorderLayout.CENTER); 
		frame.setLocationRelativeTo(null);
		frame.pack();
		frame.setVisible(true); 
	}
	
	@Override
	public void show(ILEDArray leds) {
		frame.getContentPane().removeAll();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		for(int x=0; x<leds.sizeX(); x++) {
			for(int y=0; y<leds.sizeY(); y++) {
				JLabel textLabel = new JLabel("."); 
				textLabel.setPreferredSize(new Dimension(20, 20)); 
				textLabel.setOpaque(true);
				textLabel.setBackground(new Color(leds.led(x, y).r(), leds.led(x, y).g(), leds.led(x, y).b()));
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
