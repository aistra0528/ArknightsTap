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
import java.lang.ref.WeakReference

/**
 * 悬浮吐司，有点问题 TODO
 */
object OverlayToast {
    const val LENGTH_INDEFINITE = 0L
    const val LENGTH_SHORT = 2000L
    const val LENGTH_LONG = 3000L
    private var runnable: Runnable? = null
    private var toast: WeakReference<OverlayView?>? = null

    @JvmStatic
    fun show(context: Context, text: String, duration: Long) {
        val view: MaterialTextView
        if (toast == null || toast!!.get() == null) {
            view = MaterialTextView(ContextThemeWrapper(context.applicationContext, R.style.Theme_MaterialComponents_Light))
            val padding = context.resources.getDimensionPixelOffset(R.dimen.view_padding)
            view.setPadding(padding, padding, padding, padding)
            view.setBackgroundResource(R.drawable.bg_toast)
            TextViewCompat.setTextAppearance(view, R.style.TextAppearance_AppCompat)
            view.setOnClickListener {
                it.removeCallbacks(runnable)
                toast!!.get()!!.remove()
            }
            toast = WeakReference(OverlayView(view))
            toast!!.get()!!.setRelativePosition(0, ResolutionConfig.getAbsoluteHeight(context) shr 2)
        } else {
            view = toast!!.get()!!.view as MaterialTextView
        }
        if (runnable == null) {
            runnable = Runnable { toast!!.get()!!.remove() }
        } else {
            view.removeCallbacks(runnable)
            toast!!.get()!!.remove()
        }
        view.text = text
        toast!!.get()!!.show()
        if (duration > LENGTH_INDEFINITE) view.postDelayed(runnable, duration)
    }

    @JvmStatic
    fun show(context: Context, resId: Int, duration: Long) = show(context, context.getString(resId), duration)
}