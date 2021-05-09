package com.icebem.akt.model

import android.content.Context
import com.icebem.akt.util.DataUtil
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

internal class OperatorInfo private constructor(obj: JSONObject) {
    companion object {
        private const val KEY_STAR = "star"
        private const val KEY_NAME = "name"
        private const val KEY_NAME_CN = "nameCN"
        private const val KEY_NAME_TW = "nameTW"
        private const val KEY_NAME_JP = "nameJP"
        private const val KEY_NAME_KR = "nameKR"
        private const val KEY_TYPE = "type"
        private const val KEY_TAGS = "tags"

        @JvmStatic
        @Throws(IOException::class, JSONException::class)
        fun load(context: Context): Array<OperatorInfo?> {
            val array = DataUtil.getRecruitData(context)
            val infoList = arrayOfNulls<OperatorInfo>(array.length())
            for (i in infoList.indices) infoList[i] = OperatorInfo(array.getJSONObject(i))
            return infoList
        }
    }

    val star: Int = obj.getInt(KEY_STAR)
    private val name: String = obj.getString(KEY_NAME)
    private val nameCN: String = obj.getString(KEY_NAME_CN)
    private val nameTW: String = obj.getString(KEY_NAME_TW)
    private val nameJP: String = obj.getString(KEY_NAME_JP)
    private val nameKR: String = obj.getString(KEY_NAME_KR)
    val type: String = obj.getString(KEY_TYPE)
    val tags: Array<String?> = arrayOfNulls(obj.getJSONArray(KEY_TAGS).length())

    init {
        for (i in tags.indices) tags[i] = obj.getJSONArray(KEY_TAGS).getString(i)
    }

    fun containsTag(tag: String): Boolean {
        for (t in tags) if (t == tag) return true
        return false
    }

    fun getName(index: Int): String {
        return when (index) {
            DataUtil.INDEX_EN -> name
            DataUtil.INDEX_TW -> nameTW
            DataUtil.INDEX_JP -> nameJP
            DataUtil.INDEX_KR -> nameKR
            else -> nameCN
        }
    }
}