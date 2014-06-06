package chdk.ptp.java.standalone;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Random;

import javax.swing.JApplet;
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
import chdk.ptp.java.SupportedCamera;
import chdk.ptp.java.connection.CameraUsbDevice;
import chdk.ptp.java.connection.UsbUtils;
import chdk.ptp.java.exception.CameraConnectionException;
import chdk.ptp.java.exception.CameraNotFoundException;
import chdk.ptp.java.exception.PTPTimeoutException;

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
	private static boolean isConnected = false;

	JComboBox<CameraUsbDevice> jcboCameras = null;
	JButton jBtnConnect = null;
	JButton jBtnDisconnect = null;
	JButton jBtnCmd = null;
	JButton jBtnShoot = null;
	JSlider jSliderZoom = null;

	private void intGui() {

		JPanel jPanTop = new javax.swing.JPanel();
		jcboCameras = new JComboBox();
		jBtnConnect = new JButton();
		jBtnDisconnect = new JButton();
		jBtnCmd = new JButton();
		jBtnShoot = new JButton();
		jSliderZoom = new JSlider(0, 100, 0);
		JPanel jPanFooter = new JPanel();
		JTextField jTxtCmd = new JTextField();
		JScrollPane jScrollPaneLog = new JScrollPane();
		JTextArea jTextAreaLog = new JTextArea();
		JPanel jPanLiveArea = new JPanel();

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
										.addContainerGap(455, Short.MAX_VALUE)));
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
																jBtnDisconnect))
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
					imageShoot.setImage(image);
				}
			});

			jSliderZoom.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					if (!jSliderZoom.getValueIsAdjusting()) {
						int zoom = (int) jSliderZoom.getValue();

						commandCamera(OP_ZOOM, zoom);

					}
				}
			});

		} catch (SecurityException | UsbException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new LiveViewDemo().setVisible(true);
			}
		});

	}

	private synchronized Object commandCamera(int op, int... param) {
		try {
			if (op == OP_CONNECT) {
				cam = CameraFactory.getCamera();
				cam.connect();
				cam.setRecordingMode();
				isConnected = true;

				threadView = new Thread(new Runnable() {

					@Override
					public void run() {
						while (isConnected) {
							imageLive
									.setImage((BufferedImage) commandCamera(OP_VIEW));
							repaint();
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
			} else if (op == OP_SHOOT) {
				return cam.getPicture();
			}
		} catch (CameraConnectionException | PTPTimeoutException
				| CameraNotFoundException e1) {
			e1.printStackTrace();
		}

		return null;

	}

}
