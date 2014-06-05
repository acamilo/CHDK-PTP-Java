package chdk.ptp.java.connection;

import javax.usb.UsbDevice;

public class CameraUsbDevice {
	private Short idVendor;
	private Short idProduct;
	private String productString;
	private UsbDevice device;

	public CameraUsbDevice(short idVendor, short idProduct,
			String productString, UsbDevice usbDevice) {
		this.idVendor = idVendor;
		this.idProduct = idProduct;
		this.productString = productString;
		this.device = usbDevice;
	}

	public Short getIdVendor() {
		return idVendor;
	}

	public void setIdVendor(Short idVendor) {
		this.idVendor = idVendor;
	}

	public Short getIdProduct() {
		return idProduct;
	}

	public void setIdProduct(Short idProduct) {
		this.idProduct = idProduct;
	}

	public String getProductString() {
		return productString;
	}

	public void setProductString(String productString) {
		this.productString = productString;
	}

	public UsbDevice getDevice() {
		return device;
	}

	public void setDevice(UsbDevice device) {
		this.device = device;
	}

	@Override
	public String toString() {
		return "CameraUsbDevice [idVendor=" + idVendor + ", idProduct="
				+ idProduct + ", productString=" + productString + "]";
	}

}
