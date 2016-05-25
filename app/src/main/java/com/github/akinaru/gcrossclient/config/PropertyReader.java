package com.github.akinaru.gcrossclient.config;


import android.content.Context;
import android.content.res.AssetManager;

import java.io.InputStream;
import java.util.Properties;

/**
 * Property Reader used to parse properties.
 */
public class PropertyReader {

    /**
     * android context.
     */
    private Context context;

    /**
     * properties list.
     */
    private Properties properties;

    /**
     * server hostname field.
     */
    public static final String PROPERTY_SERVER_HOSTNAME = "server_hostname";

    /**
     * server protocol field.
     */
    public static final String PROPERTY_SERVER_PROTOCOL = "server_protocol";

    /**
     * server protocol field.
     */
    public static final String PROPERTY_CLIENT_ID = "server_client_id";

    /**
     * server port field.
     */
    public static final String PROPERTY_SERVER_PORT = "server_port";

    /**
     * Build property reader.
     *
     * @param context
     */
    public PropertyReader(Context context) {
        this.context = context;
        properties = new Properties();
    }

    /**
     * Retrieve list of properties from file.
     *
     * @param file property file
     * @return list of properties
     */
    public Properties getMyProperties(String file) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(file);
            properties.load(inputStream);

        } catch (Exception e) {
            System.out.print(e.getMessage());
        }

        return properties;
    }
}