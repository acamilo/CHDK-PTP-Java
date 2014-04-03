package usb;

import java.util.List;

import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbEndpoint;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbServices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import packet.CHDKScreenImage;
import packet.PTPPacket;
import camera.PTPSession;
import camera.displayImage;

public class USBImageHello {

    private static Log log = LogFactory.getLog(USBImageHello.class);

    public static UsbDevice findDevice(UsbHub hub, short vendorId, short productId) {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
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

    public static void main(String[] args) throws Exception {
        UsbServices services = UsbHostManager.getUsbServices();
        UsbHub rootHub = services.getRootUsbHub();
        short canonVendor = 0x04a9;
        short someCanonCamera = 0x325a;
        short canonSX50 = 0x3259;
        UsbDevice dev = findDevice(rootHub, canonVendor, canonSX50);
        if (dev == null) {
            log.debug("No Device Found!");
            return;
        }
        log.debug("Found Matching Device!:\t" + dev);

        try {
            // device info
            log.debug("\tManufacturer:\t" + dev.getManufacturerString());
            log.debug("\tSerial Number:\t" + dev.getSerialNumberString());

            // Get dev config
            UsbConfiguration config = dev.getActiveUsbConfiguration();
            log.debug("\tGot Device Configuration:\t" + config);

            // Get interfaces
            List totalInterfaces = config.getUsbInterfaces();
            UsbEndpoint camIn = null;
            UsbEndpoint camOut = null;
            for (int i = 0; i < totalInterfaces.size(); i++) {
                UsbInterface interf = (UsbInterface) totalInterfaces.get(i);
                log.debug("\t\tFound Interface:\t" + interf);
                interf.claim();
                List totalEndpoints = interf.getUsbEndpoints();

                for (int j = 0; j < totalEndpoints.size(); j++) {
                    UsbEndpoint ep = (UsbEndpoint) totalEndpoints.get(j);
                    log.debug("\t\t\tFound Endpoint:\n\t\t\t\t"
                            + ep.getUsbEndpointDescriptor().toString()
                                    .replaceAll("[\\n\\r]+", "\n\t\t\t\t"));
                    // We're looking for a bulk In and bulk out
                    if (ep.getDirection() == -128 && ep.getType() == 2) {
                        log.debug("\t\t\tAssigning Bulk In endpoint #" + j + " to camIn");
                        camIn = ep; // Bulk IN endpoint
                    }
                    if (ep.getDirection() == 0 && ep.getType() == 2) {
                        log.debug("\t\t\tAssigning Bulk OUT endpoint #" + j + " to camOut");
                        camOut = ep; // Bulk Out endpoint
                    }

                }

            }
            if (camIn == null || camOut == null)
                log.debug("Didn't find my endpoints Something verry bad happened..");
            else
                log.debug("\tFound my endpoints, Building pipe");

            log.debug("Starting PTP session");
            PTPSession session = new PTPSession(camIn, camOut);

            PTPPacket p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND,
                    PTPPacket.PTP_OPPCODE_OpenSession, 0, new byte[] { 0x01, 0x00, 0x00, 0x00 });
            log.debug("Sending CHDK start package: " + p);
            session.sendPTPPacket(p);
            log.debug("response: " + session.getResponse());

            p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND, PTPPacket.PTP_OPPCODE_CHDK, 1,
                    new byte[] { 7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });
            log.debug("Sending script mode start package: " + p);
            session.sendPTPPacket(p); // IMPORTANT: NO RESPONSE!!!

            p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_DATA, PTPPacket.PTP_OPPCODE_CHDK, 2,
                    "switch_mode_usb(1)\0".getBytes());
            log.debug("switching to capture mode: " + p);
            session.sendPTPPacket(p); // IMPORTANT: NO RESPONSE!!!

            displayImage d = null;
            while (true) {
                p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND, PTPPacket.PTP_OPPCODE_CHDK,
                        1, new byte[] { 12, 0x00, 0x00, 0x00, (byte) (0x01), 0x00, 0x00, 0x00 });
                log.debug("Sending getDisplayData command: " + p);
                session.sendPTPPacket(p);

                /*
                 * // capture mode done before loop p = new
                 * PTPPacket(PTPPacket.PTP_USB_CONTAINER_DATA, PTPPacket.PTP_OPPCODE_CHDK,
                 * 2, "switch_mode_usb(1)\0".getBytes());
                 * log.debug("switching to capture mode: " + p); session.sendPTPPacket(p);
                 * // IMPORTANT: NO RESPONSE!!!
                 */

                p = session.getResponse();
                log.debug("Response for display data request: " + p);
                CHDKScreenImage i = new CHDKScreenImage(p.getData());
                if (d == null)
                    d = new displayImage(i);
                else
                    d.setImage(i);
                // System.out.print(i);

                session.getResponse();
            }

            // session.close();
            // System.out.println("Done");
        } catch (Exception e) {
            log.error("Everything went wrong...", e);
            e.printStackTrace();
        }

    }
}
