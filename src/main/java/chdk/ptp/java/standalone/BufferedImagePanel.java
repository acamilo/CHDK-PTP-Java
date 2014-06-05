package chdk.ptp.java.standalone;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class BufferedImagePanel extends JPanel {
	BufferedImage image;
	JFrame frame;
	
	public BufferedImagePanel(){
		super();
	}
	public BufferedImagePanel(BufferedImage i) {
		this();
		image = i;
		setSize(i.getWidth(), i.getHeight());
	}

	public void Close() {
		WindowEvent wev = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
	}

	@Override
	public void paint(Graphics g) {
		if(image != null){
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
		}
	}

	public void setImage(BufferedImage i) {
		image = i;
		this.repaint();
	}
}
