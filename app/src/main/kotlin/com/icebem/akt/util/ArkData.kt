/*
 * This file is part of ArkTap.
 * Copyright (C) 2019-2021 艾星Aistra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.icebem.akt.util

import androidx.lifecycle.MutableLiveData
import com.icebem.akt.ArkApp.Companion.app
import com.icebem.akt.BuildConfig
import com.icebem.akt.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

/**
 * 数据更新相关
 */
object ArkData {
    const val INDEX_EN = 0
    const val INDEX_CN = 1
    const val INDEX_TW = 2
    const val INDEX_JP = 3
    const val INDEX_KR = 4
    const val FLAG_UNRELEASED = "*"
    const val KEY_NAME = "name"
    const val KEY_DELAY = "delay"
    const val KEY_TAP = "tap"
    const val KEY_X = "x"
    const val KEY_Y = "y"
    private const val KEY_COMPAT = "compat"
    private const val KEY_VERSION = "version"
    private const val TYPE_DATA = "data"
    private const val DATA_ENTRY = "entry.json"
    private const val DATA_MATERIAL = "material.json"
    private const val DATA_RECRUIT = "recruit.json"
    private const val DATA_RESOLUTION = "resolution.json"
    private const val DATA_SLOGAN = "slogan.json"
    private const val DATA_GESTURE = "gesture.json"
    private const val URL_WEB_DATA = "https://gitee.com/aistra0528/ArknightsTap/raw/master/app/src/main/assets/data/"
    val updateResult = MutableLiveData<JSONArray?>()

    suspend fun requireUpdate() {
        var id = R.string.version_update
        var log = String()
        var url = ArkMaid.URL_RELEASE_LATEST
        runCatching {
            val entry = requestOnlineEntry()
            if (latestApp(entry)) {
                id = if (updateData(entry)) R.string.data_updated else R.string.version_latest
            } else {
                val json = JSONObject(ArkIO.fromWeb(ArkMaid.URL_RELEASE_LATEST_API))
                log = getChangelog(json)
                url = getDownloadUrl(json)
            }
            ArkPref.setCheckLastTime(true)
        }.onFailure {
            id = R.string.version_checking_failed
            log = it.toString()
        }
        updateResult.postValue(JSONArray().put(id).put(log).put(url))
    }

    @Throws(IOException::class, JSONException::class)
    suspend fun updateData(onlineEntry: JSONArray): Boolean {
        val entry = getOfflineData(DATA_ENTRY)
        var updated = false
        for (i in 0 until onlineEntry.length()) {
            val data = onlineEntry.getJSONObject(i)
            if (data.getString(KEY_NAME) == DATA_ENTRY) {
                if (!compatible(data)) return updated
                if (updatable(data, entry)) updated = true
            } else if (compatible(data) && (updatable(data, entry))) {
                updateOfflineData(data.getString(KEY_NAME), onlineEntry)
                updated = true
            }
        }
        if (updated) updateOfflineData(DATA_ENTRY, onlineEntry)
        return updated
    }

    @Throws(IOException::class, JSONException::class)
    fun resetData() = ArkIO.clearDirectory(app.filesDir.path)

    @Throws(IOException::class)
    private suspend fun updateOfflineData(data: String, onlineEntry: JSONArray) {
        ArkIO.writeText(app.filesDir.path + File.separatorChar + data, when (data) {
            DATA_ENTRY -> onlineEntry.toString()
            else -> ArkIO.fromWeb(URL_WEB_DATA + data)
        })
    }

    val hasGestureData get() = ArkIO.exists(app.filesDir.path + File.separatorChar + DATA_GESTURE)

    @Throws(IOException::class)
    fun resetGestureData() = ArkIO.delete(app.filesDir.path + File.separatorChar + DATA_GESTURE)

    fun setGestureData(string: String? = null): Boolean {
        string ?: return false
        runCatching {
            val array = JSONArray(string)
            if (array.length() == 0) return false
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                if (obj.optString(KEY_NAME) != KEY_TAP || obj.optInt(KEY_X) <= 0 || obj.optInt(KEY_Y) <= 0)
                    return false
            }
            ArkIO.writeText(app.filesDir.path + File.separatorChar + DATA_GESTURE, string)
            return true
        }
        return false
    }

    @Throws(IOException::class, JSONException::class)
    private fun getOfflineData(data: String): JSONArray {
        val path = app.filesDir.path + File.separatorChar + data
        return JSONArray(if (ArkIO.exists(path)) ArkIO.fromFile(path) else getAssetsData(data))
    }

    private fun getAssetsData(data: String) = ArkIO.fromAssets(TYPE_DATA + File.separatorChar + data)

    @Throws(IOException::class, JSONException::class)
    fun getMaterialData(): JSONArray = getOfflineData(DATA_MATERIAL)

    @Throws(IOException::class, JSONException::class)
    fun getRecruitData(): JSONArray = getOfflineData(DATA_RECRUIT)

    @Throws(IOException::class, JSONException::class)
    fun getResolutionData(): JSONArray = getOfflineData(DATA_RESOLUTION)

    @Throws(IOException::class, JSONException::class)
    fun getSloganData(): JSONArray = getOfflineData(DATA_SLOGAN)

    @Throws(IOException::class, JSONException::class)
    fun getGestureData(): JSONArray = getOfflineData(DATA_GESTURE)

    @Throws(IOException::class, JSONException::class)
    private suspend fun requestOnlineEntry(): JSONArray = JSONArray(ArkIO.fromWeb(URL_WEB_DATA + DATA_ENTRY))

    @Throws(JSONException::class)
    private fun updatable(data: JSONObject, entry: JSONArray): Boolean {
        for (i in 0 until entry.length()) if (entry.getJSONObject(i).getString(KEY_NAME) == data.getString(KEY_NAME)) return data.getInt(KEY_VERSION) > entry.getJSONObject(i).getInt(KEY_VERSION)
        return false
    }

    @Throws(JSONException::class)
    private fun compatible(data: JSONObject): Boolean = BuildConfig.VERSION_CODE >= data.getInt(KEY_COMPAT)

    @Throws(JSONException::class)
    private fun latestApp(onlineEntry: JSONArray): Boolean = BuildConfig.VERSION_CODE >= onlineEntry.getJSONObject(0).getInt(KEY_VERSION)

    @Throws(JSONException::class)
    private fun getChangelog(json: JSONObject): String = json.getString("name") + System.lineSeparator() + json.getString("body")

    @Throws(JSONException::class)
    private fun getDownloadUrl(json: JSONObject): String = json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url")
}