package su.css3.klaymod.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.List;

public class PreferencesUtils {
    public static final String KEY_PREF_KODI_SERVICE = "service";
    public static final String KEY_PREF_KODI_TIMEOUT = "timeout";
    public static final String KEY_PREF_KODI_HOST = "host";
    public static final String KEY_PREF_KODI_APP = "application";
    public static final String KEY_PREF_UPDATE = "update";
    public static final String KEY_PREF_LAST_UPDATE = "lastupdate";

    public static final String ELEMENTUM = "Elementum";
    public static final String TORRSERVE = "TorrServe";
    public static final String TORRSERVER_AE = "TorrServer (AE)";

    private static final String LOCALHOST = "127.0.0.1";
    private static final String DEFAULT_TIMEOUT = "10";
    private static final long UPDATE_INTERVAL = 24 * 60 * 60 * 1000;

    public static String getService(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(KEY_PREF_KODI_SERVICE, ELEMENTUM);
    }

    public static int getTimeout(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            return Integer.parseInt(sharedPreferences.getString(KEY_PREF_KODI_TIMEOUT, DEFAULT_TIMEOUT));
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    public static String getHost(Context context) {
        String kodiPackageName = getKodiPackageName(context);
        if (kodiPackageName != null) {
            return LOCALHOST;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(KEY_PREF_KODI_HOST, LOCALHOST);
    }

    public static String getKodiPackageName(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String packageName = sharedPreferences.getString(KEY_PREF_KODI_APP, null);
        List<CharSequence[]> kodiPackages = AppUtils.getKodiPackages(context);

        if (packageName == null) {
            return kodiPackages.isEmpty() ? null : kodiPackages.get(0)[0].toString();
        }

        return !packageName.isEmpty() && AppUtils.isAppInstalled(context, packageName) ? packageName : null;
    }

    public static boolean isNeedCheckUpdate(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return System.currentTimeMillis() - sharedPreferences.getLong(KEY_PREF_LAST_UPDATE, 0) > UPDATE_INTERVAL;
    }

    public static void setLastUpdateTime(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putLong(KEY_PREF_LAST_UPDATE, System.currentTimeMillis()).apply();
    }

    public static String getMethodAceStream(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString("methodace", "AceStream");
    }

    public static String getMethodSopcast(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString("methodsopcast", "Plexus");
    }
}
