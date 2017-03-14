package ch.burci.docslock.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {
    private static final String PREF_KIOSK_MODE = "pref_is_locked";


    public static boolean isLocked(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_KIOSK_MODE, false);
    }

    public static void setLock(final boolean locked, final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_KIOSK_MODE, locked).commit();
    }
}