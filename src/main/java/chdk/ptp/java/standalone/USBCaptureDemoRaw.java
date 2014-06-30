package chdk.ptp.java.standalone;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import chdk.ptp.java.CameraFactory;
import chdk.ptp.java.ICamera;
import chdk.ptp.java.SupportedCamera;
import chdk.ptp.java.camera.AbstractCamera;
import chdk.ptp.java.connection.PTPConnection;
import chdk.ptp.java.connection.packet.ByteOrder;
import chdk.ptp.java.connection.packet.PTPPacket;

public class USBCaptureDemoRaw {
	static Logger log = Logger.getLogger(AbstractCamera.class.getName());
	private static ICamera cam;

	/**
	 * Runs the demo.
	 * 
	 * @param args
	 *            currently unused
	 * 
	 */
	public static void main(String[] args) {
		cam = null;
		try {
			cam = CameraFactory.getCamera(SupportedCamera.SX160IS);
			cam.connect();
			Thread.sleep(1000);

			// switch to capture mode
			cam.setRecordingMode();

			Thread.sleep(1000);

			Thread.sleep(1000);
			AbstractCamera acam = (AbstractCamera) cam;
			PTPConnection c = acam.getPTPConnection();
			//
			BufferedImagePanel impannel = null;
			int count = 0;

			while (true) {
				acam.executeLuaCommand("init_usb_capture(1,0,0)"); // init
																	// capture,
																	// we have 3
																	// seconds
																	// to take a
																	// picture
				acam.executeLuaCommand("shoot()"); // take picture
				Thread.sleep(1000); // there needs to be a delay between this
									// and the one below or camera will shut
									// down.
				// Loop until camera takes picture.
				PTPPacket ready = new PTPPacket(
						PTPPacket.PTP_USB_CONTAINER_COMMAND,
						PTPPacket.PTP_OPPCODE_CHDK, 0, new byte[8]);

				ready.encodeInt(PTPPacket.iPTPCommandARG0,
						PTPPacket.CHDK_RemoteCaptureIsReady,
						ByteOrder.LittleEndian);

				PTPPacket response;
				long timeout = System.currentTimeMillis();
				int retries = 0;
				while (true) {
					if (System.currentTimeMillis() > timeout + 2000) {
						if (retries > 9) {
							log.severe("Camera Won't shoot photo. Resetting Camera.");
							acam.executeLuaCommand("reboot()");
							throw new Exception("Camera Capture Failed");
						}
						log.warning("Camera ignored shoot command. Retrying.");
						timeout = System.currentTimeMillis();
						acam.executeLuaCommand("shoot()"); // take picture
						Thread.sleep(1000); // there needs to be a delay between
											// this and the one below or camera
											// will shut down.
						retries++;
					}
					c.sendPTPPacket(ready);
					// log.debug(ready);
					response = c.getResponse();
					// log.debug(response);
					if (response.decodeInt(PTPPacket.iPTPCommandARG0,
							ByteOrder.LittleEndian) == 0x10000000)
						throw new Exception(
								"balls. camera doesn't think it's capturing an image");// Camera
																						// says
																						// it's
																						// not
																						// capturing
					else if (response.decodeInt(PTPPacket.iPTPCommandARG0,
							ByteOrder.LittleEndian) != 0)
						break;

				}
				;

				log.info("Camera is ready to send image!");

				PTPPacket getChunk = new PTPPacket(
						PTPPacket.PTP_USB_CONTAINER_COMMAND,
						PTPPacket.PTP_OPPCODE_CHDK, 0, new byte[8]);

				getChunk.encodeInt(PTPPacket.iPTPCommandARG0,
						PTPPacket.CHDK_RemoteCaptureGetData,
						ByteOrder.LittleEndian);

				getChunk.encodeInt(PTPPacket.iPTPCommandARG1, 1, // image type
						ByteOrder.LittleEndian);

				PTPPacket chunk;
				byte[] image = new byte[10000000]; // this will need to get
													// bigger if the images are
													// more than 10mb
				int position = 0;
				while (true) {
					c.sendPTPPacket(getChunk);
					// log.debug(getChunk);
					chunk = c.getResponse();
					// log.debug(chunk);
					response = c.getResponse();
					int offset = response.decodeInt(PTPPacket.iPTPCommandARG2,
							ByteOrder.LittleEndian);
					log.info("Got Image Chunk! Offset: " + offset);
					if (offset != -1) {
						position = offset;
						// log.debug("Seeking");
					}
					System.arraycopy(chunk.getData(), 0, image, position,
							chunk.getDataLength());
					position += chunk.getDataLength();

					// log.debug(response);
					if (response.decodeInt(PTPPacket.iPTPCommandARG1,
							ByteOrder.LittleEndian) == 0
							&& response.decodeInt(PTPPacket.iPTPCommandARG0,
									ByteOrder.LittleEndian) == 0)
						break;
				}
				log.info("Done!");
				// log.debug(ReturnedImage.toByteArray().length);
				// c.getResponse();
				// BufferedOutputStream bos = new BufferedOutputStream(new
				// FileOutputStream(new File("test.jpg")));
				// bos.write(image);
				// bos.close();

				try {
					InputStream in = new ByteArrayInputStream(image);
					BufferedImage bImageFromConvert = ImageIO.read(in);
					BufferedImage resizedImage = new BufferedImage(1024, 768,
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = resizedImage.createGraphics();
					g.drawImage(bImageFromConvert, 0, 0, 1024, 768, null);
					g.dispose();

					log.info("Image Type: " + bImageFromConvert);
					if (impannel == null) {
						impannel = new BufferedImagePanel(resizedImage);
						// log.debug("null");
					} else {
						// log.debug("notnull");
						impannel.setImage(resizedImage);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				// impannel.setImage(resizedImage);
				log.info("Count is: " + count++);
				Thread.sleep(4000);
			}

			// cam.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
