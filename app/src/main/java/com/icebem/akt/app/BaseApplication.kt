package com.icebem.akt.app

import android.app.ActivityManager
import android.app.Application
import android.provider.Settings
import com.icebem.akt.service.GestureService
import com.icebem.akt.service.OverlayService

class BaseApplication : Application() {
    val isOverlayServiceRunning: Boolean get() = isServiceRunning(OverlayService::class.java.name)
    val isGestureServiceRunning: Boolean get() = isServiceRunning(GestureService::class.java.name)
    val isGestureServiceEnabled: Boolean
        get() {
            val pref = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            return pref != null && pref.contains(packageName + "/" + GestureService::class.java.name)
        }

    private fun isServiceRunning(name: String): Boolean {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (info in am.getRunningServices(Int.MAX_VALUE)) if (info.service.className == name) return true
        return false
    }
}