package chdk.ptp.java;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbServices;

import chdk.ptp.java.camera.FailSafeCamera;
import chdk.ptp.java.camera.SX160ISCamera;
import chdk.ptp.java.camera.SX50Camera;
import chdk.ptp.java.connection.UsbUtils;
import chdk.ptp.java.exception.CameraNotFoundException;

/**
 * 
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 * 
 */
public class CameraFactory  {

	private static Logger log = Logger.getLogger(CameraFactory.class.getName());

	
	public static ICamera getCamera() throws CameraNotFoundException {
		ICamera camera = null;
		try{
			camera =  UsbUtils.findCamera();
		} catch (SecurityException | UsbException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			e.printStackTrace();
			throw new CameraNotFoundException();
		}
		if (camera == null){
			throw new CameraNotFoundException();
		}
		
		return camera;
	}

	/**
	 * Attempts to get known camera implementation or backs up to failsafe
	 * camera with liveview support
	 * 
	 * @param cameraModel
	 *            selected camera model
	 * @return configured camera object
	 * @throws CameraNotFoundException
	 *             on error
	 */
	public static ICamera getCamera(SupportedCamera cameraModel)
			throws CameraNotFoundException {
		UsbDevice cameraDevice = null;
		try {

			UsbServices services = UsbHostManager.getUsbServices();
			UsbHub rootHub = services.getRootUsbHub();
			cameraDevice = UsbUtils.findDevice(rootHub, cameraModel.getVendorID(),
					cameraModel.getPID());

		} catch (SecurityException | UsbException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			e.printStackTrace();
			throw new CameraNotFoundException();
		}
		if (cameraDevice == null)
			throw new CameraNotFoundException();

		switch (cameraModel) {
		case SX50HS:
			return new SX50Camera(cameraDevice);
		case SX160IS:
			return new SX160ISCamera(cameraDevice);
		case FailsafeCamera:
		default:
			return new FailSafeCamera(cameraDevice);
		}
	}

	/**
	 * Attempts to get known camera implementation for given serialNumber, backs
	 * up to failsafe camera with liveview support
	 * 
	 * @param serialNumber
	 *            camera serial number
	 * @return configured camera object
	 * @throws CameraNotFoundException
	 *             on error
	 */
	public static ICamera getCamera(String serialNumber)
			throws CameraNotFoundException {
		UsbDevice cameraDevice = null;
		try {
			UsbServices services = UsbHostManager.getUsbServices();
			UsbHub rootHub = services.getRootUsbHub();
			cameraDevice = UsbUtils.findDeviceBySerialNumber(rootHub,
					serialNumber);
		} catch (SecurityException | UsbException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			throw new CameraNotFoundException(e.getMessage());
		}
		if (cameraDevice == null)
			throw new CameraNotFoundException();

		// try to map product id to known implementations
		SupportedCamera discoveredCameraModel = SupportedCamera.FailsafeCamera;
		for (SupportedCamera cameraModel : SupportedCamera.values()) {
			if (cameraModel.getPID() == cameraDevice.getUsbDeviceDescriptor()
					.idProduct())
				discoveredCameraModel = cameraModel;
			break;
		}

		switch (discoveredCameraModel) {
		case SX50HS:
			return new SX50Camera(serialNumber);
		case SX160IS:
		case FailsafeCamera:
		default:
			return new FailSafeCamera(serialNumber);
		}
	}
}
