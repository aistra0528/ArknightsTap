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

import android.content.Context
import com.icebem.akt.BuildConfig
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * 数据更新相关
 */
object DataUtil {
    const val INDEX_EN = 0
    const val INDEX_CN = 1
    const val INDEX_TW = 2
    const val INDEX_JP = 3
    const val INDEX_KR = 4
    const val FLAG_UNRELEASED = "*"
    private const val KEY_NAME = "name"
    private const val KEY_COMPAT = "compat"
    private const val KEY_VERSION = "version"
    private const val TYPE_DATA = "data"
    private const val DATA_ENTRY = "entry.json"
    private const val DATA_MATERIAL = "material.json"
    private const val DATA_RECRUIT = "recruit.json"
    private const val DATA_RESOLUTION = "resolution.json"
    private const val DATA_SLOGAN = "slogan.json"
    private const val URL_WEB_DATA = "https://gitee.com/aistra0528/ArknightsTap/raw/master/app/src/main/assets/data/"

    /**
     * 更新/重置本地数据
     *
     * @param context 上下文
     * @param onlineEntry 传入 null 时重置本地数据
     * @return 是否更新/重置了数据
     */
    @JvmStatic
    @Throws(IOException::class, JSONException::class)
    fun updateData(context: Context, onlineEntry: JSONArray?): Boolean {
        val newEntry = onlineEntry ?: fromStream(IOUtil.fromAssets(context, TYPE_DATA + File.separatorChar + DATA_ENTRY))
        val entry = getOfflineData(context, DATA_ENTRY)
        var updated = false
        for (i in 0 until newEntry.length()) {
            val data = newEntry.getJSONObject(i)
            if (data.getString(KEY_NAME) == DATA_ENTRY) {
                if (!compatible(data)) return updated
                if (onlineEntry == null || updatable(data, entry)) updated = true
            } else if (compatible(data) && (onlineEntry == null || updatable(data, entry))) {
                setOfflineData(context, data.getString(KEY_NAME), onlineEntry)
                updated = true
            }
        }
        if (updated) setOfflineData(context, DATA_ENTRY, onlineEntry)
        return updated
    }

    @Throws(IOException::class)
    private fun setOfflineData(context: Context, data: String, onlineEntry: JSONArray?) {
        IOUtil.stream2File(when {
            onlineEntry == null -> IOUtil.fromAssets(context, TYPE_DATA + File.separatorChar + data)
            data == DATA_ENTRY -> ByteArrayInputStream(onlineEntry.toString().toByteArray())
            else -> IOUtil.fromWeb(URL_WEB_DATA + data)
        }, context.filesDir.toString() + File.separator + data)
    }

    @JvmStatic
    @Throws(IOException::class, JSONException::class)
    fun getOnlineEntry(): JSONArray = fromStream(IOUtil.fromWeb(URL_WEB_DATA + DATA_ENTRY))

    @Throws(IOException::class, JSONException::class)
    private fun fromStream(stream: InputStream): JSONArray = JSONArray(IOUtil.stream2String(stream))

    @Throws(IOException::class, JSONException::class)
    private fun getOfflineData(context: Context, data: String): JSONArray {
        val file = File(context.filesDir.toString() + File.separator + data)
        return fromStream(if (file.exists()) IOUtil.fromFile(file) else IOUtil.fromAssets(context, TYPE_DATA + File.separatorChar + data))
    }

    @JvmStatic
    @Throws(IOException::class, JSONException::class)
    fun getMaterialData(context: Context): JSONArray = getOfflineData(context, DATA_MATERIAL)

    @JvmStatic
    @Throws(IOException::class, JSONException::class)
    fun getRecruitData(context: Context): JSONArray = getOfflineData(context, DATA_RECRUIT)

    @JvmStatic
    @Throws(IOException::class, JSONException::class)
    fun getResolutionData(context: Context): JSONArray = getOfflineData(context, DATA_RESOLUTION)

    @Throws(IOException::class, JSONException::class)
    fun getSloganData(context: Context): JSONArray = getOfflineData(context, DATA_SLOGAN)

    @Throws(JSONException::class)
    private fun updatable(data: JSONObject, entry: JSONArray): Boolean {
        for (i in 0 until entry.length())
            if (entry.getJSONObject(i).getString(KEY_NAME) == data.getString(KEY_NAME))
                return data.getInt(KEY_VERSION) > entry.getJSONObject(i).getInt(KEY_VERSION)
        return false
    }

    @Throws(JSONException::class)
    private fun compatible(data: JSONObject): Boolean = BuildConfig.VERSION_CODE >= data.getInt(KEY_COMPAT)

    @JvmStatic
    @Throws(JSONException::class)
    fun latestApp(onlineEntry: JSONArray): Boolean = BuildConfig.VERSION_CODE >= onlineEntry.getJSONObject(0).getInt(KEY_VERSION)

    @JvmStatic
    @Throws(JSONException::class)
    fun getChangelog(json: JSONObject): String = json.getString("name") + System.lineSeparator() + json.getString("body")

    @JvmStatic
    @Throws(JSONException::class)
    fun getDownloadUrl(json: JSONObject): String = json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url")
}