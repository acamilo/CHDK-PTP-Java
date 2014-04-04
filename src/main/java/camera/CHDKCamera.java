package camera;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotOpenException;
import javax.usb.UsbServices;

import packet.ByteOrder;
import packet.CHDKScreenImage;
import packet.PTPPacket;

public class CHDKCamera {
    PTPConnection connection;

    /**
     * @param args
     * @throws UsbNotOpenException
     * @throws UsbNotActiveException
     * @throws CameraConnectionException
     * @throws CameraNotFoundException
     * @throws UsbException
     * @throws UsbDisconnectedException
     * @throws UnsupportedEncodingException
     * @throws SecurityException
     */
    public static void main(String[] args) throws UsbNotActiveException, UsbNotOpenException,
            UsbDisconnectedException, UsbException {
        CHDKCamera cam = null;
        try {
            cam = new CHDKCamera((short) 0x04a9, (short) 0x325a);
            cam.executeLuaScript("switch_mode_usb(1)");

            BufferedImagePannel d = null;
            while (true) {
                if (d == null)
                    d = new BufferedImagePannel(cam.getView());
                else
                    d.setImage(cam.getView());
            }
        } catch (SecurityException | UnsupportedEncodingException | UsbDisconnectedException
                | UsbException | CameraNotFoundException | CameraConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            cam.connection.close();
            return;
        }

    }

    public CHDKCamera(short vid, short pid) throws SecurityException, UsbException,
            CameraNotFoundException, UnsupportedEncodingException, UsbDisconnectedException,
            CameraConnectionException {
        UsbServices services = UsbHostManager.getUsbServices();
        UsbHub rootHub = services.getRootUsbHub();
        UsbDevice dev = findDevice(rootHub, vid, pid);
        if (dev == null)
            throw new CameraNotFoundException();
        connection = getConenctionFromUSBDevice(dev);

    }

    public CHDKCamera(String SerialNo) throws SecurityException, UsbException,
            UnsupportedEncodingException, UsbDisconnectedException, CameraNotFoundException,
            CameraConnectionException {
        UsbServices services = UsbHostManager.getUsbServices();
        UsbHub rootHub = services.getRootUsbHub();
        UsbDevice dev = findDeviceBySerialNumber(rootHub, SerialNo);
        if (dev == null)
            throw new CameraNotFoundException();
        connection = getConenctionFromUSBDevice(dev);
    }

    public boolean executeLuaScript(String script) {
        System.out.println("Executing: \t\"" + script + "\"");
        PTPPacket p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND,
                PTPPacket.PTP_OPPCODE_CHDK, 0, new byte[8]); // 7,0x00,0x00,0x00,0x00,0x00,0x00,0x00}
        p.encodeInt(PTPPacket.iPTPCommandARG0, 7, ByteOrder.LittleEndian); // oppcode 7 is
                                                                           // execute
                                                                           // script
        p.encodeInt(PTPPacket.iPTPCommandARG1, 0, ByteOrder.LittleEndian); // i'm going to
                                                                           // hell for
                                                                           // this

        connection.sendPTPPacket(p);

