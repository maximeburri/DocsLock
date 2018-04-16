package ch.burci.docslock;

import android.content.Context;

import ch.burci.docslock.models.PrefUtils;

/**
 * Created by maxime on 14/09/17.
 */

public class Config {
    public final static String DEFAULT_SERVER_IP_PORT = "http://192.168.1.4:1337";
    public final static String APK_SERVER_FILE = "docslock-latest.apk";

    public static String getApkServerUrl(Context context){
        return PrefUtils.getServerURL(context) + "/" + APK_SERVER_FILE;
    }

    /* Websocket configuration (in ms) */
    public static final long WEB_SOCKET_RECONNECTION_DELAY = 1000;
    public static final long WEB_SOCKET_RECONNECTION_DELAY_MAX = 0 /*Infinity*/;
}
