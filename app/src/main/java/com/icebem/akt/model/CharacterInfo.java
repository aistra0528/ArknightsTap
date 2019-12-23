package com.icebem.akt.model;

import android.content.Context;

import com.icebem.akt.util.IOUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class CharacterInfo {
    private static final String ASSETS_PATH = "data/hr.json";
    private static final String KEY_STAR = "star";
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";
    private static final String KEY_TAGS = "tags";
    private int star;
    private String name, type;
    private String[] tags;

    private CharacterInfo(JSONObject obj) throws JSONException {
        star = obj.getInt(KEY_STAR);
        name = obj.getString(KEY_NAME);
        type = obj.getString(KEY_TYPE);
        tags = new String[obj.getJSONArray(KEY_TAGS).length()];
        for (int i = 0; i < tags.length; i++)
            tags[i] = obj.getJSONArray(KEY_TAGS).getString(i);
    }

    public static CharacterInfo[] fromAssets(Context context) throws IOException, JSONException {
        JSONArray array = new JSONArray(IOUtil.stream2String(IOUtil.fromAssets(context, ASSETS_PATH)));
        CharacterInfo[] infoList = new CharacterInfo[array.length()];
        for (int i = 0; i < infoList.length; i++)
            infoList[i] = new CharacterInfo(array.getJSONObject(i));
        return infoList;
    }

    public boolean containsTag(String tag) {
        for (String t : tags)
            if (t.equals(tag)) return true;
        return false;
    }

    public int getStar() {
        return star;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String[] getTags() {
        return tags;
    }
}