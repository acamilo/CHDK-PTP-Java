package chdk.ptp.java.camera;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.usb.UsbDevice;

import chdk.ptp.java.SupportedCamera;
import chdk.ptp.java.exception.CameraConnectionException;
import chdk.ptp.java.exception.PTPTimeoutException;

/**
 * SX50Camera implementation.
 */
public class SX50Camera extends FailSafeCamera {

	private Logger log = Logger.getLogger(SX50Camera.class.getName());

	public SX50Camera(UsbDevice device) {
		super(device);
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
			log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			e.printStackTrace();
		} catch (CameraConnectionException e) {
			log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			e.printStackTrace();
		} catch (PTPTimeoutException e) {
			log.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
			log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new CameraConnectionException(e.getMessage());
		}
	}

	@Override
	public SupportedCamera getCameraInfo() {
		return SupportedCamera.SX50HS;
	}

}
