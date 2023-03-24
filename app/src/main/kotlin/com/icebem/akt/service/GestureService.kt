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
package com.icebem.akt.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.icebem.akt.BuildConfig
import com.icebem.akt.R
import com.icebem.akt.overlay.OverlayToast
import com.icebem.akt.util.ArkMaid
import com.icebem.akt.util.ArkPref
import java.lang.ref.WeakReference
import java.util.*

class GestureService : AccessibilityService(), Observer<Long> {
    companion object {
        private const val WAIT_TIME = 3500L
        private const val MINUTE_TIME = 60000L
        private const val THREAD_GESTURE = "gesture"
        private const val THREAD_TIMER = "timer"
        private var instance: WeakReference<GestureService?>? = null

        val isGestureRunning: Boolean get() = instance?.get()?.running == true
        val now = MutableLiveData<Long>()
        fun toggle() = now.postValue(System.currentTimeMillis())
    }

    private lateinit var handler: Handler
    private var time = 0
    private var running = false
    private var thread: Thread? = null
    private var timer: Timer? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (!ArkPref.isActivated) ArkPref.setActivatedId()
        if (ArkMaid.requireOverlayPermission || ArkPref.unsupportedResolution || ArkMaid.requireRootPermission) {
            disableSelfCompat()
            when {
                ArkMaid.requireOverlayPermission -> ArkMaid.startManageOverlay(this)
                ArkPref.unsupportedResolution -> OverlayToast.show(R.string.state_resolution_unsupported, OverlayToast.LENGTH_LONG)
                ArkMaid.requireRootPermission -> OverlayToast.show(R.string.root_mode_msg, OverlayToast.LENGTH_LONG)
            }
            return
        }
        handler = Handler(Looper.getMainLooper())
        now.observeForever(this)
        if (ArkPref.noBackground) toggle()
    }


    override fun onChanged(now: Long) {
        instance = WeakReference(this)
        if (running) pauseAction() else startAction()
    }

    private fun startAction() {
        running = true
        ArkMaid.launchGame(this)
        if (thread?.isAlive?.not() != false) thread = Thread(::performGestures, THREAD_GESTURE).apply { start() }
        time = ArkPref.timerTime
        if (time > 0) {
            timer = Timer(THREAD_TIMER).apply {
                schedule(object : TimerTask() {
                    override fun run() {
                        if (time > 0) {
                            handler.post(::showTimeLeft)
                        } else {
                            pauseAction()
                            ArkMaid.disableKeepScreen(this@GestureService)
                        }
                    }
                }, 0, MINUTE_TIME)
            }
        } else OverlayToast.show(R.string.info_gesture_connected, OverlayToast.LENGTH_SHORT)
    }

    private fun pauseAction() = if (ArkPref.noBackground) disableSelfCompat() else stopAction()

    private fun stopAction() {
        running = false
        timer?.cancel()
    }

    private fun performGestures() {
        SystemClock.sleep(WAIT_TIME)
        if (running) {
            startService(Intent(this, OverlayService::class.java))
            var process = 0
            while (running) {
                when (process) {
                    0 -> ArkMaid.performClick(this, ArkPref.blueX, ArkPref.blueY)
                    2 -> ArkMaid.performClick(this, ArkPref.redX, ArkPref.redY)
                    else -> if (ArkPref.greenPoint) ArkMaid.performClick(this, ArkPref.greenX, ArkPref.greenY)
                }
                if (++process > 4) process = 0
                SystemClock.sleep(ArkPref.updateTime)
            }
        }
        handler.post(::showActionFinished)
    }

    private fun showActionFinished() = OverlayToast.show(R.string.info_gesture_disconnected, OverlayToast.LENGTH_SHORT)

    private fun showTimeLeft() = OverlayToast.show(getString(R.string.info_gesture_running, time--), OverlayToast.LENGTH_SHORT)

    private fun disableSelfCompat() = ArkMaid.disableSelf(this, ::stopAction)

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (BuildConfig.DEBUG) Log.d(javaClass.simpleName, "onAccessibilityEvent: $event")
    }

    override fun onInterrupt() {
        if (BuildConfig.DEBUG) Log.d(javaClass.simpleName, "onInterrupt")
    }

    override fun onUnbind(intent: Intent): Boolean {
        now.removeObserver(this)
        stopAction()
        return super.onUnbind(intent)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (running && event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && ArkPref.volumeControl) {
            pauseAction()
            return true
        }
        return false
    }
}