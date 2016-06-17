package com.github.akinaru.gcrossclient.model;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Create / Parse uplink/downlink requests.
 *
 * @author Bertrand Martel
 */
public class ComModel {

    /**
     * message field for auth response.
     */
    private static final String RESPONSE_FIELD_MESSAGE = "message";

    private static final String RESPONSE_FIELD_DEVICEID = "deviceId";

    /**
     * status field for auth response.
     */
    private static final String RESPONSE_FIELD_STATUS = "status";

    /**
     * event code field for auth response.
     */
    private static final String RESPONSE_FIELD_EVENTCODE = "eventCode";

    /**
     * response field for auth response.
     */
    private static final String RESPONSE_FIELD_RESPONSE = "response";

    /**
     * token field for auth request.
     */
    private static final String REQUEST_FIELD_TOKEN = "token";

    /**
     * hash field for auth request.
     */
    private static final String REQUEST_FIELD_HASH = "hash";

    /**
     * Build authentication request.
     *
     * @param jwtToken google JWT token
     * @return json object request body
     */
    public static JSONObject buildAuthRequest(final String jwtToken) {

        final JSONObject request = new JSONObject();
        try {
            request.put(REQUEST_FIELD_TOKEN, jwtToken);
            request.put(REQUEST_FIELD_HASH, Build.SERIAL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return request;
    }

    /**
     * parse authentication response.
     *
     * @param response authentication response json object
     * @return response object
     */
    public static ResponseFrame parseResponse(final JSONObject response) {

        try {
            if (response.has(RESPONSE_FIELD_RESPONSE)) {

                final JSONObject responseObj = response.getJSONObject(RESPONSE_FIELD_RESPONSE);

                if (responseObj.has(RESPONSE_FIELD_STATUS) && responseObj.has(RESPONSE_FIELD_MESSAGE) && responseObj.has(RESPONSE_FIELD_EVENTCODE)) {

                    final FrameStatus status = FrameStatus.get(responseObj.getInt(RESPONSE_FIELD_STATUS));
                    final EventCode eventCode = EventCode.getStatusCode(responseObj.getInt(RESPONSE_FIELD_EVENTCODE));

                    String deviceId = "";

                    if (responseObj.has(RESPONSE_FIELD_DEVICEID)) {
                        deviceId = responseObj.getString(RESPONSE_FIELD_DEVICEID);
                    }
                    return new ResponseFrame(status, eventCode, responseObj.getString(RESPONSE_FIELD_MESSAGE), deviceId);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
