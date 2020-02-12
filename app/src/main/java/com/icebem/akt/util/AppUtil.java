package com.icebem.akt.util;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.app.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class AppUtil {
    private static final String TYPE_DATA = "data";
    private static final String DATA_RECRUIT = "recruit.json";
    private static final String DATA_RESOLUTION = "resolution.json";
    public static final String THREAD_UPDATE = "update";
    public static final String MARKET_COOLAPK = "com.coolapk.market";
    //    public static final String MARKET_PLAY = "com.android.vending";
    public static final String URL_SETTINGS = "package:com.icebem.akt";
    public static final String URL_ALIPAY_API = "intent://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/tsx02922ajwj6xekqyd1rbf#Intent;scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
    public static final String URL_PAYPAL = "https://www.paypal.me/icebem";
    public static final String URL_QQ_API = "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3DN_OjFuCOkERq58jO2KoJEDD2a48vzB53";
    public static final String URL_MARKET = "market://details?id=com.icebem.akt";
    public static final String URL_PROJECT = "https://github.com/IcebemAst/ArknightsTap";
    public static final String URL_COOLAPK = "https://www.coolapk.com/apk/com.icebem.akt";
    public static final String URL_RELEASE_LATEST = "https://github.com/IcebemAst/ArknightsTap/releases/latest";
    public static final String URL_RELEASE_LATEST_API = "https://api.github.com/repos/IcebemAst/ArknightsTap/releases/latest";
    private static final String URL_RELEASE_DATA = "https://raw.githubusercontent.com/IcebemAst/ArknightsTap/master/app/release/output.json";
    private static final String URL_WEB_DATA = "https://raw.githubusercontent.com/IcebemAst/ArknightsTap/master/app/src/main/assets/data/";

    public static boolean isLatestVersion(PreferenceManager manager) throws IOException, JSONException {
        updateData(manager, true);
        int version = new JSONArray(IOUtil.stream2String(IOUtil.fromWeb(URL_RELEASE_DATA))).getJSONObject(0).getJSONObject("apkData").getInt("versionCode");
        return BuildConfig.VERSION_CODE >= version;
    }

    public static String getChangelog(JSONObject json) throws JSONException {
        return json.getString("name") + System.lineSeparator() + json.getString("body");
    }

    public static String getDownloadUrl(JSONObject json) throws JSONException {
        return json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
    }

    public static void updateData(PreferenceManager manager, boolean fromWeb) throws IOException, JSONException {
        IOUtil.stream2File(fromWeb ? IOUtil.fromWeb(AppUtil.URL_WEB_DATA + DATA_RECRUIT) : IOUtil.fromAssets(manager.getContext(), TYPE_DATA + File.separatorChar + DATA_RECRUIT), manager.getContext().getExternalFilesDir(TYPE_DATA) + File.separator + DATA_RECRUIT);
        manager.setResolutionConfig(fromWeb);
    }

    public static JSONArray getRecruitArray(Context context) throws IOException, JSONException {
        File file = new File(context.getExternalFilesDir(TYPE_DATA) + File.separator + DATA_RECRUIT);
        return new JSONArray(IOUtil.stream2String(file.exists() ? IOUtil.fromFile(file) : IOUtil.fromAssets(context, TYPE_DATA + File.separatorChar + DATA_RECRUIT)));
    }

    public static JSONArray getResolutionArray(Context context, boolean fromWeb) throws IOException, JSONException {
        return new JSONArray(IOUtil.stream2String(fromWeb ? IOUtil.fromWeb(AppUtil.URL_WEB_DATA + DATA_RESOLUTION) : IOUtil.fromAssets(context, TYPE_DATA + File.separatorChar + DATA_RESOLUTION)));
    }

    public static void showLogDialog(Context context, Throwable t) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.error_occurred);
        builder.setMessage(t.toString());
        builder.setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }
}