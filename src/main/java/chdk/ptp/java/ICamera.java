package chdk.ptp.java;

import java.awt.image.BufferedImage;

import chdk.ptp.java.exception.CameraConnectionException;

/**
 * Standardized to Canon CHDK-enabled cameras.
 * 
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 *
 */
public interface ICamera {

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
     * @return true if response code was OK
     */
    public boolean executeLuaCommand(String command);

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
    public String executeLuaQuery(String command);

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
     * Switches a CHDK camera (eg. SX50HS) from MF to AF mode
     * 
     * @see <a
     *      href="http://chdk.wikia.com/wiki/CHDK_Manual_Focus_and_Subject_Distance_Overrides">CHDK
     *      toggle MF/AF</a>
     */
    public void setAutoFocusMode();

    /**
     * Switches a CHDK camera (eg. SX50HS) from AF to MF mode
     * 
     * @see <a
     *      href="http://chdk.wikia.com/wiki/CHDK_Manual_Focus_and_Subject_Distance_Overrides">CHDK
     *      toggle MF/AF</a>
     * 
     */
    public void setManualFocusMode();

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
     */
    public void setFocus(int focusingDistance);

    /**
     * Sets camera zoom to designated position.
     * 
     * @see <a
     *      href="http://chdk.wikia.com/wiki/CHDK_scripting#set_zoom_.2F_set_zoom_rel_.2F_get_zoom_.2F_set_zoom_speed">CHDK
     *      lua set_zoom()</a>
     * 
     * @param zoomPosition
     *            desired lens zoom position
     */
    public void setZoom(int zoomPosition);

    /**
     * Switches a CHDK camera (eg. SX50HS) into recording mode (image/video)
     * 
     * @see <a
     *      href="http://chdk.wikia.com/wiki/Script_commands#set_record.28state.29">CHDK
     *      set_record(1)</a>
     * 
     */
    public void setRecordingMode();

    /**
     * Switches a CHDK camera (eg. SX50HS) into Playback mode
     * 
     * @see <a
     *      href="http://chdk.wikia.com/wiki/Script_commands#set_record.28state.29">CHDK
     *      set_record(0)</a>
     * 
     */
    public void setPlaybackMode();

}