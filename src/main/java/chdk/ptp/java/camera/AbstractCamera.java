package chdk.ptp.java.camera;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbServices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import chdk.ptp.java.ICamera;
import chdk.ptp.java.connection.PTPConnection;
import chdk.ptp.java.connection.UsbUtils;
import chdk.ptp.java.connection.packet.ByteOrder;
import chdk.ptp.java.connection.packet.CHDKScreenImage;
import chdk.ptp.java.connection.packet.PTPPacket;
import chdk.ptp.java.exception.CameraConnectionException;
import chdk.ptp.java.exception.CameraNotFoundException;

/**
 * Generic CHDK camera implementation with functions that should work for all
 * cameras.
 * 
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 *
 */
public abstract class AbstractCamera implements ICamera {

    private static Log log = LogFactory.getLog(AbstractCamera.class);

    private PTPConnection connection = null;
    private short cameraVendorID = -1;
    private short cameraProductID = -1;
    private String cameraSerialNo = "";

    /**
     * Creates a new instance of
     *
     * @param cameraVendorID
     *            Canon camera Vendor ID
     * @param cameraProductID
     *            Canon camera product ID
     */
    public AbstractCamera(short cameraVendorID, short cameraProductID) {
	this.cameraVendorID = cameraVendorID;
	this.cameraProductID = cameraProductID;
    }

    /**
     * Creates a new instance of
     *
     * @param SerialNo
     *            canon camera serial number
     */
    public AbstractCamera(String SerialNo) {
	cameraSerialNo = SerialNo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see chdk.ptp.java.ICamera#connect()
     */
    @Override
    public void connect() throws CameraConnectionException {
	try {
	    UsbDevice cameraDevice = null;
	    UsbServices services = UsbHostManager.getUsbServices();
	    UsbHub rootHub = services.getRootUsbHub();

	    if (!cameraSerialNo.isEmpty())
		cameraDevice = UsbUtils.findDeviceBySerialNumber(rootHub,
			cameraSerialNo);

	    if (cameraProductID != -1 && cameraVendorID != -1)
		cameraDevice = UsbUtils.findDevice(rootHub, cameraVendorID,
			cameraProductID);

	    if (cameraDevice == null)
		throw new CameraNotFoundException();

	    connection = getConenctionFromUSBDevice(cameraDevice);
	    log.debug("Connected to camera");
	} catch (SecurityException | UsbException
		| UnsupportedEncodingException | UsbDisconnectedException
		| CameraNotFoundException e) {
	    String message = "Could not connect to camera device: "
		    + e.getLocalizedMessage();
	    log.error(message, e);
	    e.printStackTrace();
	    throw new CameraConnectionException(message);
	}
    }

    @Override
    public void disconnect() throws CameraConnectionException {
	try {
	    connection.close();
	    log.debug("Disconnected from camera");
	} catch (Exception e) {
	    String message = "Failed to disconnect from camera: "
		    + e.getLocalizedMessage();
	    log.error(message, e);
	    e.printStackTrace();
	    throw new CameraConnectionException(message);
	}
    }

    @Override
    public boolean executeLuaCommand(String command) {

	StringBuilder formattedCommand = new StringBuilder(command);
	log.debug("Executing: \t\"" + formattedCommand.toString() + "\"");

	// preparing command packet
	PTPPacket p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND,
		PTPPacket.PTP_OPPCODE_CHDK, 0, new byte[8]);
	// oppcode 7 is execute script
	p.encodeInt(PTPPacket.iPTPCommandARG0, 7, ByteOrder.LittleEndian);
	// XXX: acamilo "is going to hell for this"
	p.encodeInt(PTPPacket.iPTPCommandARG1, 0, ByteOrder.LittleEndian);
	connection.sendPTPPacket(p);

	// embedding command into data packet
	formattedCommand.append("\0"); // command needs to be null terminated

	p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_DATA,
		PTPPacket.PTP_OPPCODE_CHDK, 0, formattedCommand.toString()
			.getBytes());

	// send the command data
	connection.sendPTPPacket(p);

