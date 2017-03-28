package ch.burci.docslock.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PrefUtils {
    private static final String PREF_KIOSK_MODE = "pref_is_locked";
    private static final String PREF_PASSWORD = "pref_password";

    public static void resetSettings(final Context context) {
        setLock(false, context);
        setPassword("", context);
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
}