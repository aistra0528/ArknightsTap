package com.icebem.akt.util;

import android.content.Context;

import com.icebem.akt.BuildConfig;
import com.icebem.akt.app.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class DataUtil {
    public static final int INDEX_EN = 0;
    public static final int INDEX_CN = 1;
    public static final int INDEX_JP = 2;
    public static final String FLAG_UNRELEASED = "*";
    private static final String KEY_NAME = "name";
    private static final String KEY_COMPAT = "compat";
    private static final String KEY_VERSION = "version";
    private static final String TYPE_DATA = "data";
    private static final String DATA_ENTRY = "entry.json";
    private static final String DATA_MATERIAL = "material.json";
    private static final String DATA_RECRUIT = "recruit.json";
    private static final String DATA_RESOLUTION = "resolution.json";
    private static final String URL_WEB_DATA = "https://raw.githubusercontent.com/IcebemAst/ArknightsTap/master/app/src/main/assets/data/";

    public static boolean updateData(PreferenceManager manager, boolean fromWeb) throws IOException, JSONException {
        JSONArray targetEntry = fromStream(fromWeb ? IOUtil.fromWeb(URL_WEB_DATA + DATA_ENTRY) : IOUtil.fromAssets(manager.getApplicationContext(), TYPE_DATA + File.separatorChar + DATA_ENTRY));
        JSONArray entry = getOfflineData(manager.getApplicationContext(), DATA_ENTRY);
        boolean updated = false;
        for (int i = 0; i < targetEntry.length(); i++) {
            JSONObject data = targetEntry.getJSONObject(i);
            if (data.getString(KEY_NAME).equals(DATA_ENTRY)) {
                if (!compatible(data))
                    return updated;
                if (!fromWeb || canUpdate(data, entry))
                    updated = true;
            } else if (compatible(data) && (!fromWeb || canUpdate(data, entry))) {
                setOfflineData(manager.getApplicationContext(), data.getString(KEY_NAME), fromWeb);
                updated = true;
            }
        }
        if (updated)
            setOfflineData(manager.getApplicationContext(), DATA_ENTRY, fromWeb);
        return updated;
    }

    private static JSONArray getOfflineData(Context context, String data) throws IOException, JSONException {
        File file = new File(context.getFilesDir() + File.separator + data);
        return fromStream(file.exists() ? IOUtil.fromFile(file) : IOUtil.fromAssets(context, TYPE_DATA + File.separatorChar + data));
    }

    private static void setOfflineData(Context context, String data, boolean fromWeb) throws IOException {
        IOUtil.stream2File(fromWeb ? IOUtil.fromWeb(URL_WEB_DATA + data) : IOUtil.fromAssets(context, TYPE_DATA + File.separatorChar + data), context.getFilesDir() + File.separator + data);
    }

    public static JSONArray getMaterialData(Context context) throws IOException, JSONException {
        return getOfflineData(context, DATA_MATERIAL);
    }

    public static JSONArray getRecruitData(Context context) throws IOException, JSONException {
        return getOfflineData(context, DATA_RECRUIT);
    }

    public static JSONArray getResolutionData(Context context) throws IOException, JSONException {
        return getOfflineData(context, DATA_RESOLUTION);
    }

    private static JSONArray fromStream(InputStream in) throws IOException, JSONException {
        return new JSONArray(IOUtil.stream2String(in));
    }

    private static boolean canUpdate(JSONObject data, JSONArray entry) throws JSONException {
        for (int i = 0; i < entry.length(); i++) {
            if (entry.getJSONObject(i).getString(KEY_NAME).equals(data.getString(KEY_NAME)))
                return data.getInt(KEY_VERSION) > entry.getJSONObject(i).getInt(KEY_VERSION);
        }
        return false;
    }

    private static boolean compatible(JSONObject data) throws JSONException {
        return BuildConfig.VERSION_CODE >= data.getInt(KEY_COMPAT);
    }
}