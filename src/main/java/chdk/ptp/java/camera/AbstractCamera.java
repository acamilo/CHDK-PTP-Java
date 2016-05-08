package chdk.ptp.java.camera;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbServices;

import org.usb4java.javax.UsbHelper;

import chdk.ptp.java.ICamera;
import chdk.ptp.java.connection.PTPConnection;
import chdk.ptp.java.connection.UsbUtils;
import chdk.ptp.java.connection.packet.ByteOrder;
import chdk.ptp.java.connection.packet.CHDKScreenImage;
import chdk.ptp.java.connection.packet.PTPPacket;
import chdk.ptp.java.exception.CameraConnectionException;
import chdk.ptp.java.exception.CameraNotFoundException;
import chdk.ptp.java.exception.CameraShootException;
import chdk.ptp.java.exception.GenericCameraException;
import chdk.ptp.java.exception.InvalidPacketException;
import chdk.ptp.java.exception.PTPTimeoutException;
import chdk.ptp.java.model.Button;
import chdk.ptp.java.model.CameraMode;
import chdk.ptp.java.model.FocusMode;
import chdk.ptp.java.model.ImageResolution;

/**
 * Generic CHDK camera implementation with functions that should work for all cameras.
 * 
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 * 
 */
public abstract class AbstractCamera implements ICamera {

    private static Logger log = Logger.getLogger(AbstractCamera.class.getName());

    private PTPConnection connection = null;
    private String cameraSerialNo = "";
    private UsbDevice device = null;
    private int zoomStepsCache = -1;

    public PTPConnection getPTPConnection() {
        return connection;
    }

    public AbstractCamera(UsbDevice device) {
        this.device = device;
    }

    /**
     * Creates a new instance of
     * 
     * @param SerialNo canon camera serial number
     */
    public AbstractCamera(String SerialNo) {
        cameraSerialNo = SerialNo;
    }

    /**
     * USB reset required for camera initialization after close previous connection without disconnect.
     */
    public void usbReset() throws Exception {
        UsbHelper.reset(device);
    }

    /*
     * (non-Javadoc)
     * 
     * @see chdk.ptp.java.ICamera#connect()
     */
    @Override
    public void connect() throws CameraConnectionException {
        try {
            if (device == null) {
                findCameraDevice();
            }

            connection = getConnectionFromUSBDevice(device);
            cameraSerialNo = device.getSerialNumberString();
            log.info("Connected to camera");
        } catch (SecurityException | UsbException | UnsupportedEncodingException
                | UsbDisconnectedException | CameraNotFoundException e) {
            String message = "Could not connect to camera device: " + e.getLocalizedMessage();
            log.log(Level.SEVERE, message, e);
            e.printStackTrace();
            throw new CameraConnectionException(message);
        }
    }

    public String getCameraSerialNumber() {
        return cameraSerialNo;
    }

    private void findCameraDevice() throws SecurityException, UsbException, CameraNotFoundException {
        UsbDevice cameraDevice = null;
        UsbServices services = UsbHostManager.getUsbServices();
        UsbHub rootHub = services.getRootUsbHub();

        if (!cameraSerialNo.isEmpty())
            cameraDevice = UsbUtils.findDeviceBySerialNumber(rootHub, cameraSerialNo);

        if (getCameraInfo().getPID() != -1 && getCameraInfo().getVendorID() != -1)
            cameraDevice = UsbUtils.findDevice(rootHub, getCameraInfo().getVendorID(),
                    getCameraInfo().getPID());

        if (cameraDevice == null)
            throw new CameraNotFoundException();
        this.device = cameraDevice;
    }
    


    @Override
    public void disconnect() throws CameraConnectionException {
        try {
            connection.close();
            log.info("Disconnected from camera");
        } catch (Exception e) {
            String message = "Failed to disconnect from camera: " + e.getLocalizedMessage();
            log.log(Level.SEVERE, message, e);
            e.printStackTrace();
            throw new CameraConnectionException(message);
        }
    }

    @Override
    public int executeLuaCommand(String command) throws PTPTimeoutException,
            CameraConnectionException {

        StringBuilder formattedCommand = new StringBuilder(command);
        if (!formattedCommand.toString().endsWith(";"))
            formattedCommand.append(';');
        log.info("Executing: \t\"" + formattedCommand.toString() + "\"");

        // preparing command packet
        PTPPacket p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND,
                PTPPacket.PTP_OPPCODE_CHDK, 0, new byte[8]);

