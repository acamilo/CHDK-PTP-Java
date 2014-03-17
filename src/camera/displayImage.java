package camera;

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

import packet.CHDKScreenImage;

public class displayImage extends JPanel{
	CHDKScreenImage image;
	public displayImage(CHDKScreenImage i){
		image=i;
		JFrame frame = new JFrame("Image Display");
		frame.add(this);
		frame.setSize(i.viewportVisableWidth(), i.viewportVisableHeight());
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	

	
	
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(image.decodeViewport(), 0, 0, 720,240 , null);
	}




	public void setImage(CHDKScreenImage i) {
		// TODO Auto-generated method stub
		image = i;
		this.repaint();
		
	}
}
