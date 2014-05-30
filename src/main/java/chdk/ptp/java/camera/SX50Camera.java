package chdk.ptp.java.camera;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import chdk.ptp.java.exception.CameraConnectionException;

/**
 * SX50Camera implementation.
 */
public class SX50Camera extends AbstractCamera {

    private Log log = LogFactory.getLog(SX50Camera.class);

    /**
     * Creates a new instance of
     *
     * @param cameraVendorID
     *            Canon camera Vendor ID
     * @param cameraProductID
     *            Canon camera product ID
     */
    public SX50Camera(short cameraVendorID, short cameraProductID) {
	super(cameraVendorID, cameraProductID);
    }

    /**
     * Creates a new instance of
     *
     * @param SerialNo
     *            canon camera serial number
     */
    public SX50Camera(String SerialNo) {
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
	} catch (CameraConnectionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see chdk.ptp.java.ICamera#setAutoFocusMode()
     */
    @Override
    public void setAutoFocusMode() throws CameraConnectionException {
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
	} catch (Exception e) {
		// TODO Auto-generated catch block
		log.error(e.getLocalizedMessage(), e);
		throw new CameraConnectionException(e.getMessage());
	}
    }

}
