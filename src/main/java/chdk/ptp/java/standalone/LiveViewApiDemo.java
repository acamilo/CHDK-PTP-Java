package chdk.ptp.java.standalone;

import java.util.Random;

import chdk.ptp.java.CameraFactory;
import chdk.ptp.java.ICamera;
import chdk.ptp.java.exception.GenericCameraException;
import chdk.ptp.java.exception.PTPTimeoutException;
import chdk.ptp.java.model.CameraMode;
import chdk.ptp.java.model.FocusMode;

/**
 * Displays a panel with live view from camera.
 */
public class LiveViewApiDemo {

	/**
	 * Runs the demo.
	 * 
	 * @param args
	 *            currently unused
	 * 
	 */
	public static void main(String[] args) {
		ICamera cam = null;
		BufferedImagePanel d = null;
		Random random = new Random();
		int i = 0;
		try {
			try {
				cam = CameraFactory.getCamera();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cam.connect();
			cam.setOperaionMode(CameraMode.RECORD);
			d = new BufferedImagePanel(cam.getView(), true);
		} catch (PTPTimeoutException | GenericCameraException e1) {
			e1.printStackTrace();
		}
		while (true) {
			try {
				d.setImage(cam.getView());
				++i;

				if (i % 100 == 0) {
					cam.setZoom(random.nextInt(100) + 50);
					continue;
				}

				if (i % 50 == 0) {
					int expectedFocus = random.nextInt(1000) + 3000;
					cam.setFocus(expectedFocus);
					System.out.println("Zoom: " + cam.getZoom() + " focus: "
							+ cam.getFocus() + " expected: " + expectedFocus);
					cam.getPicture();
					continue;
				}

				if (i % 25 == 0) {
					cam.setFocusMode(FocusMode.AUTO);
					cam.getPicture();
					System.out.println("Zoom: " + cam.getZoom() + " focus: "
							+ cam.getFocus());
					continue;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
