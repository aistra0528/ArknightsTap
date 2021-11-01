package com.icebem.akt.model

import android.content.Context
import com.icebem.akt.util.DataUtil
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

        @Throws(IOException::class, JSONException::class)
        fun load(context: Context): Array<MaterialInfo?> {
            val array = DataUtil.getMaterialData(context)
            val infoList = arrayOfNulls<MaterialInfo>(array.length())
            for (i in infoList.indices) infoList[i] = MaterialInfo(array.getJSONObject(i))
            return infoList
        }
    }

    val id: Int = obj.getInt(KEY_ID)
    private val name: String = obj.getString(KEY_NAME)
    private val nameCN: String = obj.getString(KEY_NAME_CN)
    private val nameTW: String = obj.getString(KEY_NAME_TW)
    private val nameJP: String = obj.getString(KEY_NAME_JP)
    private val nameKR: String = obj.getString(KEY_NAME_KR)
    val stages: Array<Mission?> = arrayOfNulls(obj.getJSONArray(KEY_STAGES).length())
    val items: Array<ShopItem?> = arrayOfNulls(obj.getJSONArray(KEY_WORKSHOP).length())

    init {
        for (i in stages.indices) stages[i] = Mission(obj.getJSONArray(KEY_STAGES).getJSONObject(i))
        for (i in items.indices) items[i] = ShopItem(obj.getJSONArray(KEY_WORKSHOP).getJSONObject(i))
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