package com.icebem.akt.model;

import android.content.Context;

import com.icebem.akt.util.DataUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MaterialInfo {
    private static final String KEY_ID = "id";
    private static final String KEY_STAR = "star";
    private static final String KEY_NAME = "name";
    private static final String KEY_NAME_CN = "nameCN";
    private static final String KEY_NAME_JP = "nameJP";
    private static final String KEY_FROM = "from";
    private int id, star;
    private String name, nameCN, nameJP;
    private Mission[] missions;

    private MaterialInfo(JSONObject obj) throws JSONException {
        id = obj.getInt(KEY_ID);
        star = obj.getInt(KEY_STAR);
        name = obj.getString(KEY_NAME);
        nameCN = obj.getString(KEY_NAME_CN);
        nameJP = obj.getString(KEY_NAME_JP);
        JSONArray from = obj.getJSONArray(KEY_FROM);
        if (from.length() > 0) {
            missions = new Mission[from.length()];
            for (int i = 0; i < from.length(); i++)
                missions[i] = new Mission(from.getJSONObject(i));
        }
    }

    public static MaterialInfo[] load(Context context) throws IOException, JSONException {
        JSONArray array = DataUtil.getMaterialData(context);
        MaterialInfo[] infoList = new MaterialInfo[array.length()];
        for (int i = 0; i < infoList.length; i++)
            infoList[i] = new MaterialInfo(array.getJSONObject(i));
        return infoList;
    }

    public int getId() {
        return id;
    }

    public int getStar() {
        return star;
    }

    public String getName(int index) {
        switch (index) {
            case DataUtil.INDEX_EN:
                return name;
            case DataUtil.INDEX_JP:
                return nameJP;
            default:
                return nameCN;
        }
    }

    public Mission[] getMissions() {
        return missions;
    }

    public static class Mission {
        private static final String KEY_MISSION = "mission";
        private static final String KEY_SANITY = "sanity";
        private static final String KEY_FREQUENCY = "frequency";
        private String name;
        private int sanity;
        private float frequency;

        Mission(JSONObject obj) throws JSONException {
            name = obj.getString(KEY_MISSION);
            sanity = obj.getInt(KEY_SANITY);
            frequency = (float) obj.getDouble(KEY_FREQUENCY);
        }

        public String getName() {
            return name;
        }

        public int getSanity() {
            return sanity;
        }

        public float getFrequency() {
            return frequency;
        }
    }
}