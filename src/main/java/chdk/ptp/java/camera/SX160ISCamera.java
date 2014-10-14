package chdk.ptp.java.camera;

import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.usb.UsbDevice;

import chdk.ptp.java.SupportedCamera;
import chdk.ptp.java.exception.CameraConnectionException;
import chdk.ptp.java.exception.CameraShootException;
import chdk.ptp.java.exception.GenericCameraException;
import chdk.ptp.java.exception.PTPTimeoutException;
import chdk.ptp.java.model.FocusMode;

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

	@Override
	public SupportedCamera getCameraInfo() {
		return SupportedCamera.SX160IS;
	}

	@Override
	public void setFocusMode(FocusMode desiredMode) throws PTPTimeoutException,
			GenericCameraException {
		FocusMode currentFocusMode = getFocusMode();
		switch (desiredMode) {
		case AUTO:
			switch (currentFocusMode) {
			case AUTO:
				return;
			case MF:
				try {
					this.executeLuaCommand("click('left')");
					Thread.sleep(1000);
					this.executeLuaCommand("click('left')");
					Thread.sleep(500);
					this.executeLuaCommand("click('set')");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.log(Level.SEVERE, e.getLocalizedMessage(), e);
					throw new GenericCameraException(e.getLocalizedMessage());
				}
				return;
			case INF:
			case MACRO:
			case SUPERMACRO:
			case UNKNOWN:
			default:
				throw new GenericCameraException(
						"Setting auto focus mode from state: "
								+ currentFocusMode + "is not implemented");
			}
		case INF:
		case MACRO:
		case MF:
			switch (currentFocusMode) {
			case AUTO:
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
					throw new GenericCameraException(e.getLocalizedMessage());
				}
				return;
			case MF:
				return;
			case INF:
			case MACRO:
			case SUPERMACRO:
			case UNKNOWN:
			default:
				throw new GenericCameraException(
						"Setting manual focus mode from state: "
								+ currentFocusMode + "is not implemented");
			}
		case SUPERMACRO:
		case UNKNOWN:
		default:
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see chdk.ptp.java.camera.AbstractCamera#getPicture()
	 */
	@Override
	public BufferedImage getPicture() throws CameraConnectionException, CameraShootException {
		try {
			return super.getPicture();
		} finally {
			// try to uninit
			try {
				// do sad. but seem i need to do it on sx160is
				this.executeLuaQuery("return init_usb_capture(0)");
			} catch (CameraConnectionException | PTPTimeoutException ex) {
				throw new CameraConnectionException(ex.getMessage());
			}
		}
	}
}
