package chdk.ptp.java.camera;

import chdk.ptp.java.SupportedCamera;
import chdk.ptp.java.exception.GenericCameraException;
import chdk.ptp.java.exception.PTPTimeoutException;
import chdk.ptp.java.model.FocusMode;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.usb.UsbDevice;

/** SX50Camera implementation. */
public class SX50Camera extends FailSafeCamera {

  private Logger log = Logger.getLogger(SX50Camera.class.getName());

  public SX50Camera(UsbDevice device) {
    super(device);
  }

  /**
   * Creates a new instance of
   *
   * @param SerialNo canon camera serial number
   */
  public SX50Camera(String SerialNo) {
    super(SerialNo);
  }

  @Override
  public SupportedCamera getCameraInfo() {
    return SupportedCamera.SX50HS;
  }

  @Override
  public void setFocusMode(FocusMode desiredMode)
      throws PTPTimeoutException, GenericCameraException {
    FocusMode currentFocusMode = getFocusMode();
    switch (desiredMode) {
      case AUTO:
        switch (currentFocusMode) {
          case AUTO:
            return;
          case MF:
            try {
              this.executeLuaCommand("click('left');");
              Thread.sleep(1000);
              this.executeLuaCommand("click('left');");
              Thread.sleep(500);
              this.executeLuaCommand("click('set');");
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              log.log(Level.SEVERE, e.getLocalizedMessage(), e);
              throw new GenericCameraException(e.getLocalizedMessage());
            }
            return;
          case INF:
          case MACRO:
          case SUPERMACRO:
          case UNKNOWN:
          default:
            throw new GenericCameraException(
                "Setting auto focus mode from state: " + currentFocusMode + "is not implemented");
        }
      case INF:
      case MACRO:
      case MF:
        switch (currentFocusMode) {
          case AUTO:
            try {
              this.executeLuaCommand("click('left');");
              Thread.sleep(1000);
              this.executeLuaCommand("click('right');");
              Thread.sleep(500);
              this.executeLuaCommand("click('set');");
              Thread.sleep(1000);
              this.executeLuaCommand("click('set');");
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              log.log(Level.SEVERE, e.getLocalizedMessage(), e);
              throw new GenericCameraException(e.getLocalizedMessage());
            }
            return;
          case MF:
            return;
          case INF:
          case MACRO:
          case SUPERMACRO:
          case UNKNOWN:
          default:
            throw new GenericCameraException(
                "Setting manual focus mode from state: " + currentFocusMode + "is not implemented");
        }
      case SUPERMACRO:
      case UNKNOWN:
      default:
        break;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see chdk.ptp.java.camera.AbstractCamera#setZoom(int)
   */
  @Override
  public void setZoom(int zoomPosition) throws PTPTimeoutException, GenericCameraException {
    // need to switch to AF or camera would crash
    setFocusMode(FocusMode.AUTO);
    // set zoom
    super.setZoom(zoomPosition);
  }
}
