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
import android.graphics.PixelFormat
import android.os.Build
import android.view.*
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import kotlin.math.abs

class OverlayView(val view: View) {
    companion object {
        private const val GRAVITY_LEFT = 3
        private const val GRAVITY_RIGHT = 5
    }

    private var x = 0
    private var y = 0
    private var handled = false
    private var showing = false
    private val touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop
    private val manager: WindowManager = view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val params: WindowManager.LayoutParams = WindowManager.LayoutParams().apply {
        height = WindowManager.LayoutParams.WRAP_CONTENT
        width = height
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        format = PixelFormat.RGBA_8888
        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        windowAnimations = android.R.style.Animation_Toast
    }

    constructor(context: Context, resId: Int) : this(LayoutInflater.from(context).inflate(resId, FrameLayout(context)))

    fun show(): OverlayView {
        if (!showing) {
            manager.addView(view, params)
            showing = true
        }
        return this
    }

    fun show(current: OverlayView): OverlayView {
        if (current !== this) current.remove()
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
                    if (params.gravity == params.gravity or GRAVITY_RIGHT && params.gravity != params.gravity or GRAVITY_LEFT) params.x -= event.rawX.toInt() - x else params.x += event.rawX.toInt() - x
                    if (params.gravity == params.gravity or Gravity.BOTTOM && params.gravity != params.gravity or Gravity.TOP) params.y -= event.rawY.toInt() - y else params.y += event.rawY.toInt() - y
                    x = event.rawX.toInt()
                    y = event.rawY.toInt()
                    update()
                }
                else -> return@OnTouchListener view.performClick()
            }
            return@OnTouchListener true
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