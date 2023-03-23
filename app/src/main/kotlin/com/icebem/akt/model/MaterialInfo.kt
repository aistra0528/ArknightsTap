package com.icebem.akt.model

import com.icebem.akt.util.ArkData
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MaterialInfo private constructor(obj: JSONObject) {
    companion object {
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_NAME_CN = "nameCN"
        private const val KEY_NAME_TW = "nameTW"
        private const val KEY_NAME_JP = "nameJP"
        private const val KEY_NAME_KR = "nameKR"
        private const val KEY_STAGES = "stages"
        private const val KEY_WORKSHOP = "workshop"

        @get:Throws(IOException::class, JSONException::class)
        val array: Array<MaterialInfo>
            get() {
                val array = ArkData.getMaterialData()
                return mutableListOf<MaterialInfo>().apply {
                    for (i in 0 until array.length()) add(MaterialInfo(array.getJSONObject(i)))
                }.toTypedArray()
            }
    }

    val id: Int = obj.getInt(KEY_ID)
    private val name: String = obj.getString(KEY_NAME)
    private val nameCN: String = obj.getString(KEY_NAME_CN)
    private val nameTW: String = obj.getString(KEY_NAME_TW)
    private val nameJP: String = obj.getString(KEY_NAME_JP)
    private val nameKR: String = obj.getString(KEY_NAME_KR)
    val stages: Array<Mission> = mutableListOf<Mission>().apply {
        val array = obj.getJSONArray(KEY_STAGES)
        for (i in 0 until array.length()) add(Mission(array.getJSONObject(i)))
    }.toTypedArray()
    val items: Array<ShopItem> = mutableListOf<ShopItem>().apply {
        val array = obj.getJSONArray(KEY_WORKSHOP)
        for (i in 0 until array.length()) add(ShopItem(array.getJSONObject(i)))
    }.toTypedArray()

    fun getName(index: Int): String = when (index) {
        ArkData.INDEX_EN -> name
        ArkData.INDEX_TW -> nameTW
        ArkData.INDEX_JP -> nameJP
        ArkData.INDEX_KR -> nameKR
        else -> nameCN
    }

    class Mission internal constructor(obj: JSONObject) {
        companion object {
            private const val KEY_MISSION = "mission"
            private const val KEY_SANITY = "sanity"
            private const val KEY_FREQUENCY = "frequency"
        }

        val mission: String = obj.getString(KEY_MISSION)
        val sanity: Int = obj.getInt(KEY_SANITY)
        val frequency: Float = obj.getDouble(KEY_FREQUENCY).toFloat()
    }

    class ShopItem internal constructor(obj: JSONObject) {
        companion object {
            private const val KEY_QUANTITY = "quantity"
        }

        val id: Int = obj.getInt(KEY_ID)
        val quantity: Int = obj.getInt(KEY_QUANTITY)
    }
}