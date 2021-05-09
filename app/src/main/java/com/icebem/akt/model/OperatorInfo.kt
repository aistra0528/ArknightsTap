package com.icebem.akt.model;

import android.content.Context;

import com.icebem.akt.util.DataUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

class OperatorInfo {
    private static final String KEY_STAR = "star";
    private static final String KEY_NAME = "name";
    private static final String KEY_NAME_CN = "nameCN";
    private static final String KEY_NAME_TW = "nameTW";
    private static final String KEY_NAME_JP = "nameJP";
    private static final String KEY_NAME_KR = "nameKR";
    private static final String KEY_TYPE = "type";
    private static final String KEY_TAGS = "tags";
    private int star;
    private String name, nameCN, nameTW, nameJP, nameKR, type;
    private String[] tags;

    private OperatorInfo(JSONObject obj) throws JSONException {
        star = obj.getInt(KEY_STAR);
        name = obj.getString(KEY_NAME);
        nameCN = obj.getString(KEY_NAME_CN);
        nameTW = obj.getString(KEY_NAME_TW);
        nameJP = obj.getString(KEY_NAME_JP);
        nameKR = obj.getString(KEY_NAME_KR);
        type = obj.getString(KEY_TYPE);
        tags = new String[obj.getJSONArray(KEY_TAGS).length()];
        for (int i = 0; i < tags.length; i++)
            tags[i] = obj.getJSONArray(KEY_TAGS).getString(i);
    }

    static OperatorInfo[] load(Context context) throws IOException, JSONException {
        JSONArray array = DataUtil.getRecruitData(context);
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
            case DataUtil.INDEX_EN:
                return name;
            case DataUtil.INDEX_TW:
                return nameTW;
            case DataUtil.INDEX_JP:
                return nameJP;
            case DataUtil.INDEX_KR:
                return nameKR;
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