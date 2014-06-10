package chdk.ptp.java;

import java.awt.image.BufferedImage;

import chdk.ptp.java.exception.CameraConnectionException;
import chdk.ptp.java.exception.PTPTimeoutException;

/**
 * Standardized to Canon CHDK-enabled cameras.
 * 
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 * 
 */
public interface ICamera {

	SupportedCamera getCameraInfo(); 
	/**
	 * Connect via PTP to camera.
	 * 
	 * @throws CameraConnectionException
	 *             on error
	 */
	public void connect() throws CameraConnectionException;

	/**
	 * Disconnects from camera.
	 * 
	 * @throws CameraConnectionException
	 *             on error
	 */
	public void disconnect() throws CameraConnectionException;

	/**
	 * Submits provided Lua command for execution on camera.
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/CHDK_Scripting_Cross_Reference_Page">CHDK
	 *      scripting reference</a>
	 * 
	 * @param command
	 *            to be issued
	 * 
	 * @return script id. -1 if script fail
	 * @throws CameraConnectionException
	 * @throws PTPTimeoutException
	 */
	public int executeLuaCommand(String command)
			throws CameraConnectionException, PTPTimeoutException;

	/**
	 * Submits provided Lua command for execution on camera, expects result to
	 * be returned by camera
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/CHDK_Scripting_Cross_Reference_Page">CHDK
	 *      scripting reference</a>
	 * 
	 * @param command
	 *            to be issued
	 * 
	 * @return returned value
	 */
	public Object executeLuaQuery(String command) throws CameraConnectionException, PTPTimeoutException;

	/**
	 * Takes picture with current camera settings and downloads the image.
	 * 
	 * @return taken picture image
	 * @throws CameraConnectionException
	 *             on error
	 */
	public BufferedImage getPicture() throws CameraConnectionException;

	/**
	 * Downloads camera display content, might be interpreted as Live View
	 * 
	 * @return 'live view' image
	 * @throws CameraConnectionException
	 *             on error
	 */
	public BufferedImage getView() throws CameraConnectionException;

	/**
	 * Downloads camera display content, might be interpreted as Live View
	 * 
	 * @return Raw 'live view' image. sutable for openCV mat
	 * @throws CameraConnectionException
	 *             on error
	 */
	public BufferedImage getRawView() throws CameraConnectionException;

	/**
	 * Switches a CHDK camera (eg. SX50HS) from MF to AF mode
	 * 
	 * @throws CameraConnectionException
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/CHDK_Manual_Focus_and_Subject_Distance_Overrides">CHDK
	 *      toggle MF/AF</a>
	 */
	public void setAutoFocusMode() throws CameraConnectionException;

	/**
	 * Switches a CHDK camera (eg. SX50HS) from AF to MF mode
	 * 
	 * @throws CameraConnectionException
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/CHDK_Manual_Focus_and_Subject_Distance_Overrides">CHDK
	 *      toggle MF/AF</a>
	 * 
	 */
	public void setManualFocusMode() throws CameraConnectionException;

	/**
	 * Focuses the camera at selected distance
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/CHDK_Manual_Focus_and_Subject_Distance_Overrides">CHDK
	 *      manual focusing dispute</a>
	 * 
	 * 
	 * @param focusingDistance
	 *            desired focusing distance
	 * @throws CameraConnectionException
	 * @throws PTPTimeoutException
	 */
	public void setFocus(int focusingDistance)
			throws CameraConnectionException, PTPTimeoutException;

	/**
	 * Sets camera zoom to designated position.
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/CHDK_scripting#set_zoom_.2F_set_zoom_rel_.2F_get_zoom_.2F_set_zoom_speed">CHDK
	 *      lua set_zoom()</a>
	 * 
	 * @param zoomPosition
	 *            desired lens zoom position
	 * @throws CameraConnectionException
	 * @throws PTPTimeoutException
	 */
	public void setZoom(int zoomPosition) throws CameraConnectionException,
			PTPTimeoutException;
	
	
	/**
	 * Returns number of maximum zoom steps, irrespective of the processor..
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/Script_commands#get_zoom_steps">CHDK
	 *      lua set_zoom()</a>
	 * 
	 * @return number of maximum zoom steps
	 * @throws CameraConnectionException
	 * @throws PTPTimeoutException
	 */
	public int getZoomSteps() throws CameraConnectionException,
			PTPTimeoutException;

	/**
	 * Switches a CHDK camera (eg. SX50HS) into recording mode (image/video)
	 * 
	 * @throws CameraConnectionException
	 * @throws PTPTimeoutException
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/Script_commands#set_record.28state.29">CHDK
	 *      set_record(1)</a>
	 * 
	 */
	public void setRecordingMode() throws CameraConnectionException,
			PTPTimeoutException;

	/**
	 * Switches a CHDK camera (eg. SX50HS) into Playback mode
	 * 
	 * @throws CameraConnectionException
	 * @throws PTPTimeoutException
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/Script_commands#set_record.28state.29">CHDK
	 *      set_record(0)</a>
	 * 
	 */
	public void setPlaybackMode() throws CameraConnectionException,
			PTPTimeoutException;

}