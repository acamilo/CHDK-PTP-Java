package chdk.ptp.java;

/**
 * Contains a list of supported cameras Product IDs. Used for finding desired
 * connected USB device.
 * 
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 *
 */
public enum SupportedCamera {
    /** Stores value of SX160IS */
    SX160IS(0x325a),
    /** Stores value of SX50HS */
    SX50HS(0x3259),
    /** Stores value of FailsafeCamera */
    FailsafeCamera(-1);

    private int value;

    private SupportedCamera(int value) {
	this.value = value;
    }

    /**
     * Returns Canon camera USB PID
     * 
     * @return PID value
     */
    public short getPID() {
	return (short) value;
    }
}
