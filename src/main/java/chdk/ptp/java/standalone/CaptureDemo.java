package chdk.ptp.java.standalone;

import chdk.ptp.java.CameraFactory;
import chdk.ptp.java.ICamera;
import chdk.ptp.java.SupportedCamera;

/**
 * Displays a panel with live view from camera.
 */
public class CaptureDemo {

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
	    cam = CameraFactory.getCamera(SupportedCamera.SX160IS);
	    cam.connect();
	    // switch to capture mode
	    cam.setRecordingMode();

	    // show taken image
	    new BufferedImagePanel(cam.getPicture());
	    cam.disconnect();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
