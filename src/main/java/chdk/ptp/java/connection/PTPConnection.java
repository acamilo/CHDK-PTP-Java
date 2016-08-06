package chdk.ptp.java.connection;

import java.util.Arrays;

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
import chdk.ptp.java.exception.InvalidPacketException;
import chdk.ptp.java.exception.PTPTimeoutException;

public class PTPConnection {
    // private static final String PTP_USB_CONTAINER_COMMAND = null;
    private int Seq = 0;
    UsbPipe camInpipe = null;
    UsbPipe camOutpipe = null;
    Boolean isConnected = false;
    UsbIrp read, write;
    byte[] recbuf = new byte[43948288];

    public PTPConnection(String SerialNumber) {

    }

    public PTPConnection(UsbEndpoint camIn, UsbEndpoint camOut) throws CameraConnectionException,
            UsbNotActiveException, UsbNotClaimedException, UsbDisconnectedException, UsbException {
        camInpipe = camIn.getUsbPipe();
        camOutpipe = camOut.getUsbPipe();

        camInpipe.open();
        camOutpipe.open();

        if (camInpipe.isActive() == false || camOutpipe.isActive() == false)
            throw new CameraConnectionException("Pipes not active.. Balls");

        System.out.println("Sending Begin session Command to chdk.ptp.java.connection");
        PTPPacket p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND,
                PTPPacket.PTP_OPPCODE_OpenSession, 0, new byte[] { 0x01, 0x00, 0x00, 0x00 });
        this.sendPTPPacket(p);
        PTPPacket r;
        try {
            r = this.getResponse();
        } catch (InvalidPacketException | PTPTimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new CameraConnectionException("Camera returned a malformed packet or Timed Out");
        }
        if (r.getContainerCommand() == PTPPacket.PTP_USB_CONTAINER_RESPONSE
                && r.getOppcode() == PTPPacket.PTP_OPPCODE_Response_OK)
            return;
        throw new CameraConnectionException("Camera did not respond OK to our OpenSession Request");

    }

    public void sendPTPPacket(PTPPacket p) throws CameraConnectionException {
        try {

            if (p.getContainerCommand() == PTPPacket.PTP_USB_CONTAINER_COMMAND)
                Seq++;// if its a new outgoing command
            // chdk.ptp.java.connection.packet, inc seq counter
            // Send init command
            // long startTime = System.nanoTime();
            p.setTransaction(Seq);
            write = camOutpipe.createUsbIrp();
            write.setData(p.getPacket());
            write.setLength(p.getPacket().length);
            write.setOffset(0);
            write.setAcceptShortPacket(true);

            camOutpipe.syncSubmit(write);
            write.waitUntilComplete(10000);
            // long stopTime = System.nanoTime();
            // System.out.println("TX Delta:\t"+((stopTime -
            // startTime)/(float)10000000)+"ms");
            // System.out.print(p);
        } catch (Exception e) {
            
            throw new CameraConnectionException("Sending Packet to camera Timed Out");
        }
    }

    public PTPPacket getResponse() throws CameraConnectionException, InvalidPacketException,
            PTPTimeoutException {

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

        try {
            camInpipe.asyncSubmit(read);
        } catch (UsbNotActiveException | UsbNotOpenException | IllegalArgumentException
                | UsbDisconnectedException | UsbException e) {
            e.printStackTrace();
            throw new CameraConnectionException("USB exception!");
        }
        read.waitUntilComplete(10000); // so we don't block forever when the
                                       // camera poops itself and throw a
                                       // proper exception
        if (read.isComplete() == false)
            throw new PTPTimeoutException("Camera Reply Timeout");
        PTPPacket response;
        response = new PTPPacket(Arrays.copyOfRange(recbuf, 0, read.getActualLength())); // +40
                                                                                         // this
                                                                                         // is
                                                                                         // a
                                                                                         // hack
                                                                                         // to
                                                                                         // get
                                                                                         // it
                                                                                         // to
                                                                                         // kinda
                                                                                         // work
                                                                                         // right
                                                                                         // now.
                                                                                         // some
                                                                                         // of
                                                                                         // the
                                                                                         // packets
                                                                                         // come
                                                                                         // back
                                                                                         // malformed.
                                                                                         // if
                                                                                         // you
                                                                                         // retry
                                                                                         // it
                                                                                         // breaks.
                                                                                         // if
                                                                                         // you
                                                                                         // ignore
                                                                                         // it
                                                                                         // then
                                                                                         // it
                                                                                         // sometimes
                                                                                         // works
                                                                                         // and
                                                                                         // when
                                                                                         // it
                                                                                         // doesn't
                                                                                         // you
                                                                                         // can
                                                                                         // reset
                                                                                         // the
                                                                                         // camera.
                                                                                         // need
                                                                                         // to
                                                                                         // look
                                                                                         // into
                                                                                         // this.
        // read.waitUntilComplete(4000);
        return response;

    }

    public void close() throws UsbNotActiveException, UsbNotOpenException,
            UsbDisconnectedException, UsbException {
        this.camInpipe.close();
        this.camOutpipe.close();
        camInpipe.getUsbEndpoint().getUsbInterface().release();

    }

}
