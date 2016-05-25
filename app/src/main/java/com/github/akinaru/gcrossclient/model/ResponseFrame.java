package com.github.akinaru.gcrossclient.model;

/**
 * Response frame used to represent authentication response object.
 *
 * @author Bertrand Martel
 */
public class ResponseFrame {

    /**
     * request status (success/error).
     */
    private FrameStatus status = FrameStatus.NONE;

    /**
     * event code.
     */
    private EventCode eventCode = EventCode.NONE;

    /**
     * message.
     */
    private String message;

    /**
     * Build Response frame object.
     *
     * @param status  request status
     * @param code    event code
     * @param message request message
     */
    public ResponseFrame(FrameStatus status, EventCode code, String message) {
        this.status = status;
        this.message = message;
        this.eventCode = code;
    }

    /**
     * get request status.
     *
     * @return
     */
    public FrameStatus getStatus() {
        return status;
    }

    /**
     * get request message.
     *
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * get event code.
     *
     * @return
     */
    public EventCode getEventCode() {
        return eventCode;
    }
}
