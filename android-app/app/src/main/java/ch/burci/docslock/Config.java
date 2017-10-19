package ch.burci.docslock;

/**
 * Created by maxime on 14/09/17.
 */

public class Config {
    public final static String SERVER_PROTCOL = "http";
    public final static String SERVER_IP_PORT = "192.168.1.118:1337"; // Wifi Assistants
    // public final static String SERVER_IP_PORT = "192.168.178.53:1337"; // Wifi Maison
    public final static String SERVER_URL = SERVER_PROTCOL + "://" + SERVER_IP_PORT;
}
