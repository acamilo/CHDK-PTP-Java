package usb;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.swing.JFrame;
import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbServices;

import packet.CHDKScreenImage;
import packet.PTPPacket;

import camera.PTPSession;
import camera.displayImage;


public class USBImageHello {
	public static UsbDevice findDevice(UsbHub hub, short vendorId, short productId)
    {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())
        {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
            if (device.isUsbHub())
            {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null) return device;
            }
        }
        return null;
    }
    public static void main(String[] args) throws Exception
    {
        UsbServices services = UsbHostManager.getUsbServices();
        UsbHub rootHub = services.getRootUsbHub();
        UsbDevice dev = findDevice(rootHub,(short)0x04a9,(short)0x325a);
        if (dev==null) {
        	System.out.println("No Device Found!");
        	return;
        }
        System.out.println("Found Matching Device!:\t"+dev);
        

        try {
        	// device info
            System.out.println("\tManufacturer:\t"+dev.getManufacturerString());
            System.out.println("\tSerial Number:\t"+dev.getSerialNumberString());
			
			// Get dev config
	        UsbConfiguration config = dev.getActiveUsbConfiguration();
	        System.out.println("\tGot Device Configuration:\t"+config);
	        
	        // Get interfaces
	        List totalInterfaces = config.getUsbInterfaces();
            UsbEndpoint camIn=null;
            UsbEndpoint camOut=null;
	          for (int i=0; i<totalInterfaces.size(); i++)
	          {
	             UsbInterface interf = (UsbInterface) totalInterfaces.get(i);
	             System.out.println("\t\tFound Interface:\t"+interf);
	             interf.claim();
	             List totalEndpoints = interf.getUsbEndpoints();

	             for (int j=0; j<totalEndpoints.size(); j++)
	             {
	                UsbEndpoint ep = (UsbEndpoint) totalEndpoints.get(j);
	                //System.out.println("\t\t\tFound Endpoint:\n\t\t\t\t"+ep.getUsbEndpointDescriptor().toString().replaceAll("[\\n\\r]+", "\n\t\t\t\t"));
	                // We're looking for a bulk In and bulk out
	                if (ep.getDirection()==-128 && ep.getType()==2){
	                	System.out.println("\t\t\tAssigning Bulk In endpoint #"+j+" to camIn");
	                	camIn = ep; // Bulk IN endpoint
	                }
	                if (ep.getDirection()==0 && ep.getType()==2){
	                	System.out.println("\t\t\tAssigning Bulk OUT endpoint #"+j+" to camOut");
	                	camOut = ep; // Bulk Out endpoint
	                }
	                
	             }
	             
	             
	             
	          }
	        if (camIn==null || camOut==null) System.out.println("Didn't find my endpoints Something verry bad happened..");
	        else System.out.println("\tFound my endpoints, Building pipe");


	        PTPSession session = new PTPSession(camIn,camOut);

	        PTPPacket p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND, PTPPacket.PTP_OPPCODE_OpenSession, 0, new byte[]{0x01,0x00,0x00,0x00});
	        session.sendPTPPacket(p);
	        session.getResponse();
	        //while(true){
	        p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_COMMAND, PTPPacket.PTP_OPPCODE_CHDK, 1, new byte[]{12,0x00,0x00,0x00,(byte)(0x01),0x00,0x00,0x00});
	        session.sendPTPPacket(p);
	        /*
	        p = new PTPPacket(PTPPacket.PTP_USB_CONTAINER_DATA, PTPPacket.PTP_OPPCODE_CHDK, 2, 
	        		"switch_mode_usb(1)\0".getBytes());
	        session.sendPTPPacket(p);
	        */        


	        p=session.getResponse();
	        CHDKScreenImage i = new CHDKScreenImage(p.getData());
	        System.out.print(i);
	        
	        displayImage d = new displayImage(i);

	        
	        session.getResponse();
	        //}
	        
	        //session.close();
	        //System.out.println("Done");
		} catch (UnsupportedEncodingException | UsbDisconnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
    }

}
