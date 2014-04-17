package chdk.ptp.java.standalone;

import java.util.Random;

import chdk.ptp.java.Camera;

/**
 * Displays a panel with live view from camera.
 */
public class LiveViewDemo {

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
	    cam.setRecordingMode();
	    cam.switchToManualFocus();
	    int i = 0;
	    BufferedImagePanel d = new BufferedImagePanel(cam.getView());
	    Random random = new Random();
	    while (true) {
		d.setImage(cam.getView());
		++i;
		if (i % 40 == 0) {
		    cam.switchToAutoFocus();
		    cam.setZoom(random.nextInt(100));
		    cam.switchToManualFocus();
		}

		if (i % 8 == 0)
		    cam.setFocus(random.nextInt(1000) + 100);
	    }
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    cam.disconnect();
	}
    }
}
