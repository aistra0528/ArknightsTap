package com.icebem.akt.util

import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.icebem.akt.ArkApp.Companion.app

object Resolution {
    @Suppress("DEPRECATION")
    val displayMetrics: DisplayMetrics
        get() = DisplayMetrics().also {
            ContextCompat.getSystemService(app, WindowManager::class.java)!!.defaultDisplay.getRealMetrics(it)
        }
    val absoluteResolution: IntArray
        get() = displayMetrics.let {
            intArrayOf(it.widthPixels.coerceAtLeast(it.heightPixels), it.widthPixels.coerceAtMost(it.heightPixels))
        }
    val absoluteHeight: Int get() = absoluteResolution[1]
}