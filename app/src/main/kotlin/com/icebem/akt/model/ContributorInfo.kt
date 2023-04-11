package com.icebem.akt.model

import android.content.res.Resources
import com.icebem.akt.R
import com.icebem.akt.util.ArkContributors
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ContributorInfo private constructor(obj: JSONObject) {
    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_TYPE = "types"
        private const val KEY_NETWORKS = "networks"

        @get:Throws(IOException::class, JSONException::class)
        val array: Array<ContributorInfo>
            get() {
                val array = ArkContributors.getContributorsData()
                return mutableListOf<ContributorInfo>().apply {
                    for (i in 0 until array.length()) add(ContributorInfo(array.getJSONObject(i)))
                }.toTypedArray()
            }
    }

    private val name: String = obj.getString(KEY_NAME)
    private val types: Array<Int> = mutableListOf<Int>().apply {
        val array = obj.getJSONArray(KEY_TYPE)
        for (i in 0 until array.length()) add(array.getInt(i))
    }.toTypedArray()
    private val networks: Array<Int> = mutableListOf<Int>().apply {
        val array = obj.getJSONArray(KEY_NETWORKS)
        for (i in 0 until array.length()) add(array.getInt(i))
    }.toTypedArray()

    fun toLocalizedString(resources : Resources): String {
        var result = ""
        for (i in networks.indices step 1) {
            val networkName = resources.getStringArray(R.array.network_names)[networks[i]]
            result = if (i == 0) {
                networkName
            } else {
                String.format("%s\\%s", result, networkName)
            }

            if (i != networks.size - 2) {
                result = String.format("%s ", result)
            }
        }
        result = String.format("%s%s", result, name)
        for (i in types.indices step 1) {
            val typeName = resources.getStringArray(R.array.contributor_type_names)[types[i]]
            result = if (i == 0) {
                String.format("%s - %s", result, typeName)
            } else {
                String.format("%s, %s", result, typeName)
            }
        }
        return result
    }
}