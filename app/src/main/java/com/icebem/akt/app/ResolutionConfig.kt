package com.icebem.akt.app

import android.content.Context

object ResolutionConfig {
    fun getAbsoluteResolution(context: Context): IntArray {
        val res = intArrayOf(0, 0)
        val metric = CompatOperations.getDisplayMetrics(context)
        res[0] = metric.widthPixels.coerceAtLeast(metric.heightPixels)
        res[1] = metric.widthPixels.coerceAtMost(metric.heightPixels)
        return res
    }

    fun getAbsoluteHeight(context: Context): Int = getAbsoluteResolution(context)[1]
}