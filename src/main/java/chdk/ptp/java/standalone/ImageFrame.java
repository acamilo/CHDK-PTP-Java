package chdk.ptp.java.standalone;

import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class ImageFrame extends JFrame{

	public ImageFrame(BufferedImage image) {
		super("Image Display");
		add(new BufferedImagePanel(image));
		setSize(image.getWidth(), image.getHeight());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
}
