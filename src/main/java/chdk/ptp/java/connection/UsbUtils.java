package chdk.ptp.java.connection;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbServices;

import chdk.ptp.java.ICamera;
import chdk.ptp.java.SupportedCamera;

/**
 * Various USB utilities
 * 
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 * 
 */
public class UsbUtils {

	private static Logger log = Logger.getLogger(UsbUtils.class.getName());

	/**
	 * Searches for a device with matching vendor and product id in given hub
	 * 
	 * @param hub
	 *            to search in
	 * @param vendorId
	 *            to search for
	 * @param productId
	 *            to search for
	 * @return matching device or null
	 */
	@SuppressWarnings("unchecked")
	public static UsbDevice findDevice(UsbHub hub, short vendorId,
			short productId) {
		log.info("Processing Attatched devices for " + hub);
		for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
			UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
			log.info("Checking out " + device);
			if (desc.idVendor() == vendorId && desc.idProduct() == productId)
				return device;
			if (device.isUsbHub()) {
				device = findDevice((UsbHub) device, vendorId, productId);
				if (device != null)
					return device;
			}
		}
		return null;
	}
	
	/**
	 * Searches for a device with matching serial number in given USB hub
	 * 
	 * @param hub
	 *            to search in
	 * @param serialNo
	 *            to search for
	 * @return matching device or null
	 */
	@SuppressWarnings("unchecked")
	public static UsbDevice findDeviceBySerialNumber(UsbHub hub, String serialNo) {
		log.info("Processing Attatched devices for " + hub);
		for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
			log.info("Checking out " + device);
			if (device.isUsbHub()) {
				device = findDeviceBySerialNumber((UsbHub) device, serialNo);
				if (device != null)
					return device;
			}
			try {
				if (device != null
						&& device.getUsbDeviceDescriptor().bDeviceClass() != 0
						&& device.getManufacturerString().equals(serialNo))
					return device;
			} catch (UnsupportedEncodingException | UsbDisconnectedException
					| UsbException e) {
				log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				e.printStackTrace();
			}
		}
		return null;
	}

	public static ICamera findCamera() throws SecurityException, UsbException {
		Collection<CameraUsbDevice> cams =  listAttachedCameras();
		
		if (cams.size() > 0){
			// picks the first
			CameraUsbDevice cameraDevice =  cams.iterator().next();
			SupportedCamera sc = SupportedCamera.getCamera(cameraDevice.getIdVendor(), cameraDevice.getIdProduct());
		
			try {
				return sc.getClazz().getConstructor(UsbDevice.class).newInstance(cameraDevice.getDevice());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
				log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		} 
		return null;
	}
	
	public static Collection<CameraUsbDevice> listAttachedCameras() throws SecurityException, UsbException {
		UsbServices services = UsbHostManager.getUsbServices();
		UsbHub rootHub = services.getRootUsbHub();
		return listAttachedCameras(rootHub);
	}
	
	@SuppressWarnings("unchecked")
	private static Collection<CameraUsbDevice> listAttachedCameras(UsbHub dev){
		List<CameraUsbDevice> list = new ArrayList<>();
		
		List<UsbDevice> devices = dev.getAttachedUsbDevices();
		for (UsbDevice usbDevice : devices) {
			UsbDeviceDescriptor desc = usbDevice.getUsbDeviceDescriptor();
			if (usbDevice.isUsbHub()) {
				list.addAll(listAttachedCameras((UsbHub) usbDevice));
			} else {
				if(SupportedCamera.isSuportedCamera(desc.idVendor(), desc.idProduct())){
					try {
						list.add(new CameraUsbDevice(desc.idVendor(), desc.idProduct(), usbDevice.getProductString(), usbDevice));
					} catch (UnsupportedEncodingException | UsbDisconnectedException | UsbException e1) {
						log.info(e1.getLocalizedMessage());
					}
				}
			}
		}
		
		return list;
	}

}
