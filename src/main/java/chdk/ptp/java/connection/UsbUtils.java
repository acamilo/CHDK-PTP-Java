package chdk.ptp.java.connection;

import chdk.ptp.java.ICamera;
import chdk.ptp.java.SupportedCamera;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.usb.UsbConfiguration;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbServices;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

/**
 * Various USB utilities
 *
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 * @author Aleś Bułojčyk (alex73mail@gmail.com)
 */
public class UsbUtils {

  private static Logger log = Logger.getLogger(UsbUtils.class.getName());

  /**
   * Searches for a device with matching vendor and product id in given hub
   *
   * @param hub to search in
   * @param vendorId to search for
   * @param productId to search for
   * @return matching device or null
   */
  @SuppressWarnings("unchecked")
  public static UsbDevice findDevice(UsbHub hub, short vendorId, short productId) {
    log.info("Processing Attatched devices for " + hub);
    for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
      UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
      log.info("Checking out " + device);
      if (desc.idVendor() == vendorId && desc.idProduct() == productId) {
        kernelDetatch(device);
        return device;
      }
      if (device.isUsbHub()) {
        device = findDevice((UsbHub) device, vendorId, productId);
        if (device != null) {
          kernelDetatch(device);
          return device;
        }
      }
    }
    return null;
  }

  public static Device findDevice(String seriualNumber) {
    // if(!OsInfoUtil.isWindows()){
    // Read the USB device list
    DeviceList list = new DeviceList();
    int result = LibUsb.getDeviceList(null, list);
    if (result < 0) throw new LibUsbException("Unable to get device list", result);

    try {
      // Iterate over all devices and scan for the right one
      for (Device device : list) {

        DeviceDescriptor descriptor = new DeviceDescriptor();
        result = LibUsb.getDeviceDescriptor(device, descriptor);
        if (result != LibUsb.SUCCESS)
          throw new LibUsbException("Unable to read device descriptor", result);
        DeviceHandle handle = new DeviceHandle();
        result = LibUsb.open(device, handle);
        if (result == LibUsb.SUCCESS) {
          String sn = LibUsb.getStringDescriptor(handle, descriptor.iSerialNumber()).trim();
          LibUsb.close(handle);
          if (sn.contains(seriualNumber.trim())) {

            return device;
          }
        }
      }
    } finally {
      // Ensure the allocated device list is freed
      LibUsb.freeDeviceList(list, true);
    }
    // }
    // Device not found
    return null;
  }

  @SuppressWarnings("unchecked")
  private static void kernelDetatch(UsbDevice mDevice) {
    // if(!OsInfoUtil.isWindows()){
    Device kDev = null;
    try {
      kDev = findDevice(mDevice.getSerialNumberString());
    } catch (UnsupportedEncodingException | UsbDisconnectedException | UsbException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (kDev == null) return;

    DeviceHandle deviceHandle = new DeviceHandle();
    for (UsbConfiguration configuration : (List<UsbConfiguration>) mDevice.getUsbConfigurations()) {
      // Process all interfaces
      for (UsbInterface iface : (List<UsbInterface>) configuration.getUsbInterfaces()) {
        int interfaceNumber = iface.getUsbInterfaceDescriptor().bInterfaceNumber();

        int result = LibUsb.open(kDev, deviceHandle);
        if (result != LibUsb.SUCCESS)
          throw new LibUsbException("Unable to open USB device", result);

        int r = LibUsb.detachKernelDriver(deviceHandle, interfaceNumber);
        if (r != LibUsb.SUCCESS && r != LibUsb.ERROR_NOT_SUPPORTED && r != LibUsb.ERROR_NOT_FOUND)
          throw new LibUsbException("Unable to detach kernel     driver", r);
        System.out.println("Camera CHDK Kernel Takeover");
      }
    }

    // System.out.println("Kernel detatched for device "+mDevice);
    // }
  }

  /**
   * Searches for a device with matching serial number in given USB hub
   *
   * @param hub to search in
   * @param serialNo to search for
   * @return matching device or null
   */
  @SuppressWarnings("unchecked")
  public static UsbDevice findDeviceBySerialNumber(UsbHub hub, String serialNo) {
    log.info("Processing Attatched devices for " + hub);
    for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
      log.info("Checking out " + device);
      if (device.isUsbHub()) {
        device = findDeviceBySerialNumber((UsbHub) device, serialNo);
        if (device != null) {
          kernelDetatch(device);
          return device;
        }
      }
      try {
        if (device != null
            && device.getUsbDeviceDescriptor().bDeviceClass() != 0
            && device.getManufacturerString().equals(serialNo)) {
          kernelDetatch(device);
          return device;
        }
      } catch (UnsupportedEncodingException | UsbDisconnectedException | UsbException e) {
        log.log(Level.SEVERE, e.getLocalizedMessage(), e);
        e.printStackTrace();
      }
    }
    return null;
  }

  public static ICamera findCamera() throws SecurityException, UsbException {
    Collection<CameraUsbDevice> cams = listAttachedCameras();

    if (cams.size() > 0) {
      // picks the first
      CameraUsbDevice cameraDevice = cams.iterator().next();
      SupportedCamera sc =
          SupportedCamera.getCamera(cameraDevice.getIdVendor(), cameraDevice.getIdProduct());

      try {
        return sc.getClazz().getConstructor(UsbDevice.class).newInstance(cameraDevice.getDevice());
      } catch (InstantiationException
          | IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException
          | NoSuchMethodException e) {
        log.log(Level.SEVERE, e.getLocalizedMessage(), e);
      }
    }
    return null;
  }

  /** Multiple cameras initialization support. */
  public static Collection<ICamera> findCameras() throws SecurityException, UsbException {
    List<ICamera> result = new ArrayList<>();

    for (CameraUsbDevice cameraDevice : listAttachedCameras()) {
      SupportedCamera sc =
          SupportedCamera.getCamera(cameraDevice.getIdVendor(), cameraDevice.getIdProduct());

      try {
        ICamera camera =
            sc.getClazz().getConstructor(UsbDevice.class).newInstance(cameraDevice.getDevice());
        result.add(camera);
      } catch (InstantiationException
          | IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException
          | NoSuchMethodException e) {
        log.log(Level.SEVERE, e.getLocalizedMessage(), e);
      }
    }
    return result;
  }

  /** Find all attached USB devices. */
  public static Collection<UsbDevice> listAttachedDevices() throws SecurityException, UsbException {
    UsbServices services = UsbHostManager.getUsbServices();
    UsbHub rootHub = services.getRootUsbHub();
    List<UsbDevice> result = new ArrayList<>();
    listAttachedDevices(rootHub, result);
    return result;
  }

  @SuppressWarnings("unchecked")
  private static void listAttachedDevices(UsbHub dev, List<UsbDevice> result) {
    List<UsbDevice> devices = dev.getAttachedUsbDevices();
    for (UsbDevice usbDevice : devices) {
      if (usbDevice.isUsbHub()) {
        listAttachedDevices((UsbHub) usbDevice, result);
      } else {
        result.add(usbDevice);
      }
    }
  }

  public static Collection<CameraUsbDevice> listAttachedCameras()
      throws SecurityException, UsbException {
    UsbServices services = UsbHostManager.getUsbServices();
    UsbHub rootHub = services.getRootUsbHub();
    return listAttachedCameras(rootHub);
  }

  @SuppressWarnings("unchecked")
  private static Collection<CameraUsbDevice> listAttachedCameras(UsbHub dev) {
    List<CameraUsbDevice> list = new ArrayList<>();

    List<UsbDevice> devices = dev.getAttachedUsbDevices();
    for (UsbDevice usbDevice : devices) {
      UsbDeviceDescriptor desc = usbDevice.getUsbDeviceDescriptor();
      if (usbDevice.isUsbHub()) {
        list.addAll(listAttachedCameras((UsbHub) usbDevice));
      } else {
        if (SupportedCamera.isSuportedCamera(desc.idVendor(), desc.idProduct())) {
          try {
            list.add(
                new CameraUsbDevice(
                    desc.idVendor(), desc.idProduct(), usbDevice.getProductString(), usbDevice));
          } catch (UnsupportedEncodingException | UsbDisconnectedException | UsbException e1) {
            log.info(e1.getLocalizedMessage());
          }
        }
      }
    }

    return list;
  }
}
