package com.icebem.akt

import android.app.Application
import com.icebem.akt.util.ArkIO

class ArkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        app = this
        clearCache()
    }

    private fun clearCache() = ArkIO.clearDirectory(cacheDir.path)

    companion object {
        lateinit var app: ArkApp private set
    }
}