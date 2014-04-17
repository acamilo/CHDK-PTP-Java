package chdk.ptp.java.exception;

/**
 * Generic exception thrown by this module
 * 
 * @author <a href="mailto:ankhazam@gmail.com">Mikolaj Dobski</a>
 *
 */
public class GenericCameraException extends Exception {

    /**
     * Creates a new instance of
     *
     * @param detailedMessage
     *            error comment
     */
    public GenericCameraException(String detailedMessage) {
	super(detailedMessage);
    }

    /** Stores value of serialVersionUID */
    private static final long serialVersionUID = 6493867117280218695L;

}