	// check response
	p = connection.getResponse();
	if (p.getContainerCommand() == PTPPacket.PTP_USB_CONTAINER_RESPONSE
		&& p.getOppcode() == PTPPacket.PTP_OPPCODE_Response_OK)
	    return true;
	return false;
    }

    @Override
    public String executeLuaQuery(String command) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public BufferedImage getView() throws CameraConnectionException {
	BufferedImage image = null;

	// preparing command packet
	PTPPacket p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND,
		PTPPacket.PTP_OPPCODE_CHDK, 0, new byte[8]);

	// oppcode 12 is transfer framebuffer
	p.encodeInt(PTPPacket.iPTPCommandARG0, 12, ByteOrder.LittleEndian);
	// argument value 1 sends viewport
	p.encodeInt(PTPPacket.iPTPCommandARG1, 1, ByteOrder.LittleEndian);

	connection.sendPTPPacket(p);

	// We should get 2 packets back. A Data and a status packet.
	p = connection.getResponse();
	if (p.getContainerCommand() == PTPPacket.PTP_USB_CONTAINER_DATA) {
	    CHDKScreenImage i = new CHDKScreenImage(p.getData());
	    image = i.decodeViewport();
	} else {
	    String message = "SX50Camera did not respond to a Live View request with a data packet!";
	    log.error(message);
	    throw new CameraConnectionException(message);
	}

	p = connection.getResponse();
	if (p.getContainerCommand() == PTPPacket.PTP_USB_CONTAINER_RESPONSE
		&& p.getOppcode() == PTPPacket.PTP_OPPCODE_Response_OK)
	    return image;
	String message = "SX50Camera did not end session with an OK response even though a data packet was sent!";
	log.error(message);
	throw new CameraConnectionException(message);
    }

    @Override
    public BufferedImage getPicture() throws CameraConnectionException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setRecordingMode() {
	this.executeLuaCommand("set_record(1)");
	try {
	    Thread.sleep(2000);
	} catch (InterruptedException e) {
	    log.error(e.getLocalizedMessage(), e);
	    e.printStackTrace();
	}
    }

    @Override
    public void setPlaybackMode() {
	this.executeLuaCommand("set_record(0)");
	try {
	    Thread.sleep(2000);
	} catch (InterruptedException e) {
	    log.error(e.getLocalizedMessage(), e);
	    e.printStackTrace();
	}
    }

    @Override
    public void setFocus(int focusingDistance) {
	this.executeLuaCommand("set_focus(" + focusingDistance + ")");
	try {
	    Thread.sleep(1000);
	} catch (InterruptedException e) {
	    log.error(e.getLocalizedMessage(), e);
	    e.printStackTrace();
	}
    }

    @Override
    public void setZoom(int zoomPosition) {
	this.executeLuaCommand("set_zoom(" + zoomPosition + ")");
	try {
	    Thread.sleep(3500);
	} catch (InterruptedException e) {
	    log.error(e.getLocalizedMessage(), e);
	    e.printStackTrace();
	}
    }

    private PTPConnection getConenctionFromUSBDevice(UsbDevice dev)
	    throws UnsupportedEncodingException, UsbDisconnectedException,
	    UsbException, CameraConnectionException {

	// device info
	log.debug("Attepting to Connect to device");
	log.debug("\tManufacturer:\t" + dev.getManufacturerString());
	log.debug("\tSerial Number:\t" + dev.getSerialNumberString());

	// Get dev config
	UsbConfiguration config = dev.getActiveUsbConfiguration();
	log.debug("\tGot Device Configuration:\t" + config);

	// Get interfaces
	List<?> totalInterfaces = config.getUsbInterfaces();
	UsbEndpoint camIn = null;
	UsbEndpoint camOut = null;
	for (int i = 0; i < totalInterfaces.size(); i++) {
	    UsbInterface interf = (UsbInterface) totalInterfaces.get(i);
	    log.debug("\t\tFound Interface:\t" + interf);
	    interf.claim();
	    List<?> totalEndpoints = interf.getUsbEndpoints();

	    for (int j = 0; j < totalEndpoints.size(); j++) {
		UsbEndpoint ep = (UsbEndpoint) totalEndpoints.get(j);
		// We're looking for a bulk In and bulk out
		if (ep.getDirection() == -128 && ep.getType() == 2) {
		    log.debug("\t\t\tAssigning Bulk In endpoint #" + j
			    + " to camIn");
		    camIn = ep; // Bulk IN endpoint
		}
		if (ep.getDirection() == 0 && ep.getType() == 2) {
		    log.debug("\t\t\tAssigning Bulk OUT endpoint #" + j
			    + " to camOut");
		    camOut = ep; // Bulk Out endpoint
		}

	    }

	}
	if (camIn == null || camOut == null)
	    throw new CameraConnectionException(
		    "Didn't find my endpoints Something verry bad happened..");
	System.out.println("\tFound my endpoints, Building PTPConnection");

	PTPConnection session = new PTPConnection(camIn, camOut);
	System.out.println("SX50Camera is ready to recieve commands");
	return session;
    }
}