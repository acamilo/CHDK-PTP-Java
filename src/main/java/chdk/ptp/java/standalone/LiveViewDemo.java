package chdk.ptp.java.standalone;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.usb.UsbException;

import chdk.ptp.java.CameraFactory;
import chdk.ptp.java.ICamera;
import chdk.ptp.java.connection.CameraUsbDevice;
import chdk.ptp.java.connection.UsbUtils;
import chdk.ptp.java.exception.GenericCameraException;
import chdk.ptp.java.exception.PTPTimeoutException;
import chdk.ptp.java.model.CameraMode;
import chdk.ptp.java.model.ImageResolution;

/**
 * Displays a panel with live view from camera.
 */
public class LiveViewDemo extends JFrame {

	private static ICamera cam = null;
	static BufferedImagePanel imageLive = new BufferedImagePanel();
	static BufferedImagePanel imageShoot = new BufferedImagePanel();
	static Thread threadView = null;

	public LiveViewDemo() {
		intGui();
		initActions();
	}

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
	private static int OP_CMD = 5;
	private static int OP_PLAY = 6;
	private static int OP_REC = 7;
	private static int OP_RES = 8;
	private static boolean isConnected = false;

	JComboBox<CameraUsbDevice> jcboCameras = null;
	JButton jBtnConnect = null;
	JButton jBtnDisconnect = null;
	JButton jBtnCmd = null;
	JButton jBtnShoot = null;
	JSlider jSliderZoom = null;
	JTextField jTxtCmd = null;
	JTextArea jTextAreaLog = null;
	JScrollPane jScrollPaneLog = null;
	JButton jBtnRec = null;
	JButton jBtnPlay = null;
	JButton jBtnLive = null;
	JComboBox<ImageResolution> jcboResol = null;

