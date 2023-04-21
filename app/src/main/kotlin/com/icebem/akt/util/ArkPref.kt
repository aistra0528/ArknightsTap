package com.icebem.akt.util

import android.content.ComponentName
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.provider.Settings
import com.icebem.akt.ArkApp.Companion.app
import com.icebem.akt.BuildConfig
import com.icebem.akt.R
import com.icebem.akt.service.GestureService
import org.json.JSONArray

object ArkPref {
    private const val PACKAGE_TW = 2
    private const val PACKAGE_EN = 3
    private const val PACKAGE_JP = 4
    private const val PACKAGE_KR = 5
    private const val KEY_WIDTH = "width"
    private const val KEY_HEIGHT = "height"
    private const val KEY_BLUE_X = "blue_x"
    private const val KEY_BLUE_Y = "blue_y"
    private const val KEY_RED_X = "red_x"
    private const val KEY_RED_Y = "red_y"
    private const val KEY_SPRITE_X = "sprite_x"
    private const val KEY_SPRITE_Y = "sprite_y"
    private const val KEY_PRO = "pro"
    private const val KEY_ACTIVATED_ID = "activated_id"
    private const val KEY_VERSION_CODE = "version_code"
    private const val KEY_VERSION_NAME = "version_name"
    private const val KEY_TIMER_TIME = "timer_time"
    private const val KEY_AUTO_UPDATE = "auto_update"
    private const val KEY_CHECK_LAST_TIME = "check_last_time"
    private const val KEY_GAME_SERVER = "game_server"
    private const val KEY_LAUNCH_GAME = "launch_game"
    private const val KEY_ANTI_BURN_IN = "anti_burn_in"
    private const val KEY_ASCENDING_STAR = "ascending_star"
    private const val KEY_SCROLL_TO_RESULT = "scroll_to_result"
    private const val KEY_RECRUIT_PREVIEW = "recruit_preview"
    private const val KEY_GREEN_POINT = "green_point"
    private const val KEY_DOUBLE_SPEED = "double_speed"
    private const val KEY_VOLUME_CONTROL = "volume_control"
    private const val KEY_NO_BACKGROUND = "no_background"
    private const val KEY_HEADHUNT_COUNT_NORMAL = "headhunt_count"
    private const val KEY_HEADHUNT_COUNT_LIMITED = "headhunt_count_limited"
    private const val TIMER_POSITION = 1
    private const val UPDATE_TIME = 2500
    private const val CHECK_TIME = 86400000

    private val TIMER_CONFIG = intArrayOf(0, 10, 15, 20, 30, 45, 60, 90, 120)
    private lateinit var points: IntArray
    private var autoUpdated = false
    private var preferences: SharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(app)

    init {
        if (versionCode < BuildConfig.VERSION_CODE || versionName != BuildConfig.VERSION_NAME) {
            runCatching {
                ArkData.resetData()
                setVersionCode()
                setVersionName()
            }
        }
    }

    private val sloganList by lazy { mutableListOf<Int>() }
    private val sloganArray by lazy {
        runCatching {
            ArkData.getSloganData()
        }.getOrDefault(JSONArray())
    }

    val nextSlogan: String
        get() = if (sloganArray.length() > 0) {
            if (sloganList.isEmpty()) {
                for (i in 0 until sloganArray.length()) sloganList.add(i)
            }
            val i = Random.randomIndex(sloganList.size)
            val obj = sloganArray.getJSONObject(sloganList[i])
            sloganList.removeAt(i)
            app.getString(R.string.operator_slogan, obj.getString("name"), obj.getString("slogan"))
        } else app.getString(R.string.error_slogan)

    val blueX: Int get() = points[0]
    val blueY: Int get() = points[1]
    val redX: Int get() = points[2]
    val redY: Int get() = points[3]
    val greenX: Int get() = points[4]
    val greenY: Int get() = points[5]

    fun setSpritePosition(x: Int, y: Int) {
        preferences.edit().putInt(KEY_SPRITE_X, x).apply()
        preferences.edit().putInt(KEY_SPRITE_Y, y).apply()
    }

    val spriteX: Int get() = preferences.getInt(KEY_SPRITE_X, 0)
    val spriteY: Int get() = preferences.getInt(KEY_SPRITE_Y, 0)

