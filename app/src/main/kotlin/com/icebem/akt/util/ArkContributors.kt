package com.icebem.akt.util

import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

object ArkContributors {
    private const val CONTRIBUTORS_DATA_NAME = "contributors.json"
    private var CONTRIBUTORS_DATA : JSONArray? = null

    @Throws(IOException::class, JSONException::class)
    fun getContributorsData() : JSONArray {
        if (CONTRIBUTORS_DATA == null) {
            synchronized(ArkContributors) {
                if (CONTRIBUTORS_DATA == null) {
                    CONTRIBUTORS_DATA = JSONArray(ArkIO.fromAssets(CONTRIBUTORS_DATA_NAME))
                }
            }
        }
        return CONTRIBUTORS_DATA!!
    }
}