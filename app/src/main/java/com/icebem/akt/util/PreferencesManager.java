package com.icebem.akt.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;

public class PreferencesManager {
    private static final String PREFERENCES_NAME = "data";
    private static final String KEY_A = "point_a";
    private static final String KEY_B = "point_b";
    private static final String KEY_X = "point_x";
    private static final String KEY_Y = "point_y";
    private static final String KEY_W = "point_w";
    private static final String KEY_H = "point_h";
    private static final String KEY_TIMER_TIME = "timer_time";
    private static final String KEY_VERSION = "version";
    private static final int[] TIMER_CONFIG = {10, 15, 30, 45, 60, 90, 120};
    private static final int TIMER_POSITIVE = 0;
    private static final int UPDATE_TIME = 3500;
    private SharedPreferences preferences;

    public PreferencesManager(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        if (context instanceof Activity && !pointsAdapted()) {
            int[] res = ResolutionUtil.getResolution((Activity) context);
            for (int[] cfg : ResolutionUtil.RESOLUTION_CONFIG) {
                if (res[0] == cfg[0] && res[1] == cfg[1]) {
                    preferences.edit().putInt(KEY_A, cfg[2]).apply();
                    preferences.edit().putInt(KEY_B, cfg[3]).apply();
                    preferences.edit().putInt(KEY_X, cfg[4]).apply();
                    preferences.edit().putInt(KEY_Y, cfg[5]).apply();
                    preferences.edit().putInt(KEY_W, res[0] - RandomUtil.RANDOM_P).apply();
                    preferences.edit().putInt(KEY_H, res[1] / 4 - RandomUtil.RANDOM_P).apply();
                    break;
                }
            }
            setVersionCode();
        }
    }

    public int getA() {
        return preferences.getInt(KEY_A, 0);
    }

    public int getB() {
        return preferences.getInt(KEY_B, 0);
    }

    public int getX() {
        return preferences.getInt(KEY_X, 0);
    }

    public int getY() {
        return preferences.getInt(KEY_Y, 0);
    }

    public int getW() {
        return preferences.getInt(KEY_W, 0);
    }

    public int getH() {
        return preferences.getInt(KEY_H, 0);
    }

    public int getUpdateTime() {
        return UPDATE_TIME;
    }

    public void setTimerTime(int positive) {
        preferences.edit().putInt(KEY_TIMER_TIME, TIMER_CONFIG[positive]).apply();
    }

    public int getTimerTime() {
        return preferences.getInt(KEY_TIMER_TIME, TIMER_CONFIG[TIMER_POSITIVE]);
    }

    private void setVersionCode() {
        preferences.edit().putInt(KEY_VERSION, BuildConfig.VERSION_CODE).apply();
    }

    private int getVersionCode() {
        return preferences.getInt(KEY_VERSION, 0);
    }

    public boolean pointsAdapted() {
        return getVersionCode() == BuildConfig.VERSION_CODE && getA() > 0 && getB() > 0 && getX() > 0 && getY() > 0 && getW() > 0 && getH() > 0;
    }

    public String[] getTimerStrings(Context context) {
        String[] strings = new String[TIMER_CONFIG.length];
        for (int i = 0; i < TIMER_CONFIG.length; i++)
            strings[i] = String.format(context.getString(R.string.info_timer_min), TIMER_CONFIG[i]);
        return strings;
    }

    public int getTimerPositive() {
        int positive = TIMER_POSITIVE;
        for (int i = 0; i < TIMER_CONFIG.length; i++) {
            if (getTimerTime() == TIMER_CONFIG[i]) {
                positive = i;
                break;
            }
        }
        return positive;
    }
}