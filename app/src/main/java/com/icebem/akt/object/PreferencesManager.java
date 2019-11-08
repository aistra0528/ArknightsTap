package com.icebem.akt.object;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.service.GestureService;
import com.icebem.akt.util.RandomUtil;
import com.icebem.akt.util.ResolutionConfig;

public class PreferencesManager {
    private static final String PREFERENCES_NAME = "data";
    private static final String KEY_A = "point_a";
    private static final String KEY_B = "point_b";
    private static final String KEY_X = "point_x";
    private static final String KEY_Y = "point_y";
    private static final String KEY_W = "point_w";
    private static final String KEY_H = "point_h";
    private static final String KEY_TIMER_TIME = "timer_time";
    private static final String KEY_PRO = "pro";
    private static final String KEY_VERSION = "version";
    private static final int[] TIMER_CONFIG = {0, 10, 15, 30, 45, 60, 90, 120};
    private static final int TIMER_POSITION = 1;
    private static final int UPDATE_TIME = 3500;
    private SharedPreferences preferences;

    public PreferencesManager(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        if (context instanceof Activity && !dataUpdated()) {
            if (!isPro() && getVersionCode() > 0 && getVersionCode() < BuildConfig.VERSION_CODE) {
                setPro();
                context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, GestureService.class.getName()), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                Toast.makeText(context, R.string.info_pro, Toast.LENGTH_SHORT).show();
            }
            int[] res = ResolutionConfig.getResolution((Activity) context);
            for (int[] cfg : ResolutionConfig.RESOLUTION_CONFIG) {
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

    public void setTimerTime(int position) {
        preferences.edit().putInt(KEY_TIMER_TIME, TIMER_CONFIG[position]).apply();
    }

    public int getTimerTime() {
        return preferences.getInt(KEY_TIMER_TIME, TIMER_CONFIG[TIMER_POSITION]);
    }

    public void setPro() {
        preferences.edit().putBoolean(KEY_PRO, true).apply();
    }

    public boolean isPro() {
        return preferences.getBoolean(KEY_PRO, false);
    }

    private void setVersionCode() {
        preferences.edit().putInt(KEY_VERSION, BuildConfig.VERSION_CODE).apply();
    }

    private int getVersionCode() {
        return preferences.getInt(KEY_VERSION, 0);
    }

    public boolean dataUpdated() {
        return getVersionCode() == BuildConfig.VERSION_CODE && getA() > 0 && getB() > 0 && getX() > 0 && getY() > 0 && getW() > 0 && getH() > 0;
    }

    public String[] getTimerStrings(Context context) {
        String[] strings = new String[TIMER_CONFIG.length];
        for (int i = 0; i < TIMER_CONFIG.length; i++)
            strings[i] = TIMER_CONFIG[i] == 0 ? context.getString(R.string.info_timer_none) : String.format(context.getString(R.string.info_timer_min), TIMER_CONFIG[i]);
        return strings;
    }

    public int getTimerPosition() {
        for (int i = 0; i < TIMER_CONFIG.length; i++) {
            if (getTimerTime() == TIMER_CONFIG[i])
                return i;
        }
        return TIMER_POSITION;
    }
}