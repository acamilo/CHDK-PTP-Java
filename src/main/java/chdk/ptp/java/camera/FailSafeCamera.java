package chdk.ptp.java.camera;

import chdk.ptp.java.SupportedCamera;
import chdk.ptp.java.exception.GenericCameraException;
import chdk.ptp.java.exception.PTPTimeoutException;
import chdk.ptp.java.model.FocusMode;
import java.util.logging.Logger;
import javax.usb.UsbDevice;

/**
 * Fail safe camera class which should at least show live view from camera.
 *
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 */
public class FailSafeCamera extends AbstractCamera {

  private Logger log = Logger.getLogger(FailSafeCamera.class.getName());

  public FailSafeCamera(UsbDevice device) {
    super(device);
  }

  /**
   * Creates a new instance of
   *
   * @param SerialNo canon camera serial number
   */
  public FailSafeCamera(String SerialNo) {
    super(SerialNo);
  }

  @Override
  public void setFocusMode(FocusMode mode) throws GenericCameraException, PTPTimeoutException {
    log.info("Dummy method here, won't do anything");
  }

  @Override
  public SupportedCamera getCameraInfo() {
    return SupportedCamera.FailsafeCamera;
  }
}
