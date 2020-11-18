package chdk.ptp.java;

import chdk.ptp.java.camera.FailSafeCamera;
import chdk.ptp.java.camera.SX160ISCamera;
import chdk.ptp.java.camera.SX50Camera;
import chdk.ptp.java.connection.UsbUtils;
import chdk.ptp.java.exception.CameraNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbServices;

/**
 * @author ankhazam@gmail.com Mikolaj Dobski
 * @author Aleś Bułojčyk (alex73mail@gmail.com)
 */
public class CameraFactory {

  private static Logger log = Logger.getLogger(CameraFactory.class.getName());

  /** Find any known cameras. */
  public static Collection<ICamera> getCameras() throws Exception {
    List<ICamera> cameras = new ArrayList<>();
    // iterate by all USB devices
    for (UsbDevice usbDevice : UsbUtils.listAttachedDevices()) {
      UsbDeviceDescriptor desc = usbDevice.getUsbDeviceDescriptor();
      SupportedCamera sc = SupportedCamera.getCamera(desc.idVendor(), desc.idProduct());
      if (sc != null) {
        // known camera - create specified class
        try {
          ICamera camera = sc.getClazz().getConstructor(UsbDevice.class).newInstance(usbDevice);
          cameras.add(camera);
        } catch (Exception ex) {
          log.log(Level.SEVERE, "Error instantiate camera class", ex);
          throw ex;
        }
      }
    }
    return cameras;
  }

  /** Find specified cameras. */
  public static Collection<ICamera> getCameras(short idVendor, short idProduct) throws Exception {
    List<ICamera> cameras = new ArrayList<>();
    for (UsbDevice usbDevice : UsbUtils.listAttachedDevices()) {
      UsbDeviceDescriptor desc = usbDevice.getUsbDeviceDescriptor();
      if (desc.idVendor() == idVendor && desc.idProduct() == idProduct) {
        // requested vendor/product
        SupportedCamera sc = SupportedCamera.getCamera(desc.idVendor(), desc.idProduct());
        if (sc == null) {
          // unknown camera
          sc = SupportedCamera.FailsafeCamera;
        }
        // known camera - create specified class
        try {
          ICamera camera = sc.getClazz().getConstructor(UsbDevice.class).newInstance(usbDevice);
          cameras.add(camera);
        } catch (Exception ex) {
          log.log(Level.SEVERE, "Error instantiate camera class", ex);
          throw ex;
        }
      }
    }
    return cameras;
  }

  /**
   * Find known camera. It returns only first one if multiple cameras found.
   *
   * @throws CameraNotFoundException when no supported camera is found
   */
  public static ICamera getCamera() throws Exception {
    Collection<ICamera> cameras = getCameras();
    if (cameras.isEmpty()) {
      throw new CameraNotFoundException();
    }
    return cameras.iterator().next();
  }

  /**
   * Find specified camera. It returns only first one if multiple cameras found.
   *
   * @throws CameraNotFoundException when no supported camera is found
   */
  public static ICamera getCamera(short idVendor, short idProduct) throws Exception {
    Collection<ICamera> cameras = getCameras(idVendor, idProduct);
    if (cameras.isEmpty()) {
      throw new CameraNotFoundException();
    }
    return cameras.iterator().next();
  }

  /**
   * Attempts to get known camera implementation or backs up to failsafe camera with liveview
   * support
   *
   * @param cameraModel selected camera model
   * @return configured camera object
   * @throws CameraNotFoundException on error
   */
  public static ICamera getCamera(SupportedCamera cameraModel) throws CameraNotFoundException {
    UsbDevice cameraDevice = null;
    try {

      UsbServices services = UsbHostManager.getUsbServices();
      UsbHub rootHub = services.getRootUsbHub();
      cameraDevice = UsbUtils.findDevice(rootHub, cameraModel.getVendorID(), cameraModel.getPID());

    } catch (SecurityException | UsbException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      e.printStackTrace();
      throw new CameraNotFoundException();
    }
    if (cameraDevice == null) throw new CameraNotFoundException();

    switch (cameraModel) {
      case SX50HS:
        return new SX50Camera(cameraDevice);
      case SX160IS:
        return new SX160ISCamera(cameraDevice);
      case FailsafeCamera:
      default:
        return new FailSafeCamera(cameraDevice);
    }
  }

  /**
   * Attempts to get known camera implementation for given serialNumber, backs up to failsafe camera
   * with liveview support
   *
   * @param serialNumber camera serial number
   * @return configured camera object
   * @throws CameraNotFoundException on error
   */
  public static ICamera getCamera(String serialNumber) throws CameraNotFoundException {
    UsbDevice cameraDevice = null;
    try {
      UsbServices services = UsbHostManager.getUsbServices();
      UsbHub rootHub = services.getRootUsbHub();
      cameraDevice = UsbUtils.findDeviceBySerialNumber(rootHub, serialNumber);
    } catch (SecurityException | UsbException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      throw new CameraNotFoundException(e.getMessage());
    }
    if (cameraDevice == null) throw new CameraNotFoundException();

    // try to map product id to known implementations
    SupportedCamera discoveredCameraModel = SupportedCamera.FailsafeCamera;
    for (SupportedCamera cameraModel : SupportedCamera.values()) {
      if (cameraModel.getPID() == cameraDevice.getUsbDeviceDescriptor().idProduct())
        discoveredCameraModel = cameraModel;
      break;
    }

    switch (discoveredCameraModel) {
      case SX50HS:
        return new SX50Camera(serialNumber);
      case SX160IS:
      case FailsafeCamera:
      default:
        return new FailSafeCamera(serialNumber);
    }
  }
}
