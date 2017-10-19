package ch.burci.docslock;

/**
 * Created by maxime on 11/09/17.
 */

public class Device {
    private String id;
    private String mac;
    private String firebaseToken;

    public Device() {
    }

    public String getId() {
        return id;
    }

    public String getMAC() {
        return mac;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }
}
