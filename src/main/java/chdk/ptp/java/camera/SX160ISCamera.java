package chdk.ptp.java.camera;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SX160ISCamera extends FailSafeCamera {
    private Log log = LogFactory.getLog(SX160ISCamera.class);

    /**
     * Creates a new instance of
     *
     * @param cameraVendorID
     *            Canon camera Vendor ID
     * @param cameraProductID
     *            Canon camera product ID
     */
    public SX160ISCamera(short cameraVendorID, short cameraProductID) {
	super(cameraVendorID, cameraProductID);
    }

    /**
     * Creates a new instance of
     *
     * @param SerialNo
     *            canon camera serial number
     */
    public SX160ISCamera(String SerialNo) {
	super(SerialNo);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see chdk.ptp.java.ICamera#setManualFocusMode()
     */
    @Override
    public void setManualFocusMode() {
	// TODO: check current camera focus mode!!!
	try {
	    this.executeLuaCommand("click('left')");
	    Thread.sleep(1000);
	    this.executeLuaCommand("click('right')");
	    Thread.sleep(500);
	    this.executeLuaCommand("click('set')");
	    Thread.sleep(1000);
	    this.executeLuaCommand("click('set')");
	    Thread.sleep(1000);
	} catch (InterruptedException e) {
	    log.error(e.getLocalizedMessage());
	    e.printStackTrace();
	}
    }
    

    /*
     * (non-Javadoc)
     * 
     * @see chdk.ptp.java.ICamera#setAutoFocusMode()
     */
    @Override
    public void setAutoFocusMode() {
	// TODO: check current camera focus mode!!!
	try {
	    this.executeLuaCommand("click('left')");
	    Thread.sleep(1000);
	    this.executeLuaCommand("click('left')");
	    Thread.sleep(500);
	    this.executeLuaCommand("click('set')");
	    Thread.sleep(1000);
	} catch (InterruptedException e) {
	    log.error(e.getLocalizedMessage());
	    e.printStackTrace();
	}
    }
}
