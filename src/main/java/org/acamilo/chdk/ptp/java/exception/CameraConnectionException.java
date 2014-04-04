package org.acamilo.chdk.ptp.java.exception;

/**
 * Exception thrown when connection with camera experienced errors.
 */
public class CameraConnectionException extends Exception {

    /** Stores value of serialVersionUID */
    private static final long serialVersionUID = 1033821630668190615L;

    /**
     * Creates a new instance of
     * 
     * @param detailedMessage message about the error
     */
    public CameraConnectionException(String detailedMessage) {
        super(detailedMessage);
    }

}
