package ch.burci.docslock.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ch.burci.docslock.Config;
import ch.burci.docslock.DeviceWithGroup;

public class PrefUtils {
    private static final String PREF_KIOSK_MODE = "pref_is_locked";
    private static final String PREF_PASSWORD = "pref_password";
    private static final String PREF_DEVICE_ID = "device_id";
    private static final String PREF_LAST_DEVICE = "last_device";
    private static final String PREF_SERVER_URL = "server_ip_port";

    public static void resetSettings(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().clear().apply();
    }

    public static boolean isLocked(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_KIOSK_MODE, false);
    }

    public static void setLock(final boolean locked, final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_KIOSK_MODE, locked).commit();
    }

    public static void setPassword(final String password, final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_PASSWORD, password).commit();
    }

    public static boolean checkPassword(final String password, final Context context) {
        return password.equals(getPassword(context));
    }

    protected static String getPassword(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_PASSWORD, null);
    }

    public static String getFilesFolderName() {
        return "DocsLock";
    }

    public static boolean isDeviceIdExists(final Context context) {
        return getDeviceId(context) == null;
    }

    public static String getDeviceId(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_DEVICE_ID, null);
    }

    public static void setDeviceId(String id, final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_DEVICE_ID, id).commit();
    }

    public static DeviceWithGroup getLastDevice(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String jsonDevice = sp.getString(PREF_LAST_DEVICE, null);
        return DeviceWithGroup.fromJSON(jsonDevice);
    }

    public static void setLastDevice(DeviceWithGroup device, final Context context){
        String deviceJson = "";
        if(device != null)
            deviceJson = device.toJSON();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_LAST_DEVICE, deviceJson).commit();
    }

    public static void setServerURL(String ipPort, final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_SERVER_URL, ipPort).commit();
    }

    public static String getServerURL(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String serverIpPort = sp.getString(PREF_SERVER_URL, null);
        if(serverIpPort == null)
            return Config.DEFAULT_SERVER_IP_PORT;
        return serverIpPort;
    }
}