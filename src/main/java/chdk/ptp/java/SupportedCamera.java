package chdk.ptp.java;

import chdk.ptp.java.camera.A2200Camera;
import chdk.ptp.java.camera.FailSafeCamera;
import chdk.ptp.java.camera.SX160ISCamera;
import chdk.ptp.java.camera.SX50Camera;

/**
 * Contains a list of supported cameras Product IDs. Used for finding desired connected USB device.
 *
 * <p>see <a href="http://www.linux-usb.org/usb.ids">http://www.linux-usb.org/usb.ids</a>
 *
 * <p>Cannon has Vendor-ID "0x04a9".
 *
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 */
public enum SupportedCamera {
  /** Stores value of SX160IS */
  SX160IS(0x04a9, 0x325a, SX160ISCamera.class),
  /** Stores value of SX50HS */
  SX50HS(0x04a9, 0x3259, SX50Camera.class),
  /** Stores value of A2200 */
  A2200(0x04a9, 0x322a, A2200Camera.class),
  /** Stores value of FailsafeCamera */
  FailsafeCamera(-1, -1, FailSafeCamera.class);

  private short productId;
  private short vendorId;
  private Class<? extends ICamera> clazz;

  private SupportedCamera(int vendorId, int productId, Class<? extends ICamera> clazz) {
    this.productId = (short) productId;
    this.vendorId = (short) vendorId;
    this.clazz = clazz;
  }

  /**
   * Returns Canon camera USB PID
   *
   * @return PID value
   */
  public short getPID() {
    return productId;
  }

  /**
   * Returns Canon camera USB Vendor ID
   *
   * @return Vendor ID value
   */
  public short getVendorID() {
    return vendorId;
  }

  /** @return camera driver implementation */
  public Class<? extends ICamera> getClazz() {
    return clazz;
  }

  public boolean equals(short idVendor, short idProduct) {
    return vendorId == idVendor && idProduct == productId;
  }

  public static SupportedCamera getCamera(short idVendor, short idProduct) {
    for (SupportedCamera supportedCamera : values()) {
      if (supportedCamera.equals(idVendor, idProduct)) {
        return supportedCamera;
      }
    }
    return null;
  }

  public static boolean isSuportedCamera(short idVendor, short idProduct) {
    return getCamera(idVendor, idProduct) != null;
  }
}
