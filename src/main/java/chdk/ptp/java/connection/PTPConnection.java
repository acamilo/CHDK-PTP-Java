package chdk.ptp.java.connection;

import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbIrp;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotClaimedException;
import javax.usb.UsbNotOpenException;
import javax.usb.UsbPipe;

import chdk.ptp.java.connection.packet.PTPPacket;
import chdk.ptp.java.exception.CameraConnectionException;

public class PTPConnection {
    private static final String PTP_USB_CONTAINER_COMMAND = null;
    private int Seq = 0;
    UsbPipe camInpipe = null;
    UsbPipe camOutpipe = null;
    Boolean isConnected = false;
    UsbIrp read, write;
    byte[] recbuf = new byte[300000];

    public PTPConnection(String SerialNumber) {

    }

    public PTPConnection(UsbEndpoint camIn, UsbEndpoint camOut)
	    throws CameraConnectionException, UsbNotActiveException,
	    UsbNotClaimedException, UsbDisconnectedException, UsbException {
	camInpipe = camIn.getUsbPipe();
	camOutpipe = camOut.getUsbPipe();

	camInpipe.open();
	camOutpipe.open();

	if (camInpipe.isActive() == false || camOutpipe.isActive() == false)
	    throw new CameraConnectionException("Pipes not active.. Balls");

	System.out
		.println("Sending Begin session Command to chdk.ptp.java.connection");
	PTPPacket p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND,
		PTPPacket.PTP_OPPCODE_OpenSession, 0, new byte[] { 0x01, 0x00,
			0x00, 0x00 });
	this.sendPTPPacket(p);
	PTPPacket r = this.getResponse();
	if (r.getContainerCommand() == PTPPacket.PTP_USB_CONTAINER_RESPONSE
		&& r.getOppcode() == PTPPacket.PTP_OPPCODE_Response_OK)
	    return;
	throw new CameraConnectionException(
		"Camera Did not respond OK to our OpenSession Request");

    }

    public void sendPTPPacket(PTPPacket p) {
	try {

	    if (p.getContainerCommand() == p.PTP_USB_CONTAINER_COMMAND)
		Seq++;// if its a new outgoing command
		      // chdk.ptp.java.connection.packet, inc seq counter
	    // Send init command
	    long startTime = System.nanoTime();
	    p.setTransaction(Seq);
	    write = camOutpipe.createUsbIrp();
	    write.setData(p.getPacket());
	    write.setLength(p.getPacket().length);
	    write.setOffset(0);
	    write.setAcceptShortPacket(true);

	    camOutpipe.syncSubmit(write);
	    write.waitUntilComplete();
	    long stopTime = System.nanoTime();
	    // System.out.println("TX Delta:\t"+((stopTime -
	    // startTime)/(float)10000000)+"ms");
	    System.out.print(p);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public PTPPacket getResponse() {
	try {
	    long startTime = System.nanoTime();
	    if (camInpipe == null)
		throw new CameraConnectionException("My pipe is null.!");
	    read = camInpipe.createUsbIrp();
	    read.setData(recbuf);
	    read.setLength(recbuf.length);
	    read.setOffset(0);
	    read.setAcceptShortPacket(true);
	    if (read == null)
		throw new CameraConnectionException(
			"I tried to get a chdk.ptp.java.connection.packet from the in pipe and got null!");
	    camInpipe.syncSubmit(read);

	    PTPPacket response;
	    response = new PTPPacket(recbuf);
	    read.waitUntilComplete();
	    long stopTime = System.nanoTime();

	    // System.out.println("RX Delta:\t"+((stopTime -
	    // startTime)/(float)1000000)+"ms");
	    startTime = System.nanoTime();

	    byte[] image = response.getData();
	    stopTime = System.nanoTime();
	    // System.out.println("Copy:\t\t"+((stopTime -
	    // startTime)/(float)1000000)+"ms");
	    startTime = System.nanoTime();
	    System.out.print(response);
	    return response;
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return null;
    }

    public void close() throws UsbNotActiveException, UsbNotOpenException,
	    UsbDisconnectedException, UsbException {
	// TODO Auto-generated method stub
	this.camInpipe.close();
	this.camOutpipe.close();
    }

}
