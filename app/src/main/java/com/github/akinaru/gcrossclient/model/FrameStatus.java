package com.github.akinaru.gcrossclient.model;


/**
 * Frame status.
 *
 * @author Bertrand Martel
 */
public enum FrameStatus {

    /**
     * default frame status.
     */
    NONE(0),
    /**
     * success status.
     */
    SUCCESS(1),
    /**
     * error status.
     */
    ERRROR(2);

    /**
     * frame status value.
     */
    private int value;

    /**
     * Build frame satus object.
     *
     * @param value frame status value.
     */
    FrameStatus(int value) {
        this.value = value;
    }

    /**
     * Build frame status from integer value.
     *
     * @param value integer value featruring frame object
     * @return frame status object
     */
    public static FrameStatus get(int value) {

        for (FrameStatus frameStatus : FrameStatus.values()) {
            if (value == frameStatus.ordinal()) {
                return frameStatus;
            }
        }
        return FrameStatus.NONE;
    }
}
