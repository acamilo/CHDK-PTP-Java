package chdk.ptp.java.connection;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import javax.usb.UsbHub;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Various USB utilities
 * 
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 * 
 */
public class UsbUtils {

	private static Log log = LogFactory.getLog(UsbUtils.class);

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
		log.debug("Processing Attatched devices for " + hub);
		for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
			UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
			log.debug("Checking out " + device);
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
		log.debug("Processing Attatched devices for " + hub);
		for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
			log.debug("Checking out " + device);
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
				log.error(e.getLocalizedMessage(), e);
				e.printStackTrace();
			}
		}
		return null;
	}

}