	private void intGui() {

		JPanel jPanTop = new javax.swing.JPanel();
		jcboCameras = new JComboBox();
		jBtnConnect = new JButton();
		jBtnDisconnect = new JButton();
		jBtnCmd = new JButton();
		jBtnShoot = new JButton();
		jSliderZoom = new JSlider(0, 100, 0);
		JPanel jPanFooter = new JPanel();
		jTxtCmd = new JTextField();
		jScrollPaneLog = new JScrollPane();
		jTextAreaLog = new JTextArea();
		JPanel jPanLiveArea = new JPanel();

		jBtnRec = new JButton("Rec");
		jBtnPlay = new JButton("Play");
		jBtnLive = new JButton("Live");
		jcboResol = new JComboBox<ImageResolution>(ImageResolution.values());

		jTextAreaLog.setEditable(false);

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		jBtnConnect.setText("Connect");

		jBtnDisconnect.setText("Disconnect");

		javax.swing.GroupLayout jPanTopLayout = new javax.swing.GroupLayout(
				jPanTop);
		jPanTop.setLayout(jPanTopLayout);
		jPanTopLayout
				.setHorizontalGroup(jPanTopLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanTopLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(
												jcboCameras,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												300,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(18, 18, 18)
										.addComponent(jBtnConnect)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jBtnDisconnect)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jBtnRec)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jBtnPlay)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jBtnLive)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jcboResol)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addContainerGap(251, Short.MAX_VALUE)));
		jPanTopLayout
				.setVerticalGroup(jPanTopLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanTopLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanTopLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jcboCameras,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(
																jBtnConnect)
														.addComponent(
																jBtnDisconnect)
														.addComponent(jBtnRec)
														.addComponent(jBtnPlay)
														.addComponent(jBtnLive)
														.addComponent(jcboResol))

										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		jBtnCmd.setText("Send");

		jTextAreaLog.setColumns(20);
		jTextAreaLog.setRows(5);
		jScrollPaneLog.setViewportView(jTextAreaLog);

		javax.swing.GroupLayout jPanFooterLayout = new javax.swing.GroupLayout(
				jPanFooter);
		jPanFooter.setLayout(jPanFooterLayout);
		jPanFooterLayout
				.setHorizontalGroup(jPanFooterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanFooterLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanFooterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																jScrollPaneLog)
														.addGroup(
																jPanFooterLayout
																		.createSequentialGroup()
																		.addComponent(
																				jTxtCmd)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				jBtnCmd)))
										.addContainerGap()));
		jPanFooterLayout
				.setVerticalGroup(jPanFooterLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								jPanFooterLayout
										.createSequentialGroup()
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(
												jScrollPaneLog,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												120,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanFooterLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																jTxtCmd,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(jBtnCmd))
										.addContainerGap()));

		jBtnShoot.setText("Shoot");

		javax.swing.GroupLayout imageLiveLayout = new javax.swing.GroupLayout(
				imageLive);
		imageLive.setLayout(imageLiveLayout);
		imageLiveLayout.setHorizontalGroup(imageLiveLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0,
				Short.MAX_VALUE));
		imageLiveLayout.setVerticalGroup(imageLiveLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 249,
				Short.MAX_VALUE));

		javax.swing.GroupLayout jPanLiveAreaLayout = new javax.swing.GroupLayout(
				jPanLiveArea);
		jPanLiveArea.setLayout(jPanLiveAreaLayout);
		jPanLiveAreaLayout
				.setHorizontalGroup(jPanLiveAreaLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addComponent(imageLive,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)
						.addGroup(
								jPanLiveAreaLayout
										.createSequentialGroup()
										.addComponent(jBtnShoot)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(
												jSliderZoom,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												700, Short.MAX_VALUE)));
		jPanLiveAreaLayout
				.setVerticalGroup(jPanLiveAreaLayout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								jPanLiveAreaLayout
										.createSequentialGroup()
										.addComponent(
												imageLive,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanLiveAreaLayout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.TRAILING)
														.addComponent(jBtnShoot)
														.addComponent(
																jSliderZoom,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))));

		javax.swing.GroupLayout imageShootLayout = new javax.swing.GroupLayout(
				imageShoot);
		imageShoot.setLayout(imageShootLayout);
		imageShootLayout.setHorizontalGroup(imageShootLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 0, Short.MAX_VALUE));
		imageShootLayout.setVerticalGroup(imageShootLayout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0,
				Short.MAX_VALUE));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jPanTop, javax.swing.GroupLayout.DEFAULT_SIZE,
						javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(jPanFooter, javax.swing.GroupLayout.DEFAULT_SIZE,
						javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(jPanLiveArea,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(imageShoot,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE).addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(jPanTop,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(
														jPanLiveArea,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														Short.MAX_VALUE)
												.addComponent(
														imageShoot,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														Short.MAX_VALUE))
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(jPanFooter,
										javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE)));

		setExtendedState(MAXIMIZED_BOTH);
		pack();
	}

	public void initActions() {

		try {
			Collection<CameraUsbDevice> cameras = UsbUtils
					.listAttachedCameras();
			for (CameraUsbDevice cameraUsbDevice : cameras) {
				jcboCameras.addItem(cameraUsbDevice);
			}

			jBtnConnect.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					commandCamera(OP_CONNECT);
				}
			});

			jBtnDisconnect.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					commandCamera(OP_DISCONNECT);
				}
			});

			jBtnShoot.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					BufferedImage image = (BufferedImage) commandCamera(OP_SHOOT);
					System.out.println(image.getWidth() + " x " + image.getHeight());
					imageShoot.setImage(image);
				}
			});

			jSliderZoom.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					if (!jSliderZoom.getValueIsAdjusting()) {
						int zoom = jSliderZoom.getValue();

						commandCamera(OP_ZOOM, zoom);

					}
				}
			});

		} catch (SecurityException | UsbException e) {
			e.printStackTrace();
		}

		jBtnCmd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeCmd();
			}
		});

		jTxtCmd.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					executeCmd();
				}

			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		jBtnRec.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				commandCamera(OP_REC);
				keepAlive();
			}

		});

		jBtnPlay.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				commandCamera(OP_PLAY);
			}

		});

		jBtnLive.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				threadView = new Thread(new LiveCapture());
				threadView.start();
			}

		});
		
		jcboResol.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImageResolution r = (ImageResolution) jcboResol.getSelectedItem();
				commandCamera(OP_RES, r);
			}
		});

	}

	private void executeCmd() {
		String cmd = jTxtCmd.getText();
		jTextAreaLog.append("# " + cmd + "\n");
		jTextAreaLog.append(">> " + commandCamera(OP_CMD, cmd) + "\n");
		jTextAreaLog.setCaretPosition(jTextAreaLog.getDocument().getLength());

		jTxtCmd.setText("");
		jTxtCmd.requestFocus();
	}

	public static void main(String[] args) {

		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new LiveViewDemo().setVisible(true);
			}
		});

	}

	private synchronized Object commandCamera(int op, Object... param) {
		try {
			if (op == OP_CONNECT) {
				jTextAreaLog.append("_Connecting..._\n");
				jTextAreaLog.updateUI();
				try {
					cam = CameraFactory.getCamera();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cam.connect();
				jSliderZoom.setMaximum(cam.getZoomSteps());
				// cam.setRecordingMode();
				isConnected = true;
				jTextAreaLog.append("_Camera Connected_\n");
			} else if (op == OP_DISCONNECT) {
				isConnected = false;
				// cam.setPlaybackMode();
				jTextAreaLog.append("_Disconnecting_\n");
				cam.disconnect();
				jTextAreaLog.append("_Camera Disonnected_\n");
			} else if (op == OP_ZOOM) {
				jTextAreaLog.append("_set zoom " + param[0] + "_\n");
				cam.setZoom((Integer) param[0]);
				jTextAreaLog.append("_zoom complete_\n");
			} else if (op == OP_VIEW) {
				return cam.getView();
			} else if (op == OP_SHOOT) {
				return cam.getPicture();
			} else if (op == OP_CMD) {
				return cam.executeLuaQuery(param[0].toString());
			} else if (op == OP_PLAY) {
				cam.setOperaionMode(CameraMode.PLAYBACK);
			} else if (op == OP_REC) {
				cam.setOperaionMode(CameraMode.RECORD);
			} else if (op == OP_RES) {
				cam.setImageResolution((ImageResolution) param[0]);
			}
		} catch (GenericCameraException | PTPTimeoutException e1) {
			e1.printStackTrace();
			jTextAreaLog.append(">>>" + e1.getLocalizedMessage() + "\n");
		}

		return null;

	}

	Thread threadKeepAlive = new Thread(new KeepAliveRunnable());

	private void keepAlive() {
		//
		if (!threadKeepAlive.isAlive()) {
			threadKeepAlive.start();
		}

	}

	private class LiveCapture implements Runnable {

		@Override
		public void run() {
			while (isConnected) {
				BufferedImage visorImage = (BufferedImage) commandCamera(OP_VIEW);
				visorImage = resizeImage(visorImage,
						(int) (visorImage.getHeight() * 1.5),
						visorImage.getHeight());
				imageLive.setImage(visorImage);
				repaint();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class KeepAliveRunnable implements Runnable {
		@Override
		public void run() {
			while (isConnected) {
				int zoomOriginal = jSliderZoom.getValue();
				int zoom = 0;
				if (zoomOriginal == 0) {
					zoom = 1;
				} else {
					zoom = zoomOriginal - 1;
				}
				commandCamera(OP_ZOOM, zoom);
				commandCamera(OP_ZOOM, zoomOriginal);
				try {
					Thread.sleep(45000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
	}

	public static BufferedImage resizeImage(final Image image, int width,
			int height) {
		final BufferedImage bufferedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		final Graphics2D graphics2D = bufferedImage.createGraphics();
		graphics2D.setComposite(AlphaComposite.Src);
		// below three lines are for RenderingHints for better image quality at
		// cost of
		// higher processing time
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2D.drawImage(image, 0, 0, width, height, null);
		graphics2D.dispose();
		return bufferedImage;
	}
}
