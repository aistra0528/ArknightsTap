package com.icebem.akt.object;

import android.content.Context;

import com.icebem.akt.util.IOUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class CharacterInfo implements Comparable<CharacterInfo> {
    private static final String ASSETS_PATH = "data/hr.json";
    private static final String KEY_STAR = "star";
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";
    private static final String KEY_SEX = "sex";
    private static final String KEY_LIMITED = "limited";
    private static final String KEY_TAGS = "tags";
    private int star;
    private boolean limited;
    private String name, type, sex;
    private String[] tags;

    private CharacterInfo(JSONObject obj) throws JSONException {
        star = obj.getInt(KEY_STAR);
        limited = obj.getBoolean(KEY_LIMITED);
        name = obj.getString(KEY_NAME);
        type = obj.getString(KEY_TYPE);
        sex = obj.getString(KEY_SEX);
        tags = new String[obj.getJSONArray(KEY_TAGS).length()];
        for (int i = 0; i < tags.length; i++) {
            tags[i] = obj.getJSONArray(KEY_TAGS).getString(i);
        }
    }

    public static CharacterInfo[] fromAssets(Context context) throws IOException, JSONException {
        JSONArray array = new JSONArray(IOUtil.stream2String(IOUtil.fromAssets(context, ASSETS_PATH)));
        CharacterInfo[] infos = new CharacterInfo[array.length()];
        for (int i = 0; i < infos.length; i++) {
            infos[i] = new CharacterInfo(array.getJSONObject(i));
        }
        return infos;
    }

    @Override
    public int compareTo(CharacterInfo info) {
        return info.star - star;
    }

    public int getStar() {
        return star;
    }

    public boolean isLimited() {
        return limited;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getSex() {
        return sex;
    }

    public String[] getTags() {
        return tags;
    }
}