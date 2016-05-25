package com.github.akinaru.gcrossclient.model;


/**
 * Event code enum.
 *
 * @author Bertrand Martel
 */
public enum EventCode {

    /**
     * default event code.
     */
    NONE(0),
    /**
     * registration is successful, device has been successfully added to server database.
     */
    REGISTRATION_SUCCESS(1),
    /**
     * registration is successful but device is already existing in server database.
     */
    ALREADY_REGISTERED(2),
    /**
     * token has not been specified in auth request.
     */
    TOKEN_REQUIRED(3),
    /**
     * hash has not been specified in auth request.
     */
    HASH_REQUIRED(4),
    /**
     * database error has occured.
     */
    DB_ERROR(5),
    /**
     * google JWT token verification has failed.
     */
    TOKEN_VERIFICATION_FAILURE(6),
    /**
     * google JWT verification request has failed.
     */
    VERIFICATION_REQUEST_ERROR(7),
    /**
     * user has sign out successfully from server.
     */
    SIGNOUT_SUCCESS(8),
    /**
     * device is not authorized and cant registrate to server.
     */
    DEVICE_NOT_AUTHORIZED(9);

    /**
     * event code value.
     */
    private int value;

    /**
     * Build event code enum.
     *
     * @param value event code value
     */
    EventCode(int value) {
        this.value = value;
    }

    /**
     * build event code from integer value.
     *
     * @param value integer value featuring the event code object
     * @return event code object
     */
    public static EventCode getStatusCode(int value) {
        for (EventCode status : EventCode.values()) {
            if (value == status.ordinal()) {
                return status;
            }
        }
        return EventCode.NONE;
    }
}
