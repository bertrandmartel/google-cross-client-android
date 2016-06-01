package com.github.akinaru.gcrossclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.SSLCertificateSocketFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.akinaru.gcrossclient.config.PropertyReader;
import com.github.akinaru.gcrossclient.model.ComModel;
import com.github.akinaru.gcrossclient.model.EventCode;
import com.github.akinaru.gcrossclient.model.ResponseFrame;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

/**
 * Main activity used to sign in / sign out.
 *
 * @author Bertrand Martel
 */
public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    /**
     * log tag.
     */
    private final static String TAG = LoginActivity.class.getSimpleName();

    /**
     * google api client object.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * int callback for google sign in.
     */
    private final static int RC_SIGN_IN = 123;

    /**
     * volley http request queue.
     */
    private RequestQueue queue;

    /**
     * textview for output local authentication.
     */
    private TextView mLocalAuthTv;

    /**
     * textview for output remote authentication.
     */
    private TextView mRemoteAuthTv;

    /**
     * define if signout remote request should be sent once we receive successfull signin from user/app.
     */
    private boolean mSignout = false;

    /**
     * shared preference object.
     */
    private SharedPreferences sharedPref;

    /**
     * authentication server default hostname.
     */
    private static final String SERVER_DEFAULT_HOSTNAME = "192.168.1.1";

    /**
     * authentication server default port.
     */
    private static final String SERVER_DEFAULT_PORT = "4747";

    /**
     * authentication server default protocol.
     */
    private static final String SERVER_DEFAULT_PROTOCOL = "https";

    /**
     * server hostname.
     */
    private String serverHostName = SERVER_DEFAULT_HOSTNAME;

    /**
     * server port.
     */
    private String serverPort = SERVER_DEFAULT_PORT;

    /**
     * server protocol.
     */
    private String serverProtocol = SERVER_DEFAULT_PROTOCOL;

    /**
     * signout api.
     */
    private static final String SERVER_SIGNOUT_API = "/api/signout";

    /**
     * signin api.
     */
    private static final String SERVER_SIGNIN_API = "/api/signin";

    /**
     * server clientId value.
     */
    private String serverClientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        PropertyReader propertyReader = new PropertyReader(this);
        Properties properties = propertyReader.getMyProperties("config.properties");

        if (properties.containsKey(PropertyReader.PROPERTY_SERVER_HOSTNAME) &&
                properties.containsKey(PropertyReader.PROPERTY_SERVER_PORT) &&
                properties.containsKey(PropertyReader.PROPERTY_SERVER_PROTOCOL) &&
                properties.containsKey(PropertyReader.PROPERTY_CLIENT_ID)) {
            Log.v(TAG, "setting custom values for authentication server config");
            serverHostName = properties.getProperty(PropertyReader.PROPERTY_SERVER_HOSTNAME);
            serverPort = properties.getProperty(PropertyReader.PROPERTY_SERVER_PORT);
            serverProtocol = properties.getProperty(PropertyReader.PROPERTY_SERVER_PROTOCOL);
            serverClientId = properties.getProperty(PropertyReader.PROPERTY_CLIENT_ID);
        } else {
            Log.v(TAG, "setting default values for authentication server config");
        }

        //shared preference
        sharedPref = getPreferences(Context.MODE_PRIVATE);

        //textview configsss
        mLocalAuthTv = (TextView) findViewById(R.id.local_status_text);
        mRemoteAuthTv = (TextView) findViewById(R.id.remote_status_text);

        if (serverProtocol.equals("https")) {

            //trust self signed certificate : THIS SHOULD BE REMOVED IN PRODUCTION
            HttpStack hurlStack = new HurlStack() {
                @Override
                protected HttpURLConnection createConnection(URL url) throws IOException {
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                    try {
                        httpsURLConnection.setSSLSocketFactory(SSLCertificateSocketFactory.getInsecure(0, null));
                        httpsURLConnection.setHostnameVerifier(new AllowAllHostnameVerifier());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return httpsURLConnection;
                }
            };

            queue = Volley.newRequestQueue(this, hurlStack);

        } else {

            queue = Volley.newRequestQueue(this, null);
            
        }

        //google signin configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId)
                .requestScopes(new Scope(Scopes.EMAIL))
                .requestServerAuthCode(serverClientId, false)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //set button listener
        findViewById(R.id.disconnect_btn).setOnClickListener(this);
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        //signin if user has already signin before
        boolean authenticated = sharedPref.getBoolean("authenticated", false);
        if (authenticated) {
            signIn();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //catch sign in response
        if (requestCode == RC_SIGN_IN) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            //user has been authenticated locally

            if (mSignout) {

                //signout request should be sent
                mSignout = false;

                if (result.isSuccess()) {

                    GoogleSignInAccount acct = result.getSignInAccount();
                    String idToken = acct.getIdToken();
                    remoteSignout(idToken);

                } else {
                    Toast.makeText(LoginActivity.this, "you are not signed in", Toast.LENGTH_SHORT).show();
                }
            } else {

                //signin request should be sent
                mLocalAuthTv.setVisibility(View.VISIBLE);

                if (result.isSuccess()) {
                    mLocalAuthTv.setTextColor(Color.parseColor("#039967"));
                    mLocalAuthTv.setText("Local Authentication successfull");
                    findViewById(R.id.img_local_status_img).setVisibility(View.VISIBLE);
                    findViewById(R.id.img_local_status_img).setBackgroundResource(R.drawable.ok);

                    GoogleSignInAccount acct = result.getSignInAccount();
                    String idToken = acct.getIdToken();
                    remoteSignin(idToken);

                    findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                    findViewById(R.id.disconnect_btn).setVisibility(View.VISIBLE);
                } else {
                    Log.v(TAG, "authentication failure");
                    mLocalAuthTv.setTextColor(Color.parseColor("#FF0000"));
                    mLocalAuthTv.setText("Local Authentication failure");
                    findViewById(R.id.img_local_status_img).setVisibility(View.VISIBLE);
                    findViewById(R.id.img_local_status_img).setBackgroundResource(R.drawable.notok);
                }

                findViewById(R.id.separator_auth).setVisibility(View.VISIBLE);
            }
        } else {

            //permission denied for scope email
            findViewById(R.id.local_status_text).setVisibility(View.VISIBLE);
            findViewById(R.id.local_status_text).setBackgroundResource(R.drawable.notok);
            Toast.makeText(LoginActivity.this, "permission denied for scope email", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Send signout requeset to server.
     *
     * @param idToken google JWT cross client token
     */
    private void remoteSignout(String idToken) {

        JSONObject request = ComModel.buildAuthRequest(idToken);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (serverProtocol + "://" + serverHostName + ":" + serverPort + SERVER_SIGNOUT_API, request, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.v(TAG, "response : " + response.toString());

                        ResponseFrame responseFrame = ComModel.parseResponse(response);

                        if (responseFrame != null) {

                            Log.v(TAG, "response.getStatus()  : " + responseFrame.getStatus());
                            Log.v(TAG, "response.getMessage() : " + responseFrame.getMessage());

                            if ((responseFrame.getEventCode() == EventCode.SIGNOUT_SUCCESS)) {

                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("authenticated", false);
                                editor.commit();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this, "successfully remotely signout", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                                        new ResultCallback<Status>() {
                                            @Override
                                            public void onResult(Status status) {
                                                if (status.isSuccess()) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(LoginActivity.this, "You have been signout locally", Toast.LENGTH_SHORT).show();
                                                            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                                                            findViewById(R.id.disconnect_btn).setVisibility(View.GONE);
                                                        }
                                                    });
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "Error occured while signing out locally", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LoginActivity.this, "error occured during remote signout", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Log.e(TAG, "response error");
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        jsObjRequest.setShouldCache(false);

        queue.add(jsObjRequest);
    }

    /**
     * Send signin request to server.
     *
     * @param idToken google JWT cross client token
     */
    private void remoteSignin(String idToken) {

        JSONObject request = ComModel.buildAuthRequest(idToken);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (serverProtocol + "://" + serverHostName + ":" + serverPort + SERVER_SIGNIN_API, request, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.v(TAG, "response : " + response.toString());

                        ResponseFrame responseFrame = ComModel.parseResponse(response);
                        if (responseFrame != null) {
                            Log.v(TAG, "response.getStatus()  : " + responseFrame.getStatus());
                            Log.v(TAG, "response.getMessage() : " + responseFrame.getMessage());

                            if ((responseFrame.getEventCode() == EventCode.REGISTRATION_SUCCESS) || (responseFrame.getEventCode() == EventCode.ALREADY_REGISTERED)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRemoteAuthTv.setVisibility(View.VISIBLE);
                                        mRemoteAuthTv.setTextColor(Color.parseColor("#039967"));
                                        mRemoteAuthTv.setText("Remote Authentication successfull");
                                        findViewById(R.id.img_remote_status_img).setVisibility(View.VISIBLE);
                                        findViewById(R.id.img_remote_status_img).setBackgroundResource(R.drawable.ok);

                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putBoolean("authenticated", true);
                                        editor.commit();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRemoteAuthTv.setVisibility(View.VISIBLE);
                                        mRemoteAuthTv.setTextColor(Color.parseColor("#FF0000"));
                                        mRemoteAuthTv.setText("Remote Authentication failure");
                                        findViewById(R.id.img_remote_status_img).setVisibility(View.VISIBLE);
                                        findViewById(R.id.img_remote_status_img).setBackgroundResource(R.drawable.notok);
                                        signOut();

                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putBoolean("authenticated", false);
                                        editor.commit();
                                    }
                                });
                            }
                        } else {
                            Log.e(TAG, "response error");
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        error.printStackTrace();
                    }
                });
        jsObjRequest.setShouldCache(false);

        queue.add(jsObjRequest);
    }

    /**
     * send signin request once user is signin locally.
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    /**
     * hide all views.
     */
    private void clear() {
        findViewById(R.id.local_status_text).setVisibility(View.GONE);
        findViewById(R.id.remote_status_text).setVisibility(View.GONE);
        findViewById(R.id.img_local_status_img).setVisibility(View.GONE);
        findViewById(R.id.img_remote_status_img).setVisibility(View.GONE);
        findViewById(R.id.separator_auth).setVisibility(View.GONE);
    }

    /**
     * send signout request once user is authenticated.
     */
    private void signOut() {
        mSignout = true;
        signIn();
    }

    /**
     * manage button click listener.
     *
     * @param v button view
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.disconnect_btn:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clear();
                    }
                });
                signOut();
                break;
        }
    }
}
