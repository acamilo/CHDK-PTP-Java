package org.usb4java.javax;

import javax.usb.UsbDevice;

import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;

/**
 * Helper for some USB calls.
 * 
 * @author Aleś Bułojčyk <alex73mail@gmail.com>
 */
public class UsbHelper {
    public static void reset(UsbDevice device) throws Exception {
        AbstractDevice a = (AbstractDevice) device;
        DeviceHandle handle = a.open();
        LibUsb.resetDevice(handle);
    }
}
