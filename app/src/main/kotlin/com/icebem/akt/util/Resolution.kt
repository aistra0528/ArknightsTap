package com.icebem.akt.util

import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.icebem.akt.ArkApp.Companion.app

object Resolution {
    val physicalResolution: IntArray
        get() = ContextCompat.getSystemService(app, WindowManager::class.java)!!.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) currentWindowMetrics.bounds.run {
                intArrayOf(width(), height())
            } else DisplayMetrics().also { defaultDisplay.getRealMetrics(it) }.run {
                intArrayOf(widthPixels, heightPixels)
            }
        }.sortedArrayDescending()
    val physicalHeight: Int get() = physicalResolution[1]
}