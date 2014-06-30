package chdk.ptp.java.camera;

import java.util.logging.Logger;

import javax.usb.UsbDevice;

import chdk.ptp.java.SupportedCamera;
import chdk.ptp.java.exception.CameraConnectionException;
import chdk.ptp.java.exception.PTPTimeoutException;

/**
 * Fail safe camera class which should at least show live view from camera.
 * 
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 * 
 */
public class FailSafeCamera extends AbstractCamera {

	private Logger log = Logger.getLogger(FailSafeCamera.class.getName());

	public FailSafeCamera(UsbDevice device) {
		super(device);
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

	@Override
	public SupportedCamera getCameraInfo() {
		return SupportedCamera.FailsafeCamera;
	}

}
