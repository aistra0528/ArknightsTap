package com.icebem.akt.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.service.GestureService;
import com.icebem.akt.util.DataUtil;
import com.icebem.akt.util.RandomUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class PreferenceManager {
    private static final int PACKAGE_TW = 2;
    private static final int PACKAGE_EN = 3;
    private static final int PACKAGE_JP = 4;
    private static final int PACKAGE_KR = 5;
    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_BLUE_X = "blue_x";
    private static final String KEY_BLUE_Y = "blue_y";
    private static final String KEY_RED_X = "red_x";
    private static final String KEY_RED_Y = "red_y";
    private static final String KEY_SPRITE_X = "sprite_x";
    private static final String KEY_SPRITE_Y = "sprite_y";
    private static final String KEY_TIMER_TIME = "timer_time";
    private static final String KEY_PRO = "pro";
    private static final String KEY_VERSION_CODE = "version_code";
    private static final String KEY_VERSION_NAME = "version_name";
    private static final String KEY_AUTO_UPDATE = "auto_update";
    private static final String KEY_CHECK_LAST_TIME = "check_last_time";
    private static final String KEY_LAUNCH_GAME = "launch_game";
    private static final String KEY_GAME_SERVER = "game_server";
    private static final String KEY_HEADHUNT_COUNT_NORMAL = "headhunt_count";
    private static final String KEY_HEADHUNT_COUNT_LIMITED = "headhunt_count_limited";
    private static final String KEY_ANTI_BURN_IN = "anti_burn_in";
    private static final String KEY_NO_BACKGROUND = "no_background";
    private static final String KEY_ROOT_MODE = "root_mode";
    private static final String KEY_DOUBLE_SPEED = "double_speed";
    private static final String KEY_ASCENDING_STAR = "ascending_star";
    private static final String KEY_RECRUIT_PREVIEW = "recruit_preview";
    private static final String KEY_SCROLL_TO_RESULT = "scroll_to_result";
    private static final String KEY_ACTIVATED_ID = "activated_id";
    private static final int[] TIMER_CONFIG = {0, 10, 15, 20, 30, 45, 60, 90, 120};
    private static final int TIMER_POSITION = 1;
    private static final int UPDATE_TIME = 2500;
    private static final int CHECK_TIME = 86400000;
    private final Context context;
    private static int[] points;
    private static boolean autoUpdated;
    private static SharedPreferences preferences;
    private static PreferenceManager instance;

    private PreferenceManager(Context applicationContext) {
        context = applicationContext;
        preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        if (getVersionCode() < BuildConfig.VERSION_CODE || !getVersionName().equals(BuildConfig.VERSION_NAME)) {
            try {
                DataUtil.updateData(context, null);
                setVersionCode();
                setVersionName();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), Log.getStackTraceString(e));
            }
        }
    }

    public static PreferenceManager getInstance(Context context) {
        if (instance == null)
            instance = new PreferenceManager(context.getApplicationContext());
        return instance;
    }

    public int getBlueX() {
        return points[0];
    }

    public int getBlueY() {
        return points[1];
    }

    public int getRedX() {
        return points[2];
    }

    public int getRedY() {
        return points[3];
    }

    public int getGreenX() {
        return points[4];
    }

    public int getGreenY() {
        return points[5];
    }

    public void setSpritePosition(int x, int y) {
        preferences.edit().putInt(KEY_SPRITE_X, x).apply();
        preferences.edit().putInt(KEY_SPRITE_Y, y).apply();
    }

    public int getSpriteX() {
        return preferences.getInt(KEY_SPRITE_X, 0);
    }

    public int getSpriteY() {
        return preferences.getInt(KEY_SPRITE_Y, 0);
    }

    public int getUpdateTime() {
        return doubleSpeed() ? UPDATE_TIME >> 1 : UPDATE_TIME;
    }

    public void setTimerTime(int position) {
        preferences.edit().putInt(KEY_TIMER_TIME, TIMER_CONFIG[position]).apply();
    }

    public int getTimerTime() {
        return preferences.getInt(KEY_TIMER_TIME, TIMER_CONFIG[TIMER_POSITION]);
    }

    public void setPro(boolean pro) {
        preferences.edit().putBoolean(KEY_PRO, pro).apply();
        if (pro && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            preferences.edit().putBoolean(KEY_ROOT_MODE, true).apply();
            CompatOperations.checkRootPermission();
        }
        context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, GestureService.class.getName()), pro ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    public boolean isPro() {
        return preferences.getBoolean(KEY_PRO, false);
    }

    private void setVersionCode() {
        preferences.edit().putInt(KEY_VERSION_CODE, BuildConfig.VERSION_CODE).apply();
    }

    private int getVersionCode() {
        return preferences.getInt(KEY_VERSION_CODE, 0);
    }

    private void setVersionName() {
        preferences.edit().putString(KEY_VERSION_NAME, BuildConfig.VERSION_NAME).apply();
    }

    private String getVersionName() {
        return preferences.getString(KEY_VERSION_NAME, BuildConfig.VERSION_NAME);
    }

    public boolean isActivated() {
        String id = getActivatedId();
        return id != null && id.equals(getAndroidId());
    }

    public void setActivatedId() {
        preferences.edit().putString(KEY_ACTIVATED_ID, getAndroidId()).apply();
    }

    private String getActivatedId() {
        return preferences.getString(KEY_ACTIVATED_ID, null);
    }

    private String getAndroidId() {
        return Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public boolean unsupportedResolution() {
        try {
            int[] res = ResolutionConfig.getAbsoluteResolution(context);
            JSONArray array = DataUtil.getResolutionData(context);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (obj.getInt(KEY_WIDTH) == res[0] && obj.getInt(KEY_HEIGHT) == res[1]) {
                    points = new int[6];
                    points[0] = obj.getInt(KEY_BLUE_X);
                    points[1] = obj.getInt(KEY_BLUE_Y);
                    points[2] = obj.getInt(KEY_RED_X);
                    points[3] = obj.getInt(KEY_RED_Y);
                    points[4] = res[0] - RandomUtil.DELTA_POINT;
                    points[5] = res[1] >> 1;
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), Log.getStackTraceString(e));
        }
        return true;
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

    public void setCheckLastTime(boolean isReset) {
        preferences.edit().putLong(KEY_CHECK_LAST_TIME, isReset ? 0 : System.currentTimeMillis()).apply();
    }

    public long getCheckLastTime() {
        return preferences.getLong(KEY_CHECK_LAST_TIME, 0);
    }

    public boolean autoUpdate() {
        if (!autoUpdated) {
            autoUpdated = true;
            // 每隔24小时自动获取更新
            return preferences.getBoolean(KEY_AUTO_UPDATE, true) && System.currentTimeMillis() - getCheckLastTime() > CHECK_TIME;
        }
        return false;
    }

    public boolean launchGame() {
        return preferences.getBoolean(KEY_LAUNCH_GAME, false);
    }

    public void setGamePackage(String packageName) {
        preferences.edit().putString(KEY_GAME_SERVER, packageName).apply();
    }

    public boolean multiPackage() {
        return getAvailablePackages().size() > 1;
    }

    public ArrayList<String> getAvailablePackages() {
        ArrayList<String> availablePackages = new ArrayList<>();
        String[] packages = context.getResources().getStringArray(R.array.game_server_values);
        for (String name : packages) {
            if (context.getPackageManager().getLaunchIntentForPackage(name) != null)
                availablePackages.add(name);
        }
        return availablePackages;
    }

    @Nullable
    public String getDefaultPackage() {
        String selected = preferences.getString(KEY_GAME_SERVER, null);
        if (selected != null && context.getPackageManager().getLaunchIntentForPackage(selected) != null)
            return selected;
        for (String installed : getAvailablePackages())
            return installed;
        return null;
    }

    public int getGamePackagePosition() {
        String packageName = getDefaultPackage();
        if (packageName != null) {
            ArrayList<String> packages = getAvailablePackages();
            for (int i = 0; i < packages.size(); i++)
                if (packageName.equals(packages.get(i)))
                    return i;
        }
        return 0;
    }

    public int getTranslationIndex() {
        String packageName = getDefaultPackage();
        String[] packages = context.getResources().getStringArray(R.array.game_server_values);
        if (packageName == null)
            return DataUtil.INDEX_CN;
        if (packageName.equals(packages[PACKAGE_TW]))
            return DataUtil.INDEX_TW;
        if (packageName.equals(packages[PACKAGE_EN]))
            return DataUtil.INDEX_EN;
        if (packageName.equals(packages[PACKAGE_JP]))
            return DataUtil.INDEX_JP;
        if (packageName.equals(packages[PACKAGE_KR]))
            return DataUtil.INDEX_KR;
        return DataUtil.INDEX_CN;
    }

    public void setHeadhuntCount(int count, boolean limited) {
        preferences.edit().putInt(limited ? KEY_HEADHUNT_COUNT_LIMITED : KEY_HEADHUNT_COUNT_NORMAL, count).apply();
    }

    public int getHeadhuntCount(boolean limited) {
        return preferences.getInt(limited ? KEY_HEADHUNT_COUNT_LIMITED : KEY_HEADHUNT_COUNT_NORMAL, 0);
    }

    public boolean antiBurnIn() {
        return preferences.getBoolean(KEY_ANTI_BURN_IN, false);
    }

    public boolean noBackground() {
        return preferences.getBoolean(KEY_NO_BACKGROUND, false);
    }

    public boolean rootMode() {
        return preferences.getBoolean(KEY_ROOT_MODE, false);
    }

    private boolean doubleSpeed() {
        return preferences.getBoolean(KEY_DOUBLE_SPEED, false);
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

    public Context getApplicationContext() {
        return context;
    }
}