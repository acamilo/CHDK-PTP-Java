package chdk.ptp.java.camera;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import chdk.ptp.java.exception.CameraConnectionException;

/**
 * Fail safe camera class which should at least show live view from camera.
 * 
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 *
 */
public class FailSafeCamera extends AbstractCamera {

    private Log log = LogFactory.getLog(FailSafeCamera.class);

    /**
     * Creates a new instance of
     *
     * @param cameraVendorID
     *            Canon camera Vendor ID
     * @param cameraProductID
     *            Canon camera product ID
     */
    public FailSafeCamera(short cameraVendorID, short cameraProductID) {
	super(cameraVendorID, cameraProductID);
    }

    /**
     * Creates a new instance of
     *
     * @param SerialNo
     *            canon camera serial number
     */
    public FailSafeCamera(String SerialNo) {
	super(SerialNo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see chdk.ptp.java.ICamera#setAutoFocusMode()
     */
    @Override
    public void setAutoFocusMode() throws CameraConnectionException {
	log.info("Dummy method here, won't do anything");
    }

    /*
     * (non-Javadoc)
     * 
     * @see chdk.ptp.java.ICamera#setManualFocusMode()
     */
    @Override
    public void setManualFocusMode() throws CameraConnectionException {
	log.info("Dummy method here, won't do anything");
    }

}
