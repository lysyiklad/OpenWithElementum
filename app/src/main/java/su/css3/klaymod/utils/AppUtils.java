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

        if (PreferencesUtils.TORRSERVE.equals(service)) {
            playTorrServe(host, maxAttempts, magnetUrl, listener);
        } else {
            playElementum(host, maxAttempts, magnetUrl, listener);
        }
    }

    private static void playTorrServe(String host, int maxAttempts, String magnetUrl, WebService.ResponseListener listener) {
        try {
            JSONObject item = new JSONObject();
            item.put("file", "plugin://plugin.video.torrserve/?action=play_now&selFile=0&magnet=" + URLEncoder.encode(magnetUrl, "UTF-8"));


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

            JSONObject item = new JSONObject();
            if (methodAce.equals("AceStream")) {
                // URLEncoder.encode(aceUrl, "UTF-8")
                // item.put("file", URLEncoder.encode("http://" + host + ":6878/ace/getstream?id=" + aceUrl.substring(12), "UTF-8"));
                // item.put("file", "http://192.168.10.152:6878/ace/getstream?infohash=cdee5cf35013448586286837ca5a310d0a4bb6e6");
                item.put("file", "http://" + host + ":6878/ace/getstream?id=" + aceUrl.substring(12) + "&.mp4");
            } else if (methodAce.equals("HTTPAceProxy")) {
                // item.put("file", URLEncoder.encode("http://" + host + ":8000/pid/" + aceUrl.substring(12) + "/stream.mp4", "UTF-8"));
                item.put("file", "http://" + host + ":8000/pid/" + aceUrl.substring(12) + "/stream.mp4");
            } else if (methodAce.equals("Plexus")) {
                item.put("file", "plugin://program.plexus/?mode=1&url=" + aceUrl.substring(12) + "&name=" + aceUrl.substring(12));
            } else if (methodAce.equals("TAM")) {
                item.put("file", "plugin://plugin.video.tam/?mode=play&url=" + aceUrl + "&engine=ace_proxy");
            }


            JSONObject params = new JSONObject();
            params.put("item", item);

            JSONObject data = new JSONObject();
            data.put("id", 1);
            data.put("jsonrpc", "2.0");
            data.put("method", "Player.Open");
            data.put("params", params);

            String link = "";
            String postData = "";

            //if (methodAce.equals("AceStream") || methodAce.equals("HTTPAceProxy")) {
            //    link = "http://" + host + ":8080/jsonrpc" + "?request=" + data.toString();
            //    postData = "acestream";
            //} else {
                link = "http://" + host + ":8080/jsonrpc";
                postData = data.toString();
            //}

            WebService ws = new WebService(link, listener, maxAttempts, postData);
            ws.execute();
        } catch (Exception e) {
            listener.onReady(false);
        }
    }

    private static void playSopcast(String methodSopcast, String host, int maxAttempts, String aceUrl, WebService.ResponseListener listener) {
        try {

            JSONObject item = new JSONObject();
            if (methodSopcast.equals("Plexus")) {
                item.put("file", "plugin://program.plexus/?mode=2&url=" + URLEncoder.encode(aceUrl, "UTF-8") + "&name=Sopcast");
            } else {
                return;
            }

            JSONObject params = new JSONObject();
            params.put("item", item);

            JSONObject data = new JSONObject();
            data.put("id", 1);
            data.put("jsonrpc", "2.0");
            data.put("method", "Player.Open");
            data.put("params", params);

            String link = "";
            String postData = "";

            if (methodSopcast.equals("Plexus")) {
                link = "http://" + host + ":8080/jsonrpc";
                postData = data.toString();
            }

            new WebService(link, listener, maxAttempts, postData).execute();
        } catch (Exception e) {
            listener.onReady(false);
        }
    }


}
