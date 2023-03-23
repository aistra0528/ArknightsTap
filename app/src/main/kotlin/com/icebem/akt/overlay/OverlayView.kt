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

import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.view.View.OnTouchListener
import androidx.core.content.ContextCompat
import com.icebem.akt.R
import kotlin.math.abs

class OverlayView(val view: View) {
    private var x = 0
    private var y = 0
    private var handled = false
    private var showing = false
    private val touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop
    private val manager: WindowManager = ContextCompat.getSystemService(view.context, WindowManager::class.java)!!

    @Suppress("DEPRECATION", "PrivateResource")
    private val params: WindowManager.LayoutParams = WindowManager.LayoutParams().apply {
        height = WindowManager.LayoutParams.WRAP_CONTENT
        width = height
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        format = PixelFormat.RGBA_8888
        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        windowAnimations = R.style.MaterialAlertDialog_Material3_Animation
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) view.isForceDarkAllowed = false
    }

    fun show(): OverlayView {
        if (!showing) {
            manager.addView(view, params)
            showing = true
        }
        return this
    }

    fun showAfterRemove(showing: OverlayView): OverlayView {
        if (showing !== this) showing.remove()
        return show()
    }

    fun remove() {
        if (showing) {
            manager.removeView(view)
            showing = false
        }
    }

    private fun update() {
        if (showing) manager.updateViewLayout(view, params)
    }

    fun resize(width: Int, height: Int) {
        params.width = width
        params.height = height
        update()
    }

    fun setGravity(gravity: Int) {
        params.gravity = gravity
        update()
    }

    fun setMobilizable(mobilizable: Boolean) {
        view.setOnTouchListener(if (mobilizable) OnTouchListener { view: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handled = false
                    x = event.rawX.toInt()
                    y = event.rawY.toInt()
                    view.postDelayed({ handled = handled || view.performLongClick() }, ViewConfiguration.getLongPressTimeout().toLong())
                }
                MotionEvent.ACTION_UP -> if (!handled) handled = view.performClick()
                MotionEvent.ACTION_MOVE -> {
                    if (!handled) {
                        if (abs(event.rawX.toInt() - x) < touchSlop && abs(event.rawY.toInt() - y) < touchSlop) return@OnTouchListener true
                        handled = true
                    }
                    if (params.gravity == params.gravity or Gravity.END && params.gravity != params.gravity or Gravity.START) params.x -= event.rawX.toInt() - x else params.x += event.rawX.toInt() - x
                    if (params.gravity == params.gravity or Gravity.BOTTOM && params.gravity != params.gravity or Gravity.TOP) params.y -= event.rawY.toInt() - y else params.y += event.rawY.toInt() - y
                    x = event.rawX.toInt()
                    y = event.rawY.toInt()
                    update()
                }
            }
            true
        } else null)
    }

    fun setRelativePosition(x: Int, y: Int) {
        params.x = x
        params.y = y
        update()
    }

    val relativeX: Int get() = params.x
    val relativeY: Int get() = params.y
}