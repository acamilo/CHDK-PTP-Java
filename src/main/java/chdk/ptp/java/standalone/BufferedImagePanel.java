package chdk.ptp.java.standalone;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class BufferedImagePanel extends JPanel {
    BufferedImage image;

    public BufferedImagePanel(BufferedImage i) {
	image = i;
	JFrame frame = new JFrame("Image Display");
	frame.add(this);
	frame.setSize(i.getWidth(), i.getHeight());
	frame.setVisible(true);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	g2d.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
    }

    public void setImage(BufferedImage i) {
	// TODO Auto-generated method stub
	image = i;
	this.repaint();

    }
}
