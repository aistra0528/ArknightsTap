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
        private const val KEY_LANGUAGES = "languages"
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
    private val languages: Array<String> = mutableListOf<String>().apply {
        val array = obj.getJSONArray(KEY_LANGUAGES)
        for (i in 0 until array.length()) add(array.getString(i))
    }.toTypedArray()
    private val networks: Array<String> = mutableListOf<String>().apply {
        val array = obj.getJSONArray(KEY_NETWORKS)
        for (i in 0 until array.length()) add(array.getString(i))
    }.toTypedArray()

    fun toLocalizedString(resources: Resources): String = buildString {
        if (networks.isNotEmpty()) append(networks.joinToString(separator = "/", postfix = " @"))
        append(name)
        append(" - ")
        append(resources.getString(R.string.contributor))
        if (languages.isNotEmpty()) {
            append("&")
            append(resources.getString(R.string.translator))
            append(languages.joinToString(prefix = " (", postfix = ")"))
        }
    }
}