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

import android.annotation.SuppressLint
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.widget.TextViewCompat
import com.google.android.material.textview.MaterialTextView
import com.icebem.akt.ArkApp.Companion.app
import com.icebem.akt.R
import com.icebem.akt.util.Resolution

/**
 * 悬浮吐司
 */
object OverlayToast {
    const val LENGTH_INDEFINITE = 0L
    const val LENGTH_SHORT = 2000L
    const val LENGTH_LONG = 3000L
    private val removal = Runnable { toast?.remove() }

    @SuppressLint("StaticFieldLeak")
    private var toast: OverlayView? = null

    fun show(text: String, duration: Long) {
        view.removeCallbacks(removal)
        removal.run()
        view.text = text
        toast?.show()
        if (duration > LENGTH_INDEFINITE) view.postDelayed(removal, duration)
    }

    fun show(resId: Int, duration: Long) = show(app.getString(resId), duration)

    private val view: MaterialTextView
        get() = toast?.view as? MaterialTextView
                ?: MaterialTextView(ContextThemeWrapper(app, R.style.Theme_MaterialComponents_Light)).apply {
                    TextViewCompat.setTextAppearance(this, R.style.TextAppearance_MaterialComponents_Body2)
                    val padding = context.resources.getDimensionPixelOffset(R.dimen.view_padding)
                    setPadding(padding, padding, padding, padding)
                    setBackgroundResource(R.drawable.bg_toast)
                    setOnClickListener {
                        it.removeCallbacks(removal)
                        removal.run()
                    }
                }.also {
                    toast = OverlayView(it).apply { setRelativePosition(0, Resolution.absoluteHeight shr 2) }
                }
}