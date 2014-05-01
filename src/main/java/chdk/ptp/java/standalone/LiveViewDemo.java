package chdk.ptp.java.standalone;

import java.util.Random;

import chdk.ptp.java.CameraFactory;
import chdk.ptp.java.ICamera;
import chdk.ptp.java.SupportedCamera;

/**
 * Displays a panel with live view from camera.
 */
public class LiveViewDemo {

    private static ICamera cam;

    /**
     * Runs the demo.
     * 
     * @param args
     *            currently unused
     * 
     */
    public static void main(String[] args) {
	cam = null;
	try {
	    cam = CameraFactory.getCamera(SupportedCamera.SX50HS);
	    cam.connect();
	    cam.setRecordingMode();
	    cam.setManualFocusMode();
	    int i = 0;
	    BufferedImagePanel d = new BufferedImagePanel(cam.getView());
	    Random random = new Random();
	    while (true) {
		d.setImage(cam.getView());
		++i;
		if (i % 40 == 0) {
		    cam.setAutoFocusMode();
		    cam.setZoom(random.nextInt(100));
		    cam.setManualFocusMode();
		}

		if (i % 8 == 0)
		    cam.setFocus(random.nextInt(1000) + 100);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
