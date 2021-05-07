/*
 * This file is part of ArkTap.
 * Copyright (C) 2019-2021 艾星Aistra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.icebem.akt.overlay

import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.widget.TextViewCompat
import com.google.android.material.textview.MaterialTextView
import com.icebem.akt.R
import com.icebem.akt.app.ResolutionConfig

/**
 * 悬浮吐司
 */
object OverlayToast {
    const val LENGTH_INDEFINITE = 0L
    const val LENGTH_SHORT = 2000L
    const val LENGTH_LONG = 3000L
    private val runnable = Runnable { toast!!.remove() }
    private var toast: OverlayView? = null

    @JvmStatic
    fun show(context: Context, text: String, duration: Long) {
        val view = getView(context)
        view.removeCallbacks(runnable)
        runnable.run()
        view.text = text
        toast!!.show()
        if (duration > LENGTH_INDEFINITE) view.postDelayed(runnable, duration)
    }

    @JvmStatic
    fun show(context: Context, resId: Int, duration: Long) = show(context, context.getString(resId), duration)

    private fun getView(context: Context): MaterialTextView {
        if (toast == null) {
            val view = MaterialTextView(ContextThemeWrapper(context.applicationContext, R.style.Theme_MaterialComponents_Light)).apply {
                TextViewCompat.setTextAppearance(this, R.style.TextAppearance_AppCompat)
                val padding = context.resources.getDimensionPixelOffset(R.dimen.view_padding)
                setPadding(padding, padding, padding, padding)
                setBackgroundResource(R.drawable.bg_toast)
                setOnClickListener {
                    it.removeCallbacks(runnable)
                    runnable.run()
                }
            }
            toast = OverlayView(view).apply { setRelativePosition(0, ResolutionConfig.getAbsoluteHeight(context) shr 2) }
        }
        return toast!!.view as MaterialTextView
    }
}