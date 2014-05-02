package chdk.ptp.java.standalone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import chdk.ptp.java.CameraFactory;
import chdk.ptp.java.ICamera;
import chdk.ptp.java.SupportedCamera;
import chdk.ptp.java.camera.AbstractCamera;
import chdk.ptp.java.connection.PTPConnection;
import chdk.ptp.java.connection.packet.ByteOrder;
import chdk.ptp.java.connection.packet.PTPPacket;

public class USBCaptureDemoRaw {
	private static ICamera cam;
    /**
     * Runs the demo.
     * 
     * @param args
     *            currently unused
     * 
     */
    public static void main(String[] args) {
	cam = null;
	Log log = LogFactory.getLog(AbstractCamera.class);
	try {
	    cam = CameraFactory.getCamera(SupportedCamera.SX160IS);
	    cam.connect();
	    // switch to capture mode
	    cam.setRecordingMode();
	    AbstractCamera acam = (AbstractCamera) cam;
	    PTPConnection c = acam.getPTPConnection();
	    //
	    acam.executeLuaCommand("init_usb_capture(1,0,0)"); // init capture, we have 3 seconds to take a picture
	    Thread.sleep(100);
	    acam.executeLuaCommand("shoot()"); // take picture
	    
	    // Loop until camera takes picture.
	    PTPPacket ready = new PTPPacket(
	    		PTPPacket.PTP_USB_CONTAINER_COMMAND,
	    		PTPPacket.PTP_OPPCODE_CHDK,
	    		0,
	    		new byte[8]);
	    
	    ready.encodeInt(PTPPacket.iPTPCommandARG0,
	    		PTPPacket.CHDK_RemoteCaptureIsReady,
	    		ByteOrder.LittleEndian);
	    
	    PTPPacket response;
	    while(true){
	    	c.sendPTPPacket(ready);
	    	//log.debug(ready);
	    	response = c.getResponse();
	    	//log.debug(response);
	    	if (response.decodeInt(PTPPacket.iPTPCommandARG0, ByteOrder.LittleEndian) == 0x10000000) throw new Exception("balls. camera doesn't think it's capturing an image");// Camera says it's not capturing
	    	else if (response.decodeInt(PTPPacket.iPTPCommandARG0, ByteOrder.LittleEndian) != 0) break;
	    	
	    };
	    
	    log.debug("Camera is ready to send image!");
	    

	    PTPPacket getChunk = new PTPPacket(
	    		PTPPacket.PTP_USB_CONTAINER_COMMAND,
	    		PTPPacket.PTP_OPPCODE_CHDK,
	    		0,
	    		new byte[8]);
	    
	    getChunk.encodeInt(PTPPacket.iPTPCommandARG0,
	    		PTPPacket.CHDK_RemoteCaptureGetData,
	    		ByteOrder.LittleEndian);
	    
	    getChunk.encodeInt(PTPPacket.iPTPCommandARG1,
	    		1, // image type
	    		ByteOrder.LittleEndian);
	    
	    PTPPacket chunk;
	    while(true) {
	    	c.sendPTPPacket(getChunk);
	    	//log.debug(getChunk);
	    	chunk = c.getResponse();
	    	//log.debug(chunk);
	    	response = c.getResponse();
	    	log.debug("Got Image Chunk! Offset: "+response.decodeInt(PTPPacket.iPTPCommandARG2, ByteOrder.LittleEndian)  );
	    	//log.debug(response);
	    	if (response.decodeInt(PTPPacket.iPTPCommandARG1, ByteOrder.LittleEndian)==0 && response.decodeInt(PTPPacket.iPTPCommandARG0, ByteOrder.LittleEndian)==0) break;
	    }
	    log.debug("Done!");
	    
	    
	    
	    cam.disconnect();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