        p.encodeInt(PTPPacket.iPTPCommandARG0, PTPPacket.CHDK_ExecuteScript, ByteOrder.LittleEndian);
        p.encodeInt(PTPPacket.iPTPCommandARG1, PTPPacket.PTP_CHDK_SL_LUA, ByteOrder.LittleEndian);

        connection.sendPTPPacket(p);

        // embedding command into data packet
        formattedCommand.append("\0"); // command needs to be null terminated

        p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_DATA, PTPPacket.PTP_OPPCODE_CHDK, 0,
                formattedCommand.toString().getBytes());

        // send the command data
        connection.sendPTPPacket(p);

        // check response
        int scriptId = -1;
        try {
            p = connection.getResponse();
        } catch (InvalidPacketException e) {
            throw new CameraConnectionException(e.getMessage());
        }
        if (isResponseOK(p)) {
            scriptId = p.decodeInt(PTPPacket.iPTPCommandARG0, ByteOrder.LittleEndian);
        }
        return scriptId;
    }

    private boolean isResponseOK(PTPPacket p) {
        return p.getContainerCommand() == PTPPacket.PTP_USB_CONTAINER_RESPONSE
                && p.getOppcode() == PTPPacket.PTP_OPPCODE_Response_OK;
    }

    @Override
    public Object executeLuaQuery(String command) throws PTPTimeoutException,
            CameraConnectionException {
        int scriptId = executeLuaCommand(command);

        waitScriptReady();

        List<Object> listReturn = new ArrayList<>();
        Object ro;
        while ((ro = readScriptMsg(scriptId)) != null) {
            listReturn.add(ro);
        }

        if (listReturn.size() == 0) {
            return null;
        } else if (listReturn.size() == 1) {
            return listReturn.get(0);
        } else {
            return listReturn;
        }
    }

    private void waitScriptReady() throws CameraConnectionException {
        boolean sctriptRunning = true;
        try {
            do {
                // get status
                PTPPacket p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND,
                        PTPPacket.PTP_OPPCODE_CHDK, 0, new byte[8]);

                // oppcode 12 is transfer framebuffer
                p.encodeInt(PTPPacket.iPTPCommandARG0, PTPPacket.CHDK_ScriptStatus,
                        ByteOrder.LittleEndian);

                connection.sendPTPPacket(p);

                p = connection.getResponse();

                int scriptStatus = p.decodeByte(PTPPacket.iPTPCommandARG0);
                if (scriptStatus != PTPPacket.PTP_CHDK_SCRIPT_STATUS_RUN) {
                    sctriptRunning = false;
                } else {
                    Thread.sleep(100);
                }

            } while (sctriptRunning);
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            throw new CameraConnectionException(e.getMessage());
        }
    }

    private Object readScriptMsg(int scriptId) throws CameraConnectionException,
            PTPTimeoutException {

        Object msg = null;

        // get status
        PTPPacket p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND,
                PTPPacket.PTP_OPPCODE_CHDK, 0, new byte[8]);

        // oppcode 12 is transfer framebuffer
        p.encodeInt(PTPPacket.iPTPCommandARG0, PTPPacket.CHDK_ReadScriptMsg, ByteOrder.LittleEndian);

        connection.sendPTPPacket(p);

        byte[] scriptbvalue = null;
        try {
            // recive script value
            p = connection.getResponse();
            scriptbvalue = p.getData();
            // recive script metadata
            p = connection.getResponse();
        } catch (InvalidPacketException e) {
            throw new CameraConnectionException(e.getMessage());
        }

        if (isResponseOK(p)) {
            int type = p.decodeInt(PTPPacket.iPTPCommandARG0, ByteOrder.LittleEndian);
            int subType = p.decodeInt(PTPPacket.iPTPCommandARG1, ByteOrder.LittleEndian);
            int scriptIdMsg = p.decodeInt(PTPPacket.iPTPCommandARG2, ByteOrder.LittleEndian);
            int size = p.decodeInt(PTPPacket.iPTPCommandARG3, ByteOrder.LittleEndian);

            if (scriptIdMsg == 0) {
                return null;
            }

            if (scriptId != scriptIdMsg) {
                // oops!!
                throw new CameraConnectionException(
                        "could not read script response. Is camera operations thread-safe?Script Run: "
                                + scriptId + ", Script Msg: " + scriptIdMsg);
            }

            switch (type) {
            case PTPPacket.PTP_CHDK_S_MSGTYPE_RET:
            case PTPPacket.PTP_CHDK_S_MSGTYPE_USER:

                switch (subType) {
                case PTPPacket.PTP_CHDK_TYPE_UNSUPPORTED: // type name will be
                                                          // returned in data
                case PTPPacket.PTP_CHDK_TYPE_STRING:
                case PTPPacket.PTP_CHDK_TYPE_TABLE: // tables are returned as a
                                                    // serialized string.
                    // The user is responsible for unserializing, to allow
                    // different serialization methods
                    msg = new String(scriptbvalue);
                    break;
                case PTPPacket.PTP_CHDK_TYPE_BOOLEAN:
                    msg = scriptbvalue[0] == 1;
                    break;
                case PTPPacket.PTP_CHDK_TYPE_INTEGER:
                    ByteBuffer buffer = ByteBuffer.wrap(scriptbvalue);
                    buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
                    msg = buffer.getInt();
                    break;
                }
                break;
            case PTPPacket.PTP_CHDK_S_MSGTYPE_ERR:
            default:
                msg = "ERROR: " + scriptMsgErrorTypeToName(subType) + " "
                        + new String(scriptbvalue);
                break;
            }

        }

        // (*msg)->type = ptp.Param1;
        // (*msg)->subtype = ptp.Param2;
        // (*msg)->script_id = ptp.Param3;
        // (*msg)->size = ptp.Param4;

        return msg;
    }

    private String scriptMsgErrorTypeToName(int typeId) {
        String[] names = { "none", "compile", "runtime" };
        if (typeId >= names.length) {
            return "unknown_error_subtype";
        }
        return names[typeId];
    }

    @Override
    public byte[] getByteView() throws CameraConnectionException {
        try {
            // preparing command packet
            PTPPacket p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND,
                    PTPPacket.PTP_OPPCODE_CHDK, 0, new byte[8]);

            // oppcode 12 is transfer framebuffer
            p.encodeInt(PTPPacket.iPTPCommandARG0, PTPPacket.CHDK_GetDisplayData,
                    ByteOrder.LittleEndian);
            // argument value 1 sends viewport, 4+8 is Bitmap
            p.encodeInt(PTPPacket.iPTPCommandARG1, 1, ByteOrder.LittleEndian);

            connection.sendPTPPacket(p);

            // We should get 2 packets back. A Data and a status packet.
            p = connection.getResponse();
            byte[] viewPortData;
            if (p.getContainerCommand() == PTPPacket.PTP_USB_CONTAINER_DATA) {
            	// log.info("data_size: " + p.decodeInt(PTPPacket.iPTPCommandARG0, ByteOrder.LittleEndian));
                viewPortData = p.getData();
            } else {
                String message = "SX50Camera did not respond to a Live View request with a data packet!";
                log.log(Level.SEVERE, message);
                throw new CameraConnectionException(message);
            }

            p = connection.getResponse();
            if (isResponseOK(p))
                return viewPortData;
            String message = "SX50Camera did not end session with an OK response even though a data packet was sent!";
            log.log(Level.SEVERE, message);
            throw new CameraConnectionException(message);
        } catch (InvalidPacketException | PTPTimeoutException e) {
            throw new CameraConnectionException(e.getMessage());
        }
    }

    @Override
    public BufferedImage getView() throws CameraConnectionException {
        return new CHDKScreenImage(getByteView()).decodeViewport();
    }

    @Override
    public byte[] getBytePicture() throws CameraConnectionException, CameraShootException {
        try {
            if (getUsbCaptureSupport() != 1) {
                throw new CameraConnectionException("usb capture not supported");
            }
            if (getUsbCaptureSuport() % 2 == 0) {
                // if odd the camera don't hava jpg suport
                // TODO init_usb_capture in raw
                throw new CameraConnectionException("unsupported format");

            }
            executeLuaCommand("init_usb_capture(1,0,0);");
            // init capture, we have 3 seconds to take a picture

            Thread.sleep(100); // there needs to be a delay between this and
            // the one below or camera will freak out
            executeLuaCommand("shoot();"); // take picture

            // Loop until camera takes picture.
            PTPPacket ready = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND,
                    PTPPacket.PTP_OPPCODE_CHDK, 0, new byte[8]);

            ready.encodeInt(PTPPacket.iPTPCommandARG0, PTPPacket.CHDK_RemoteCaptureIsReady,
                    ByteOrder.LittleEndian);

            int nTry = 0;
            while (true) {
                connection.sendPTPPacket(ready);

                PTPPacket response = connection.getResponse();
                // isready: 0: not ready, lowest 2 bits: available image formats, 0x10000000: error
                if (response.decodeInt(PTPPacket.iPTPCommandARG0, ByteOrder.LittleEndian) == 0x10000000)
                    throw new CameraConnectionException(
                            "camera doesn't think it's capturing an image");
                else if (response.decodeInt(PTPPacket.iPTPCommandARG0, ByteOrder.LittleEndian) != 0) {
                	// how chdkptp does it: filename =
					// string.format('IMG_%04d',hdata.imgnum)
					// log.info("imgnum = " +
					// response.decodeInt(PTPPacket.iPTPCommandARG1,
					// ByteOrder.LittleEndian));
                	break;
                }                    
                else {
                    nTry++;
                    Thread.sleep(200);
                }

                if (nTry > 20) {
                    throw new CameraConnectionException("shoot fail. Try again");
                }
            }

            log.info("Camera is ready to send image!");

            PTPPacket getChunk = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND,
                    PTPPacket.PTP_OPPCODE_CHDK, 0, new byte[8]);

            getChunk.encodeInt(PTPPacket.iPTPCommandARG0, PTPPacket.CHDK_RemoteCaptureGetData,
                    ByteOrder.LittleEndian);

            getChunk.encodeInt(PTPPacket.iPTPCommandARG1, 1, // image type
                    ByteOrder.LittleEndian);

            // we need some sort of buffer here because ByteArrayOutputStream
            // doesnt work as we might seek back in
            // already written data
            byte[] image = new byte[0];
            int position = 0; // position in the buffer.
            while (true) {
                connection.sendPTPPacket(getChunk);
                // log.debug(getChunk);
                PTPPacket chunk = connection.getResponse();
                // log.debug(chunk);
                PTPPacket response = connection.getResponse();
                int offset = response.decodeInt(PTPPacket.iPTPCommandARG2, ByteOrder.LittleEndian);
                int chunkLength = chunk.getDataLength();
                // log.info("Got Image Chunk! size: " + chunkLength + " offset: " + offset + " last:?");

                if (offset != -1) {
                    position = offset;
                    // log.debug("Seeking");
                }
                int newSize = position + chunkLength;

                // hack for buffer resizing
                // might be resonable to grow and arraycopy, because usually an
                // image consists of 2 packets...
                if (newSize > image.length) {
                	byte[] imageTmpBigger = new byte[newSize];
                	System.arraycopy(image, 0, imageTmpBigger, 0, image.length);
                	image = imageTmpBigger;
                }

                System.arraycopy(chunk.getData(), 0, image, position, chunk.getDataLength());
                position += chunk.getDataLength();

                // log.debug(response);
                if (response.decodeInt(PTPPacket.iPTPCommandARG1, ByteOrder.LittleEndian) == 0
                        && response.decodeInt(PTPPacket.iPTPCommandARG0, ByteOrder.LittleEndian) == 0)
                    break;
            }
            log.info("Done!");
            return image;
        } catch (InvalidPacketException | PTPTimeoutException | InterruptedException e) {
            throw new CameraConnectionException(e.getMessage());
        }
    }

    @Override
    public BufferedImage getPicture() throws CameraConnectionException, CameraShootException {
        BufferedImage bImageFromConvert = null;
        InputStream in;
        try {
            log.info("converting to bufferedImage");
            in = new ByteArrayInputStream(getBytePicture());
            bImageFromConvert = ImageIO.read(in);
        } catch (CameraShootException | IOException e) {
            throw new CameraShootException(e.getMessage());
        }
        return bImageFromConvert;
    }

    private int cacheUsbCaptureSuport = -1;

    private int getUsbCaptureSuport() throws PTPTimeoutException, CameraConnectionException {
        if (cacheUsbCaptureSuport == -1) {
            cacheUsbCaptureSuport = (Integer) executeLuaQuery("return get_usb_capture_support()");
        }
        return cacheUsbCaptureSuport;
    }

    private byte cacheUsbCaptureSupport = -1;

	private UsbInterface interf;

    /**
     * 0 = not supported. 1 = supported
     * 
     * @return
     * @throws PTPTimeoutException
     * @throws GenericCameraException
     */
    private byte getUsbCaptureSupport() throws PTPTimeoutException, CameraConnectionException {
        if (cacheUsbCaptureSupport == -1) {
            if ("function".equals(executeLuaQuery("return type(init_usb_capture)"))) {
                cacheUsbCaptureSupport = 1;
            } else {
                cacheUsbCaptureSupport = 0;
            }
        }
        return cacheUsbCaptureSupport;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CameraMode getOperationMode() throws PTPTimeoutException, GenericCameraException {
        // see http://chdk.wikia.com/wiki/Lua/Lua_Reference#get_mode
        List<Object> r = (List<Object>) executeLuaQuery("return get_mode();");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return CameraMode.valueOf((boolean) r.get(0) ? 1 : 0);
    }

    @Override
    public void setOperationMode(CameraMode mode) throws PTPTimeoutException, GenericCameraException {
        if (getOperationMode() == mode) {
            return;
        }

        this.executeLuaCommand("set_record(" + mode.getValue() + ");");
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            throw new CameraConnectionException(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see chdk.ptp.java.ICamera#getFocusMode()
     */
    @Override
    public FocusMode getFocusMode() throws PTPTimeoutException, GenericCameraException {
        FocusMode focusMode = FocusMode.valueOf((int) executeLuaQuery("return get_focus_mode();"));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return focusMode;
    }

    @Override
    public int getFocus() throws PTPTimeoutException, GenericCameraException {
        int focus = (int) executeLuaQuery("return get_focus();");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return focus;
    }

    @Override
    public void setFocus(int focusingDistance) throws PTPTimeoutException, GenericCameraException {
        try {
            setFocusMode(FocusMode.MF);
            this.executeLuaCommand("set_focus(" + focusingDistance + ");");
            Thread.sleep(500);
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            throw new CameraConnectionException(e.getMessage());
        }
    }

    @Override
    public int getZoomSteps() throws PTPTimeoutException, GenericCameraException {
        if (zoomStepsCache == -1) {
            zoomStepsCache = (Integer) executeLuaQuery("return get_zoom_steps();");
        }
        return zoomStepsCache;
    }

    /*
     * (non-Javadoc)
     * 
     * @see chdk.ptp.java.ICamera#getZoom()
     */
    @Override
    public int getZoom() throws PTPTimeoutException, GenericCameraException {
        return (Integer) executeLuaQuery("return get_zoom();");
    }

    @Override
    public void setZoom(int zoomPosition) throws PTPTimeoutException, GenericCameraException {
		if (zoomPosition > getZoomSteps()) {
			return;
		}
        int waitTime = (int) ((Math.abs(zoomPosition - getZoom()) * 1.0 / getZoomSteps()) * 4000.0) + 500;
        this.executeLuaCommand("set_zoom(" + zoomPosition + ");");
        try {
            // need to wait for the operation to finish
            System.out.println(waitTime);
            Thread.sleep(waitTime);
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            throw new CameraConnectionException(e.getMessage());
        }
    }

    /**
     * Lock/unlock Canon manual focus (MF) mode.
     * 
     * Similar to set_aflock(), this function places the camera in a mode where SD override commands (e.g.
     * set_focus) can be used and the value requested will be maintained across multiple shots.
     * 
     * Note: works on almost all cameras, even those that do not have a Canon MF mode. More information here:
     * http://chdk.setepontos.com/index.php?topic=11078.0
     * 
     * @param lock
     *            true locks the camera in MF mode and false unlocks it.
     * 
     * @since CHDK 1.3.0 or later
     */
    public void set_mf(boolean lock) throws PTPTimeoutException, GenericCameraException {
        int r = (int) executeLuaQuery("return set_mf(" + (lock ? 1 : 0) + ");");
        switch (r) {
        case 1: // OK
            return;
        case 0: // not supported
            throw new GenericCameraException("set_mf is not supported");
        default:
            throw new GenericCameraException("Unknown result of set_mf: " + r);
        }
    }

    /**
     * Lock/unlock autofocus.
     * 
     * It is like Canon's built-in manual focus mode (MF), but better because focus stays locked even after
     * camera returns from deep display sleep (via display key cycle or print button shortcut), which is very
     * good for timelapse.
     * 
     * Note: In older versions of CHDK (prior to 1.3.0), this function does not work on all cameras and may
     * actually crash the camera. More information here: http://chdk.setepontos.com/index.php?topic=11078.0
     * See also {@link #set_mf(boolean)}.
     * 
     * @param lock
     *            true locks the autofocus and false unlocks it.
     */
    public void set_aflock(boolean lock) throws PTPTimeoutException, GenericCameraException {
        Object r = executeLuaQuery("return set_aflock(" + (lock ? 1 : 0) + ");");
        if (r != null) {
            throw new GenericCameraException("Unknown result of set_aflock: " + r);
        }
    }

    /**
     * Returns the propset number used by the camera. Returns 1 for propset1, 2 for propset2, 3 for propset3
     * etc. Useful for writing scripts that are not dependent on a particular camera model or DIGIC processor.
     * Information about values used for propsets can be found here: http://chdk.wikia.com/wiki/PropertyCase
     */
    public int get_propset() throws PTPTimeoutException, GenericCameraException {
        int r = (int) executeLuaQuery("return get_propset();");
        if (r < 1 || r > 10) {
            throw new GenericCameraException("Unknown result of get_propset: " + r);
        }
        return r;
    }

    /**
     * get_prop (and set_prop) can be used with propcase.lua library which is included in the 'complete'
     * download packages. This module loads a table which maps camera property case names the correct number
     * for a given camera.
     * 
     * There are several different "property sets" known for CHDK cameras: Propset 1 on most Digic II cameras,
     * propset 2 on most Digic III and some Digic IV, and further propsets on later Digic IV and Digic V.
     * During the CHDK build process a Lua library with a table of the property case names and numbers is
     * generated for each propset. These generated tables are saved in the \CHDK\LUALIB\GEN\ folder.
     */
    public int get_prop(int prop) throws PTPTimeoutException, GenericCameraException {
        return (int) executeLuaQuery("return get_prop(" + prop + ");");
    }

    private PTPConnection getConnectionFromUSBDevice(UsbDevice dev)
            throws UnsupportedEncodingException, UsbDisconnectedException, UsbException,
            CameraConnectionException {

        // device info
        log.info("Attepting to Connect to device");
        log.info("\tManufacturer:\t" + dev.getManufacturerString());
        log.info("\tSerial Number:\t" + dev.getSerialNumberString());

        // Get dev config
        UsbConfiguration config = dev.getActiveUsbConfiguration();
        log.info("\tGot Device Configuration:\t" + config);

        // Get interfaces
        List<?> totalInterfaces = config.getUsbInterfaces();
        UsbEndpoint camIn = null;
        UsbEndpoint camOut = null;
        for (int i = 0; i < totalInterfaces.size(); i++) {
            interf = (UsbInterface) totalInterfaces.get(i);
            log.info("\t\tFound Interface:\t" + interf);
            interf.claim();
            List<?> totalEndpoints = interf.getUsbEndpoints();

            for (int j = 0; j < totalEndpoints.size(); j++) {
                UsbEndpoint ep = (UsbEndpoint) totalEndpoints.get(j);
                // We're looking for a bulk In and bulk out
                if (ep.getDirection() == -128 && ep.getType() == 2) {
                    log.info("\t\t\tAssigning Bulk In endpoint #" + j + " to camIn");
                    camIn = ep; // Bulk IN endpoint
                }
                if (ep.getDirection() == 0 && ep.getType() == 2) {
                    log.info("\t\t\tAssigning Bulk OUT endpoint #" + j + " to camOut");
                    camOut = ep; // Bulk Out endpoint
                }

            }

        }
        if (camIn == null || camOut == null)
            throw new CameraConnectionException(
                    "Didn't find my endpoints Something verry bad happened..");
        log.info("\tFound my endpoints, Building PTPConnection");

        PTPConnection session = new PTPConnection(camIn, camOut);
        log.info("Camera is ready to recieve commands");
        return session;
    }

    @Override
    public ImageResolution getImageResolution() throws PTPTimeoutException, GenericCameraException {
        int res = (int) executeLuaQuery("return get_prop(222);");
        return ImageResolution.valueOf(res);
    }

    @Override
    public void setImageResolution(ImageResolution resolution) throws PTPTimeoutException,
            GenericCameraException {
        try {
            this.executeLuaCommand("set_prop(24, " + resolution.getValue() + ");");
            Thread.sleep(500);
            this.executeLuaCommand("set_prop(222, " + resolution.getValue() + ");");
            Thread.sleep(500);
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            throw new CameraConnectionException(e.getMessage());
        }
    }
    
    @Override
    public void clickButton(Button button) throws CameraConnectionException, PTPTimeoutException {
    	this.executeLuaCommand("click('" + button.getCommand() + "')");
    }

}
