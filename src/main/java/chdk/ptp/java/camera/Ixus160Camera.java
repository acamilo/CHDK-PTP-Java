package chdk.ptp.java.camera;

import chdk.ptp.java.SupportedCamera;
import chdk.ptp.java.exception.GenericCameraException;
import chdk.ptp.java.exception.PTPTimeoutException;
import chdk.ptp.java.model.FocusMode;
import java.util.logging.Logger;
import javax.usb.UsbDevice;

/** Canon Powershot ELPH 160 / IXUS 160 */
public class Ixus160Camera extends AbstractCamera {

  private Logger log = Logger.getLogger(Ixus160Camera.class.getName());

  public Ixus160Camera(UsbDevice device) {
    super(device);
  }

  /**
   * Creates a new instance of
   *
   * @param SerialNo canon camera serial number
   */
  public Ixus160Camera(String SerialNo) {
    super(SerialNo);
  }

  @Override
  public SupportedCamera getCameraInfo() {
    return SupportedCamera.IXUS160;
  }

  @Override
  public void setFocusMode(FocusMode mode) throws GenericCameraException, PTPTimeoutException {
    log.info("Dummy method here, won't do anything");
  }
}
