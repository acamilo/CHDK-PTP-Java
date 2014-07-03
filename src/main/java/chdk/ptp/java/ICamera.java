package chdk.ptp.java;

import java.awt.image.BufferedImage;

import chdk.ptp.java.exception.CameraConnectionException;
import chdk.ptp.java.exception.GenericCameraException;
import chdk.ptp.java.exception.PTPTimeoutException;
import chdk.ptp.java.model.CameraMode;
import chdk.ptp.java.model.FocusMode;

/**
 * Standardized to Canon CHDK-enabled cameras.
 * 
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 * 
 */
public interface ICamera {

	/**
	 * @return access object to camera driver implementation
	 */
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
	 * @throws PTPTimeoutException
	 *             on error
	 * @throws GenericCameraException
	 *             on error
	 */
	public int executeLuaCommand(String command) throws PTPTimeoutException,
			GenericCameraException;

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
	 * @throws PTPTimeoutException
	 *             on error
	 * @throws GenericCameraException
	 *             on error
	 */
	public Object executeLuaQuery(String command) throws PTPTimeoutException,
			GenericCameraException;

	/**
	 * Takes picture with current camera settings and downloads the image.
	 * 
	 * @return taken picture image
	 * @throws GenericCameraException
	 *             on error
	 */
	public BufferedImage getPicture() throws GenericCameraException;

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
	 * @return Raw 'live view' image. suitable for openCV mat
	 * @throws CameraConnectionException
	 *             on error
	 */
	public BufferedImage getRawView() throws CameraConnectionException;

	/**
	 * Retrieves camera focusing mode
	 * 
	 * @return current focus mode
	 * 
	 * @throws PTPTimeoutException
	 *             on error
	 * @throws GenericCameraException
	 *             on error
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/Script_commands#get_focus_mode">CHDK
	 *      get_focus_mode()</a>
	 */
	public FocusMode getFocusMode() throws PTPTimeoutException,
			GenericCameraException;

	/**
	 * Switches a CHDK camera to selected focusing mode
	 * 
	 * @param mode
	 *            read {@link FocusMode}
	 * 
	 * @throws PTPTimeoutException
	 *             on error
	 * @throws GenericCameraException
	 *             on error
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/CHDK_Manual_Focus_and_Subject_Distance_Overrides">CHDK
	 *      toggle MF/AF</a>
	 */
	public void setFocusMode(FocusMode mode) throws PTPTimeoutException,
			GenericCameraException;

	/**
	 * Focuses the camera at selected distance
	 * 
	 * @return camera focusing distance
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/CHDK_Manual_Focus_and_Subject_Distance_Overrides">CHDK
	 *      manual focusing dispute</a>
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/CHDK_scripting#set_focus_.2F_get_focus">CHDK
	 *      get/set focus</a>
	 * 
	 * @throws PTPTimeoutException
	 *             on error
	 * @throws GenericCameraException
	 *             on error
	 */
	public int getFocus() throws PTPTimeoutException, GenericCameraException;

	/**
	 * Focuses the camera at selected distance
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/CHDK_Manual_Focus_and_Subject_Distance_Overrides">CHDK
	 *      manual focusing dispute</a>
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/CHDK_scripting#set_focus_.2F_get_focus">CHDK
	 *      get/set focus</a>
	 * 
	 * @param focusingDistance
	 *            desired focusing distance
	 * @throws PTPTimeoutException
	 *             on error
	 * @throws GenericCameraException
	 *             on error
	 */
	public void setFocus(int focusingDistance) throws PTPTimeoutException,
			GenericCameraException;

	/**
	 * Returns zoom position
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/CHDK_scripting#set_zoom_.2F_set_zoom_rel_.2F_get_zoom_.2F_set_zoom_speed">CHDK
	 *      lua get_zoom()</a>
	 * 
	 * @return number of maximum zoom steps
	 * @throws PTPTimeoutException
	 *             on error
	 * @throws GenericCameraException
	 *             on error
	 */
	public int getZoom() throws PTPTimeoutException, GenericCameraException;

	/**
	 * Sets camera zoom to designated position.
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/CHDK_scripting#set_zoom_.2F_set_zoom_rel_.2F_get_zoom_.2F_set_zoom_speed">CHDK
	 *      lua set_zoom()</a>
	 * 
	 * @param zoomPosition
	 *            desired lens zoom position
	 * @throws PTPTimeoutException
	 *             on error
	 * @throws GenericCameraException
	 *             on error
	 */
	public void setZoom(int zoomPosition) throws PTPTimeoutException,
			GenericCameraException;

	/**
	 * Returns number of maximum zoom steps, irrespective of the processor..
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/Script_commands#get_zoom_steps">CHDK
	 *      lua set_zoom()</a>
	 * 
	 * @return number of maximum zoom steps
	 * @throws PTPTimeoutException
	 *             on error
	 * @throws GenericCameraException
	 *             on error
	 */
	public int getZoomSteps() throws PTPTimeoutException,
			GenericCameraException;

	/**
	 * Get camera operation mode
	 * 
	 * @return camera mode
	 * @throws PTPTimeoutException
	 *             on error
	 * @throws GenericCameraException
	 *             on error
	 */
	public CameraMode getOperationMode() throws PTPTimeoutException,
			GenericCameraException;

	/**
	 * Switches a CHDK camera into a selected mode
	 * 
	 * @param mode
	 *            read {@link CameraMode}
	 * 
	 * @throws PTPTimeoutException
	 *             on error
	 * @throws GenericCameraException
	 *             on error
	 * 
	 * @see <a
	 *      href="http://chdk.wikia.com/wiki/Script_commands#set_record.28state.29">CHDK
	 *      set_record(0)</a>
	 * 
	 */
	public void setOperaionMode(CameraMode mode) throws PTPTimeoutException,
			GenericCameraException;

}