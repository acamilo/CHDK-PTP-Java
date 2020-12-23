package chdk.ptp.java.camera;

import static chdk.ptp.java.model.FocusMode.AUTO;
import static chdk.ptp.java.model.FocusMode.INF;
import static chdk.ptp.java.model.FocusMode.MACRO;
import static chdk.ptp.java.model.FocusMode.MF;
import static chdk.ptp.java.model.FocusMode.SUPERMACRO;
import static chdk.ptp.java.model.FocusMode.UNKNOWN;

import chdk.ptp.java.SupportedCamera;
import chdk.ptp.java.exception.GenericCameraException;
import chdk.ptp.java.exception.PTPTimeoutException;
import chdk.ptp.java.model.FocusMode;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.usb.UsbDevice;

/** A2200Camera implementation. */
public class A2200Camera extends FailSafeCamera {

  private Logger log = Logger.getLogger(A2200Camera.class.getName());

  public A2200Camera(UsbDevice device) {
    super(device);
  }

  /**
   * Creates a new instance of
   *
   * @param SerialNo canon camera serial number
   */
  public A2200Camera(String SerialNo) {
    super(SerialNo);
  }

  @Override
  public SupportedCamera getCameraInfo() {
    return SupportedCamera.A2200;
  }

  @Override
  public void setFocusMode(FocusMode desiredMode)
      throws PTPTimeoutException, GenericCameraException {
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      log.log(Level.SEVERE, e.getLocalizedMessage(), e);
      throw new GenericCameraException(e.getLocalizedMessage());
    }
    int currentFocusMode = getFocusMode().getValue();

    // The A2200 only has three focus modes (in this order on display):
    // currentFocusMode in: "Macro" = 4 - "Normal" = 0 - "Infinity" = 3
    final int macro = 4;
    final int normal = 0;
    final int infinity = 3;

    // FocusMode.MACRO (4), "Normal" = FocusMode.AUTO (0), "Infinity" = FocusMode.INF (3)
    switch (desiredMode) {
      case AUTO:
        // set to "Normal" (there is no AUTO mode) and handle auto focus later during shoot
        // see https://chdk.fandom.com/wiki/Script_commands#set_aflock
        switch (currentFocusMode) {
          case macro:
            try {
              this.executeLuaCommand("click('left');");
              Thread.sleep(1000);
              this.executeLuaCommand("click('right');");
              Thread.sleep(500);
              this.executeLuaCommand("click('set');");
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              log.log(Level.SEVERE, e.getLocalizedMessage(), e);
              throw new GenericCameraException(e.getLocalizedMessage());
            }
            break;
          case normal:
            break;
          case infinity:
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
            break;
          default:
            throw new GenericCameraException(
                "Setting auto focus mode from state: " + currentFocusMode + "is not implemented");
        }
        // activate auto focus and focus
        try {
          this.executeLuaCommand("set_aflock(0);");
          this.executeLuaCommand("press('shoot_half');");
          Thread.sleep(800);
          this.executeLuaCommand("release('shoot_half');");
          this.executeLuaCommand("set_aflock(1);");
          Thread.sleep(1000);
        } catch (InterruptedException ex) {
          log.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
          throw new GenericCameraException(ex.getLocalizedMessage());
        }
        return;
      case INF:
        switch (currentFocusMode) {
          case macro:
            try {
              this.executeLuaCommand("click('left');");
              Thread.sleep(1000);
              this.executeLuaCommand("click('right');");
              Thread.sleep(500);
              this.executeLuaCommand("click('right');");
              Thread.sleep(500);
              this.executeLuaCommand("click('set');");
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              log.log(Level.SEVERE, e.getLocalizedMessage(), e);
              throw new GenericCameraException(e.getLocalizedMessage());
            }
            return;
          case normal:
            try {
              this.executeLuaCommand("click('left');");
              Thread.sleep(1000);
              this.executeLuaCommand("click('right');");
              Thread.sleep(500);
              this.executeLuaCommand("click('set');");
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              log.log(Level.SEVERE, e.getLocalizedMessage(), e);
              throw new GenericCameraException(e.getLocalizedMessage());
            }
            return;
          case infinity:
            return;
          default:
            throw new GenericCameraException(
                "Setting infinity focus mode from state: "
                    + currentFocusMode
                    + "is not implemented");
        }
      case MACRO:
        switch (currentFocusMode) {
          case macro:
            return;
          case normal:
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
          case infinity:
            try {
              this.executeLuaCommand("click('left');");
              Thread.sleep(1000);
              this.executeLuaCommand("click('left');");
              Thread.sleep(500);
              this.executeLuaCommand("click('left');");
              Thread.sleep(500);
              this.executeLuaCommand("click('set');");
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              log.log(Level.SEVERE, e.getLocalizedMessage(), e);
              throw new GenericCameraException(e.getLocalizedMessage());
            }
            return;
          default:
            throw new GenericCameraException(
                "Setting macro focus mode from state: " + currentFocusMode + "is not implemented");
        }
      case MF:
        // set to "Normal" (there is no MANUAL mode) and handle manual focus later during shoot
        switch (currentFocusMode) {
          case macro:
            try {
              this.executeLuaCommand("click('left');");
              Thread.sleep(1000);
              this.executeLuaCommand("click('right');");
              Thread.sleep(500);
              this.executeLuaCommand("click('set');");
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              log.log(Level.SEVERE, e.getLocalizedMessage(), e);
              throw new GenericCameraException(e.getLocalizedMessage());
            }
            return;
          case normal:
            return;
          case infinity:
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
          default:
            throw new GenericCameraException(
                "Setting auto focus mode from state: " + currentFocusMode + "is not implemented");
        }
      case SUPERMACRO:
      case UNKNOWN:
      default:
        break;
    }
  }

  @Override
  public void setFocus(int focusingDistance) throws PTPTimeoutException, GenericCameraException {
    try {
      this.executeLuaCommand("set_aflock(1);");
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      log.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      throw new GenericCameraException(ex.getLocalizedMessage());
    }
    super.setFocus(focusingDistance);
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
