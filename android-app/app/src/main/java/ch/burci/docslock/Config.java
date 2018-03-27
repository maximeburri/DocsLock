package ch.burci.docslock;

/**
 * Created by maxime on 14/09/17.
 */

public class Config {
    public final static String SERVER_PROTCOL = "http";
    public final static String SERVER_IP_PORT = "192.168.1.118:1337"; // Wifi Assistants
    //public final static String SERVER_IP_PORT = "192.168.1.2:1337"; // Wifi Goreptar
    public final static String SERVER_URL = SERVER_PROTCOL + "://" + SERVER_IP_PORT;

    public final static String APK_SERVER_URL = SERVER_URL + "/docslock-latest.apk";

    /* Websocket configuration (in ms) */
    public static final long WEB_SOCKET_RECONNECTION_DELAY = 1000;
    public static final long WEB_SOCKET_RECONNECTION_DELAY_MAX = 0 /*Infinity*/;
}
