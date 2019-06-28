package su.css3.klaymod.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import su.css3.klaymod.system.WebService;

public class AppUtils {

    public static List<CharSequence[]> getKodiPackages(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        List<CharSequence[]> result = new ArrayList<>();

        for (ApplicationInfo packageInfo : packages) {
            String packageName = packageInfo.packageName;
            if (packageInfo.enabled && (
                    packageName.startsWith("org.xbmc.kodi")
                            || packageName.startsWith("com.semperpax.spmc")
                            || packageName.startsWith("com.zidoo.zdmc")
                            || packageName.startsWith("org.xbmc.ftmc")
            )) {
                CharSequence[] item = {packageName, packageInfo.loadLabel(packageManager)};
                result.add(item);
            }
        }

        return result;
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            if (ai != null && ai.enabled) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return false;
    }

    public static void activateApp(Context context, String packageName) {
        context.startActivity(context.getPackageManager().getLaunchIntentForPackage(packageName));
    }

    public static void showMessage(Context context, int stringId) {
        Toast.makeText(context, context.getString(stringId), Toast.LENGTH_LONG).show();
    }

    public static void showMessage(Context context, int stringId, Object... formatArgs) {
        Toast.makeText(context, context.getString(stringId, formatArgs), Toast.LENGTH_LONG).show();
    }

    public static void playMagnet(Context context, String magnetUrl, WebService.ResponseListener listener) {
        final String service = PreferencesUtils.getService(context);
        final String host = PreferencesUtils.getHost(context);
        final int maxAttempts = PreferencesUtils.getTimeout(context);

        if (PreferencesUtils.ELEMENTUM.equals(service)) {
            playElementum(host, maxAttempts, magnetUrl, listener);
        } else {
            playTorrServe(service, host, maxAttempts, magnetUrl, listener);
        }
    }

    private static void postRequest(String itemFile, String host, int maxAttempts, WebService.ResponseListener listener) {
        try {
            JSONObject item = new JSONObject();
            item.put("file", itemFile);

            JSONObject params = new JSONObject();
            params.put("item", item);

            JSONObject data = new JSONObject();
            data.put("id", 1);
            data.put("jsonrpc", "2.0");
            data.put("method", "Player.Open");
            data.put("params", params);

            String link = "http://" + host + ":8080/jsonrpc";
            new WebService(link, listener, maxAttempts, data.toString()).execute();
        } catch (Exception e) {
            listener.onReady(false);
        }
    }

    private static void playTorrServe(String service, String host, int maxAttempts, String magnetUrl, WebService.ResponseListener listener) {
        try{
            String itemFile = "";
            if (PreferencesUtils.TORRSERVE.equals(service)) {
                itemFile = "plugin://plugin.video.torrserve/?action=play_now&selFile=0&magnet=" + URLEncoder.encode(magnetUrl, "UTF-8");
            } else if (PreferencesUtils.TORRSERVER_AE.equals(service)) {
                itemFile = "plugin://plugin.video.torrserver/?action=play_now&selFile=0&magnet=" + URLEncoder.encode(magnetUrl, "UTF-8");
            }
            postRequest(itemFile, host, maxAttempts, listener);
        } catch (Exception e) {
            listener.onReady(false);
        }
    }

    private static void playElementum(String host, int maxAttempts, String magnetUrl, WebService.ResponseListener listener) {
        try {
            String link = "http://" + host + ":65220/playuri?uri=" + URLEncoder.encode(magnetUrl, "UTF-8");
            new WebService(link, listener, maxAttempts).execute();
        } catch (UnsupportedEncodingException e) {
            listener.onReady(false);
        }
    }

    public static void playLinkAceStream(Context context, String magnetUrl, WebService.ResponseListener listener) {
        final String host = PreferencesUtils.getHost(context);
        final int maxAttempts = PreferencesUtils.getTimeout(context);
        final String methodAce = PreferencesUtils.getMethodAceStream(context);
        playAceStream(methodAce, host, maxAttempts, magnetUrl, listener);
    }

    public static void playLinkSopCast(Context context, String magnetUrl, WebService.ResponseListener listener) {
        final String host = PreferencesUtils.getHost(context);
        final int maxAttempts = PreferencesUtils.getTimeout(context);
        final String methodSopcast = PreferencesUtils.getMethodSopcast(context);
        playSopcast(methodSopcast, host, maxAttempts, magnetUrl, listener);
    }


    private static void playAceStream(String methodAce, String host, int maxAttempts, String aceUrl, WebService.ResponseListener listener) {
        try {

            String itemFile = "";
            if (methodAce.equals("AceStream")) {
                itemFile = "http://" + host + ":6878/ace/getstream?id=" + aceUrl.substring(12) + "&.mp4";
            } else if (methodAce.equals("HTTPAceProxy")) {
                itemFile = "http://" + host + ":8000/pid/" + aceUrl.substring(12) + "/stream.mp4";
            } else if (methodAce.equals("Plexus")) {
                itemFile = "plugin://program.plexus/?mode=1&url=" + aceUrl.substring(12) + "&name=" + aceUrl.substring(12);
            } else if (methodAce.equals("TAM")) {
                itemFile = "plugin://plugin.video.tam/?mode=play&url=" + aceUrl + "&engine=ace_proxy";
            }

            postRequest(itemFile, host, maxAttempts, listener);
        } catch (Exception e) {
            listener.onReady(false);
        }
    }

    private static void playSopcast(String methodSopcast, String host, int maxAttempts, String aceUrl, WebService.ResponseListener listener) {
        try {


            String itemFile = "";
            if (methodSopcast.equals("Plexus")) {
                itemFile = "plugin://program.plexus/?mode=2&url=" + URLEncoder.encode(aceUrl, "UTF-8") + "&name=Sopcast";
            } else {
                return;
            }
            postRequest(itemFile, host, maxAttempts, listener);
        } catch (Exception e) {
            listener.onReady(false);
        }
    }


}
