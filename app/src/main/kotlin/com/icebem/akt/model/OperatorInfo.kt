package com.icebem.akt.model

import com.icebem.akt.util.ArkData
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

        @get:Throws(IOException::class, JSONException::class)
        val array: Array<OperatorInfo>
            get() {
                val array = ArkData.getRecruitData()
                return mutableListOf<OperatorInfo>().apply {
                    for (i in 0 until array.length()) add(OperatorInfo(array.getJSONObject(i)))
                }.toTypedArray()
            }
    }

    val star: Int = obj.getInt(KEY_STAR)
    private val name: String = obj.getString(KEY_NAME)
    private val nameCN: String = obj.getString(KEY_NAME_CN)
    private val nameTW: String = obj.getString(KEY_NAME_TW)
    private val nameJP: String = obj.getString(KEY_NAME_JP)
    private val nameKR: String = obj.getString(KEY_NAME_KR)
    val type: String = obj.getString(KEY_TYPE)
    val tags: Array<String> = mutableListOf<String>().apply {
        val array = obj.getJSONArray(KEY_TAGS)
        for (i in 0 until array.length()) add(array.getString(i))
    }.toTypedArray()

    fun containsTag(tag: String): Boolean = tag in tags

    fun getName(index: Int): String = when (index) {
        ArkData.INDEX_EN -> name
        ArkData.INDEX_TW -> nameTW
        ArkData.INDEX_JP -> nameJP
        ArkData.INDEX_KR -> nameKR
        else -> nameCN
    }
}