package chdk.ptp.java.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class BufferedImagePannel extends JPanel{
	BufferedImage image;
	public BufferedImagePannel(BufferedImage i){
		image=i;
		JFrame frame = new JFrame("Image Display");
		frame.add(this);
		frame.setSize(i.getWidth(), i.getHeight());
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	

	
	
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(image, 0, 0, image.getWidth(),image.getHeight() , null);
	}




	public void setImage(BufferedImage i) {
		// TODO Auto-generated method stub
		image = i;
		this.repaint();
		
	}
}