        // Now we prepare the script and send it as a DATA packet
        script += "\0"; // make sure string is null terminated
        p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_DATA, PTPPacket.PTP_OPPCODE_CHDK, 0,
                script.getBytes());

        connection.sendPTPPacket(p); // send the script
        connection.getResponse();
        return false;
    }

    public BufferedImage getView() throws CameraConnectionException {
        BufferedImage image = null;

        PTPPacket p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND,
                PTPPacket.PTP_OPPCODE_CHDK, 0, new byte[8]); // 7,0x00,0x00,0x00,0x00,0x00,0x00,0x00}
        p.encodeInt(PTPPacket.iPTPCommandARG0, 12, ByteOrder.LittleEndian); // oppcode 12
                                                                            // is transfer
                                                                            // framebuffer
        p.encodeInt(PTPPacket.iPTPCommandARG1, 1, ByteOrder.LittleEndian); // 1 sends
                                                                           // viewport

        connection.sendPTPPacket(p);

        // We should get 2 packets back. A Data packet and a status.
        p = connection.getResponse();
        if (p.getContainerCommand() == p.PTP_USB_CONTAINER_DATA) {
            CHDKScreenImage i = new CHDKScreenImage(p.getData());
            image = i.decodeViewport();

        } else {
            System.out.println("Was Expecting A Data Packet!");
            throw new CameraConnectionException(
                    "Camera Did not respond to a Live View request with a data packet!");
        }
        p = connection.getResponse();
        if (p.getContainerCommand() == PTPPacket.PTP_USB_CONTAINER_RESPONSE
                && p.getOppcode() == PTPPacket.PTP_OPPCODE_Response_OK)
            return image;
        else
            throw new CameraConnectionException(
                    "Camera Did not end session with an OK response even though a data packet was sent!");
    }

    public BufferedImage getPicture() {
        return null;
    }

    // Device Discovery Functions
    private UsbDevice findDevice(UsbHub hub, short vendorId, short productId) {
        System.out.println("Processing Attatched devices for " + hub);
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            System.out.println("Checkign out " + device);
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

    private UsbDevice findDeviceBySerialNumber(UsbHub hub, String serialNo)
            throws UnsupportedEncodingException, UsbDisconnectedException, UsbException {
        System.out.println("Processing Attatched devices for " + hub);
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            System.out.println("Checkign out " + device);
            if (device.isUsbHub()) {
                device = findDeviceBySerialNumber((UsbHub) device, serialNo);
                if (device != null)
                    return device;
            }
            System.out.println(desc.bDeviceClass());
            if (desc.bDeviceClass() != 0)
                return null; // Device we care about has class in descriptor.
            if (device.getManufacturerString().equals(serialNo))
                return device;
        }

        return null;
    }

    private PTPConnection getConenctionFromUSBDevice(UsbDevice dev)
            throws UnsupportedEncodingException, UsbDisconnectedException, UsbException,
            CameraConnectionException {
        // device info
        System.out.println("Attepting to Connect to device");
        System.out.println("\tManufacturer:\t" + dev.getManufacturerString());
        System.out.println("\tSerial Number:\t" + dev.getSerialNumberString());

        // Get dev config
        UsbConfiguration config = dev.getActiveUsbConfiguration();
        System.out.println("\tGot Device Configuration:\t" + config);

        // Get interfaces
        List totalInterfaces = config.getUsbInterfaces();
        UsbEndpoint camIn = null;
        UsbEndpoint camOut = null;
        for (int i = 0; i < totalInterfaces.size(); i++) {
            UsbInterface interf = (UsbInterface) totalInterfaces.get(i);
            System.out.println("\t\tFound Interface:\t" + interf);
            interf.claim();
            List totalEndpoints = interf.getUsbEndpoints();

            for (int j = 0; j < totalEndpoints.size(); j++) {
                UsbEndpoint ep = (UsbEndpoint) totalEndpoints.get(j);
                // System.out.println("\t\t\tFound Endpoint:\n\t\t\t\t"+ep.getUsbEndpointDescriptor().toString().replaceAll("[\\n\\r]+",
                // "\n\t\t\t\t"));
                // We're looking for a bulk In and bulk out
                if (ep.getDirection() == -128 && ep.getType() == 2) {
                    System.out.println("\t\t\tAssigning Bulk In endpoint #" + j + " to camIn");
                    camIn = ep; // Bulk IN endpoint
                }
                if (ep.getDirection() == 0 && ep.getType() == 2) {
                    System.out.println("\t\t\tAssigning Bulk OUT endpoint #" + j + " to camOut");
                    camOut = ep; // Bulk Out endpoint
                }

            }

        }
        if (camIn == null || camOut == null)
            throw new CameraConnectionException(
                    "Didn't find my endpoints Something verry bad happened..");
        else
            System.out.println("\tFound my endpoints, Building PTPConnection");

        PTPConnection session = new PTPConnection(camIn, camOut);
        System.out.println("Camera is ready to recieve commands");
        return session;
    }

}
