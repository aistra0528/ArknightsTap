package com.icebem.akt.model;

import android.content.Context;

import com.icebem.akt.util.AppUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

class OperatorInfo {
    private static final String KEY_STAR = "star";
    private static final String KEY_NAME = "name";
    private static final String KEY_NAME_CN = "nameCN";
    private static final String KEY_NAME_JP = "nameJP";
    private static final String KEY_TYPE = "type";
    private static final String KEY_TAGS = "tags";
    private int star;
    private String name, nameCN, nameJP, type;
    private String[] tags;

    private OperatorInfo(JSONObject obj) throws JSONException {
        star = obj.getInt(KEY_STAR);
        name = obj.getString(KEY_NAME);
        nameCN = obj.getString(KEY_NAME_CN);
        nameJP = obj.getString(KEY_NAME_JP);
        type = obj.getString(KEY_TYPE);
        tags = new String[obj.getJSONArray(KEY_TAGS).length()];
        for (int i = 0; i < tags.length; i++)
            tags[i] = obj.getJSONArray(KEY_TAGS).getString(i);
    }

    static OperatorInfo[] fromAssets(Context context) throws IOException, JSONException {
        JSONArray array = AppUtil.getRecruitArray(context);
        OperatorInfo[] infoList = new OperatorInfo[array.length()];
        for (int i = 0; i < infoList.length; i++)
            infoList[i] = new OperatorInfo(array.getJSONObject(i));
        return infoList;
    }

    boolean containsTag(String tag) {
        for (String t : tags)
            if (t.equals(tag)) return true;
        return false;
    }

    int getStar() {
        return star;
    }

    String getName(int index) {
        switch (index) {
            case RecruitTag.INDEX_EN:
                return name;
            case RecruitTag.INDEX_JP:
                return nameJP;
            default:
                return nameCN;
        }
    }

    String getType() {
        return type;
    }

    String[] getTags() {
        return tags;
    }
}