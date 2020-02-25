package com.icebem.akt.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.model.RecruitTag;
import com.icebem.akt.service.GestureService;
import com.icebem.akt.util.AppUtil;
import com.icebem.akt.util.RandomUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class PreferenceManager {
    private static final int PACKAGE_EN = 2;
    private static final int PACKAGE_JP = 3;
    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_BLUE_X = "blue_x";
    private static final String KEY_BLUE_Y = "blue_y";
    private static final String KEY_RED_X = "red_x";
    private static final String KEY_RED_Y = "red_y";
    private static final String KEY_GREEN_X = "green_x";
    private static final String KEY_GREEN_Y = "green_y";
    private static final String KEY_TIMER_TIME = "timer_time";
    private static final String KEY_PRO = "pro";
    private static final String KEY_VERSION = "version";
    private static final String KEY_AUTO_UPDATE = "auto_update";
    private static final String KEY_CHECK_LAST_TIME = "check_last_time";
    private static final String KEY_LAUNCH_GAME = "launch_game";
    private static final String KEY_GAME_SERVER = "game_server";
    private static final String KEY_HEADHUNT_COUNT = "headhunt_count";
    private static final String KEY_ASCENDING_STAR = "ascending_star";
    private static final String KEY_RECRUIT_PREVIEW = "recruit_preview";
    private static final String KEY_SCROLL_TO_RESULT = "scroll_to_result";
    private static final int[] TIMER_CONFIG = {0, 10, 15, 30, 45, 60, 90, 120};
    private static final int TIMER_POSITION = 1;
    private static final int UPDATE_TIME = 2000;
    private static final int CHECK_TIME = 28800000;
    private Context context;
    private SharedPreferences preferences;

    public PreferenceManager(Context context) {
        this.context = context;
        preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        if (getVersionCode() < BuildConfig.VERSION_CODE) {
            try {
                AppUtil.updateData(this, false);
                setCheckLastTime();
                setVersionCode();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), Log.getStackTraceString(e));
            }
        }
    }

    public int getBlueX() {
        return preferences.getInt(KEY_BLUE_X, 0);
    }

    public int getBlueY() {
        return preferences.getInt(KEY_BLUE_Y, 0);
    }

    public int getRedX() {
        return preferences.getInt(KEY_RED_X, 0);
    }

    public int getRedY() {
        return preferences.getInt(KEY_RED_Y, 0);
    }

    public int getGreenX() {
        return preferences.getInt(KEY_GREEN_X, 0);
    }

    public int getGreenY() {
        return preferences.getInt(KEY_GREEN_Y, 0);
    }

    public void setResolutionConfig(boolean fromWeb) throws IOException, JSONException {
        int[] res = ResolutionConfig.getAbsoluteResolution(context);
        JSONArray array = AppUtil.getResolutionArray(context, fromWeb);
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj.getInt(KEY_WIDTH) == res[0] && obj.getInt(KEY_HEIGHT) == res[1]) {
                preferences.edit().putInt(KEY_BLUE_X, obj.getInt(KEY_BLUE_X)).apply();
                preferences.edit().putInt(KEY_BLUE_Y, obj.getInt(KEY_BLUE_Y)).apply();
                preferences.edit().putInt(KEY_RED_X, obj.getInt(KEY_RED_X)).apply();
                preferences.edit().putInt(KEY_RED_Y, obj.getInt(KEY_RED_Y)).apply();
                preferences.edit().putInt(KEY_GREEN_X, res[0] - RandomUtil.RANDOM_P).apply();
                preferences.edit().putInt(KEY_GREEN_Y, res[1] >> 2).apply();
                break;
            }
        }
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

    public void setPro(boolean pro) {
        preferences.edit().putBoolean(KEY_PRO, pro).apply();
        context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, GestureService.class.getName()), pro ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
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

    public boolean resolutionSupported() {
        return getBlueX() > 0 && getBlueY() > 0 && getRedX() > 0 && getRedY() > 0 && getGreenX() > 0 && getGreenY() > 0;
    }

    public String[] getTimerStrings(Context context) {
        String[] strings = new String[TIMER_CONFIG.length];
        for (int i = 0; i < TIMER_CONFIG.length; i++)
            strings[i] = TIMER_CONFIG[i] == 0 ? context.getString(R.string.info_timer_none) : context.getString(R.string.info_timer_min, TIMER_CONFIG[i]);
        return strings;
    }

    public int getTimerPosition() {
        for (int i = 0; i < TIMER_CONFIG.length; i++) {
            if (getTimerTime() == TIMER_CONFIG[i])
                return i;
        }
        return TIMER_POSITION;
    }

    public void setCheckLastTime() {
        preferences.edit().putLong(KEY_CHECK_LAST_TIME, System.currentTimeMillis()).apply();
    }

    public long getCheckLastTime() {
        return preferences.getLong(KEY_CHECK_LAST_TIME, 0);
    }

    public boolean autoUpdate() {
        // 每隔8小时自动获取更新
        return preferences.getBoolean(KEY_AUTO_UPDATE, true) && System.currentTimeMillis() - getCheckLastTime() > CHECK_TIME;
    }

    public boolean launchGame() {
        return preferences.getBoolean(KEY_LAUNCH_GAME, false);
    }

    public void setGamePackage(String packageName) {
        preferences.edit().putString(KEY_GAME_SERVER, packageName).apply();
    }

    @Nullable
    public String getGamePackage() {
        return preferences.getString(KEY_GAME_SERVER, null);
    }

    @Nullable
    public String getDefaultGamePackage() {
        for (String packageName : context.getResources().getStringArray(R.array.game_server_values)) {
            if (context.getPackageManager().getLaunchIntentForPackage(packageName) != null)
                return packageName;
        }
        return null;
    }

    public int getGamePackagePosition() {
        String packageName = getGamePackage();
        if (packageName == null)
            packageName = getDefaultGamePackage();
        if (packageName != null) {
            String[] values = context.getResources().getStringArray(R.array.game_server_values);
            for (int i = 0; i < values.length; i++)
                if (packageName.equals(values[i]))
                    return i;
        }
        return PACKAGE_EN;
    }

    public int getTranslationIndex() {
        String packageName = getGamePackage();
        String[] values = getContext().getResources().getStringArray(R.array.game_server_values);
        if (packageName == null)
            packageName = getDefaultGamePackage();
        if (packageName == null || packageName.equals(values[PACKAGE_EN]))
            return RecruitTag.INDEX_EN;
        if (packageName.equals(values[PACKAGE_JP]))
            return RecruitTag.INDEX_JP;
        return RecruitTag.INDEX_CN;
    }

    public void setHeadhuntCount(int count) {
        preferences.edit().putInt(KEY_HEADHUNT_COUNT, count).apply();
    }

    public int getHeadhuntCount() {
        return preferences.getInt(KEY_HEADHUNT_COUNT, 0);
    }

    public boolean ascendingStar() {
        return preferences.getBoolean(KEY_ASCENDING_STAR, true);
    }

    public boolean recruitPreview() {
        return preferences.getBoolean(KEY_RECRUIT_PREVIEW, false);
    }

    public boolean scrollToResult() {
        return preferences.getBoolean(KEY_SCROLL_TO_RESULT, true);
    }

    public Context getContext() {
        return context;
    }
}