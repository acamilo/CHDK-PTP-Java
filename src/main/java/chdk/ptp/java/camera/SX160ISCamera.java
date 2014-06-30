package chdk.ptp.java.camera;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.usb.UsbDevice;

import chdk.ptp.java.SupportedCamera;
import chdk.ptp.java.exception.CameraConnectionException;

public class SX160ISCamera extends FailSafeCamera {
	private Logger log = Logger.getLogger(SX160ISCamera.class.getName());

	public SX160ISCamera(UsbDevice device) {
		super(device);
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
	public void setManualFocusMode() throws CameraConnectionException {
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
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw new CameraConnectionException(e.getMessage());
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
		return SupportedCamera.SX160IS;
	}
}
