package chdk.ptp.java;

import chdk.ptp.java.camera.FailSafeCamera;
import chdk.ptp.java.camera.SX160ISCamera;
import chdk.ptp.java.camera.SX50Camera;

/**
 * Contains a list of supported cameras Product IDs. Used for finding desired
 * connected USB device.
 * 
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 * 
 */
public enum SupportedCamera {
	/** Stores value of SX160IS */
	SX160IS(0x04a9, 0x325a, SX160ISCamera.class),
	/** Stores value of SX50HS */
	SX50HS(0x04a9, 0x3259, SX50Camera.class),
	/** Stores value of FailsafeCamera */
	FailsafeCamera(-1, -1, FailSafeCamera.class);

	private short pid;
	private short vendoId;
	private Class<? extends ICamera> clazz;

	private SupportedCamera(int vendoId, int pid, Class<? extends ICamera> clazz) {
		this.pid = (short) pid;
		this.vendoId = (short) vendoId;
		this.clazz = clazz;
	}

	/**
	 * Returns Canon camera USB PID
	 * 
	 * @return PID value
	 */
	public short getPID() {
		return pid;
	}
	
	/**
	 * Returns Canon camera USB Vendor ID
	 * 
	 * @return Vendor ID value
	 */
	public short getVendorID() {
		return vendoId;
	}
	
	public Class<? extends ICamera> getClazz(){
		return clazz;
	}
	
	public boolean equals(short idVendor, short idProduct){
		return vendoId == idVendor && idProduct == pid;
	}

	
	public static SupportedCamera getCamera(short idVendor, short idProduct) {
		for (SupportedCamera supportedCamera : values()) {
			if(supportedCamera.equals(idVendor, idProduct)){
				return supportedCamera;
			}
		}
		return null;
	}
	
	public static boolean isSuportedCamera(short idVendor, short idProduct) {
		return getCamera(idVendor, idProduct) != null;
	}
}