    var isPro: Boolean
        get() = preferences.getBoolean(KEY_PRO, false)
        set(pro) {
            preferences.edit().putBoolean(KEY_PRO, pro).apply()
            app.packageManager.setComponentEnabledSetting(ComponentName(app, GestureService::class.java.name), if (pro) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        }

    val isActivated: Boolean get() = activatedId == androidId

    fun setActivatedId() {
        preferences.edit().putString(KEY_ACTIVATED_ID, androidId).apply()
    }

    private val activatedId: String? get() = preferences.getString(KEY_ACTIVATED_ID, null)
    private val androidId: String get() = Settings.System.getString(app.contentResolver, Settings.Secure.ANDROID_ID)

    private fun setVersionCode() = preferences.edit().putInt(KEY_VERSION_CODE, BuildConfig.VERSION_CODE).apply()
    private val versionCode: Int get() = preferences.getInt(KEY_VERSION_CODE, 0)
    private fun setVersionName() = preferences.edit().putString(KEY_VERSION_NAME, BuildConfig.VERSION_NAME).apply()
    private val versionName: String get() = preferences.getString(KEY_VERSION_NAME, BuildConfig.VERSION_NAME)!!

    var timerTime: Int
        get() = preferences.getInt(KEY_TIMER_TIME, TIMER_CONFIG[TIMER_POSITION])
        set(position) {
            preferences.edit().putInt(KEY_TIMER_TIME, TIMER_CONFIG[position]).apply()
        }

    val timerStrings: Array<String?>
        get() = arrayOfNulls<String>(TIMER_CONFIG.size).also {
            for (i in TIMER_CONFIG.indices) it[i] = if (TIMER_CONFIG[i] == 0) app.getString(R.string.info_timer_none) else app.getString(R.string.info_timer_min, TIMER_CONFIG[i])
        }

    val timerPosition: Int
        get() {
            for (i in TIMER_CONFIG.indices) {
                if (timerTime == TIMER_CONFIG[i]) return i
            }
            return TIMER_POSITION
        }

    val autoUpdate: Boolean
        get() {
            if (!autoUpdated) {
                autoUpdated = true
                // 每隔24小时自动获取更新
                return preferences.getBoolean(KEY_AUTO_UPDATE, true) && System.currentTimeMillis() - checkLastTime > CHECK_TIME
            }
            return false
        }

    fun setCheckLastTime(now: Boolean) = preferences.edit().putLong(KEY_CHECK_LAST_TIME, if (now) System.currentTimeMillis() else 0).apply()
    val checkLastTime: Long get() = preferences.getLong(KEY_CHECK_LAST_TIME, 0)

    fun setGamePackage(packageName: String) = preferences.edit().putString(KEY_GAME_SERVER, packageName).apply()
    val multiPackage: Boolean get() = availablePackages.size > 1
    val availablePackages: List<String>
        get() = app.resources.getStringArray(R.array.game_server_values).filter {
            app.packageManager.getLaunchIntentForPackage(it) != null
        }

    val defaultPackage: String?
        get() {
            val selected = preferences.getString(KEY_GAME_SERVER, null)
            if (selected != null && app.packageManager.getLaunchIntentForPackage(selected) != null) return selected
            for (installed in availablePackages) return installed
            return selected
        }
    val gamePackagePosition: Int
        get() {
            val packageName = defaultPackage
            availablePackages.forEachIndexed { i, pkg ->
                if (packageName == pkg) return i
            }
            return 0
        }
    val translationIndex: Int
        get() {
            val packages = app.resources.getStringArray(R.array.game_server_values)
            return when (defaultPackage) {
                packages[PACKAGE_TW] -> ArkData.INDEX_TW
                packages[PACKAGE_EN] -> ArkData.INDEX_EN
                packages[PACKAGE_JP] -> ArkData.INDEX_JP
                packages[PACKAGE_KR] -> ArkData.INDEX_KR
                else -> ArkData.INDEX_CN
            }
        }

    val launchGame: Boolean get() = preferences.getBoolean(KEY_LAUNCH_GAME, false)
    val antiBurnIn: Boolean get() = preferences.getBoolean(KEY_ANTI_BURN_IN, false)
    val ascendingStar: Boolean get() = preferences.getBoolean(KEY_ASCENDING_STAR, true)
    val scrollToResult: Boolean get() = preferences.getBoolean(KEY_SCROLL_TO_RESULT, true)
    val recruitPreview: Boolean get() = preferences.getBoolean(KEY_RECRUIT_PREVIEW, false)

    val unsupportedResolution: Boolean
        get() {
            runCatching {
                if (ArkData.hasGestureData) return false
                val res = Resolution.physicalResolution
                val array = ArkData.getResolutionData()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    if (obj.getInt(KEY_WIDTH) == res[0] && obj.getInt(KEY_HEIGHT) == res[1]) {
                        points = intArrayOf(obj.getInt(KEY_BLUE_X), obj.getInt(KEY_BLUE_Y), obj.getInt(KEY_RED_X), obj.getInt(KEY_RED_Y), res[0] - Random.DELTA_POINT, res[1] shr 1)
                        return false
                    }
                }
            }
            return true
        }

    val greenPoint: Boolean get() = preferences.getBoolean(KEY_GREEN_POINT, false)
    private val doubleSpeed: Boolean get() = preferences.getBoolean(KEY_DOUBLE_SPEED, false)
    val updateTime: Int get() = if (doubleSpeed) UPDATE_TIME shr 1 else UPDATE_TIME
    val volumeControl: Boolean get() = preferences.getBoolean(KEY_VOLUME_CONTROL, true)
    val noBackground: Boolean get() = preferences.getBoolean(KEY_NO_BACKGROUND, false)

    fun setHeadhuntCount(count: Int, limited: Boolean) = preferences.edit().putInt(if (limited) KEY_HEADHUNT_COUNT_LIMITED else KEY_HEADHUNT_COUNT_NORMAL, count).apply()
    fun getHeadhuntCount(limited: Boolean): Int = preferences.getInt(if (limited) KEY_HEADHUNT_COUNT_LIMITED else KEY_HEADHUNT_COUNT_NORMAL, 0)
}