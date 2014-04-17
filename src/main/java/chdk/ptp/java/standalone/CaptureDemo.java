package chdk.ptp.java.standalone;

import chdk.ptp.java.Camera;

/**
 * Displays a panel with live view from camera.
 */
public class CaptureDemo {

    private static Camera cam;
    private static short canonVendor = 0x04a9;

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
	    short someCanonCamera = 0x325a;
	    short canonSX50 = 0x3259;
	    cam = new Camera(canonVendor, canonSX50);
	    // switch to capture mode
	    cam.setRecordingMode();

	    // show taken image
	    BufferedImagePanel d = new BufferedImagePanel(cam.getPicture());
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    cam.disconnect();
	}
    }
}
