package chdk.ptp.java.exception;

/**
 * Exception thrown when camera device cannot be detected.
 */
public class CameraNotFoundException extends GenericCameraException {

    /**
     * Creates a new instance of
     *
     */
    public CameraNotFoundException() {
	super("Couldn't detect camera");
    }

    /**
     * Creates a new instance of
     * 
     * @param detailedMessage
     *            message about the error
     */
    public CameraNotFoundException(String detailedMessage) {
	super(detailedMessage);
    }

    /** Stores value of serialVersionUID */
    private static final long serialVersionUID = 5821648263055411431L;
}