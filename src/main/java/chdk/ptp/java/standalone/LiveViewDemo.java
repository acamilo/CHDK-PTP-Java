package chdk.ptp.java.standalone;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import chdk.ptp.java.CameraFactory;
import chdk.ptp.java.ICamera;
import chdk.ptp.java.SupportedCamera;
import chdk.ptp.java.exception.CameraConnectionException;
import chdk.ptp.java.exception.CameraNotFoundException;
import chdk.ptp.java.exception.PTPTimeoutException;

/**
 * Displays a panel with live view from camera.
 */
public class LiveViewDemo {

	private static ICamera cam = null;
	static BufferedImagePanel panelLive = new BufferedImagePanel();
	static BufferedImagePanel panelShoot = new BufferedImagePanel();
	static JFrame f = null;
	static Thread threadView = null;

	/**
	 * Runs the demo.
	 * 
	 * @param args
	 *            currently unused
	 * 
	 */

	private static int OP_CONNECT = 0;
	private static int OP_DISCONNECT = 1;
	private static int OP_VIEW = 2;
	private static int OP_ZOOM = 3;
	private static int OP_SHOOT = 4;
	private static boolean isConnected = false;

	public static void main(String[] args) {
		try {
			
			f = new JFrame("Live");
			f.setLayout(new BorderLayout());
			
			JPanel panelImages = new JPanel(new GridLayout(1, 2));
			panelImages.add(panelLive);
			panelImages.add(panelShoot);
			
			f.add(panelImages,BorderLayout.CENTER);
			
			JSlider sliderZoom = new JSlider(0, 100);
			sliderZoom.setValue(0);
			f.add(sliderZoom, BorderLayout.SOUTH);
			
			
			JPanel panelBtn = new JPanel(new GridBagLayout());
			JButton btnDisc = new JButton("Disconnect");
			JButton btnCon = new JButton("Connect");
			JButton btnShoot = new JButton("Shoot");
			panelBtn.add(btnCon);
			panelBtn.add(btnDisc);
			panelBtn.add(btnShoot);
			f.add(panelBtn,BorderLayout.NORTH);
			
			f.setSize(800, 600);
			f.setVisible(true);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			
			btnCon.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					commandCamera(OP_CONNECT);
				}
			});
			
			btnDisc.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					commandCamera(OP_DISCONNECT);
				}
			});
			
			btnShoot.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					panelShoot.setImage((BufferedImage)commandCamera(OP_SHOOT));
					f.repaint();
				}
			});
			
			sliderZoom.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					JSlider source = (JSlider)e.getSource();
				    if (!source.getValueIsAdjusting()) {
				        int zoom = (int)source.getValue();
				        
				        commandCamera(OP_ZOOM, zoom);
				        
				    }
				}
			});
			
			
//			cam.setManualFocusMode();
//			int i = 0;
			
			
//			
//			BufferedImagePanel d = new BufferedImagePanel(cam.getView());
//			Random random = new Random();
//			while (true) {
//				d.setImage(cam.getView());
//				++i;
//				cam.setZoom(i % 100);
//				if (i % 40 == 0) {
//					// cam.setAutoFocusMode();
//					// cam.setZoom(random.nextInt(100));
//					// cam.setManualFocusMode();
//				}
//
//				// if (i % 8 == 0)
//				// cam.setZoom(random.nextInt(100));
//				// cam.setFocus(random.nextInt(1000) + 100);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private synchronized static Object commandCamera(int op, int... param) {
		try {
			if (op == OP_CONNECT) {
				cam = CameraFactory.getCamera();
				cam.connect();
				cam.setRecordingMode();
				isConnected = true;
				
				threadView = new Thread(new Runnable() {
					
					@Override
					public void run() {
						while(isConnected){
							if(panelLive == null){
								panelLive = new BufferedImagePanel( (BufferedImage) commandCamera(OP_VIEW)  );
							} else {
								panelLive.setImage( (BufferedImage) commandCamera(OP_VIEW) );
							}
							f.repaint();
						}
					}
				});
				
				threadView.start();
				
			} else if (op == OP_DISCONNECT) {
				cam.setPlaybackMode();
				cam.disconnect();
				isConnected = false;
			} else if (op == OP_ZOOM) {
				cam.setZoom(param[0]);
			} else if (op == OP_VIEW) {
				return cam.getView();
			} else if (op == OP_SHOOT){
				return cam.getPicture();
			}
		} catch (CameraConnectionException | PTPTimeoutException | CameraNotFoundException e1) {
			e1.printStackTrace();
		}

		return null;

	}

}
