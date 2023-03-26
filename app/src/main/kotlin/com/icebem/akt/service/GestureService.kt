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
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.icebem.akt.BuildConfig
import com.icebem.akt.R
import com.icebem.akt.overlay.OverlayToast
import com.icebem.akt.util.ArkData
import com.icebem.akt.util.ArkMaid
import com.icebem.akt.util.ArkPref
import com.icebem.akt.util.Random
import kotlinx.coroutines.*
import java.lang.ref.WeakReference

class GestureService : AccessibilityService(), Observer<Boolean> {
    companion object {
        private const val WAIT_TIME = 3500L
        private const val MINUTE_TIME = 60000L
        private var instance: WeakReference<GestureService?>? = null

        val isGestureRunning: Boolean get() = instance?.get()?.running == true
        val action = MutableLiveData<Boolean>()
        fun toggle() = action.postValue(true)
    }

    private var running = false
    private var gesture: Job? = null
    private var timer: Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (!ArkPref.isActivated) ArkPref.setActivatedId()
        if (ArkMaid.requireOverlayPermission || ArkPref.unsupportedResolution || ArkMaid.requireRootPermission) {
            ArkMaid.disableSelf(this, ::stopAction)
            when {
                ArkMaid.requireOverlayPermission -> ArkMaid.startManageOverlay(this)
                ArkPref.unsupportedResolution -> OverlayToast.show(R.string.state_resolution_unsupported, OverlayToast.LENGTH_LONG)
                ArkMaid.requireRootPermission -> OverlayToast.show(R.string.root_mode_msg, OverlayToast.LENGTH_LONG)
            }
            return
        }
        action.observeForever(this)
        if (ArkPref.noBackground) toggle()
    }

    override fun onChanged(value: Boolean) {
        if (!value) return
        instance = WeakReference(this)
        if (running) pauseAction() else startAction()
        action.postValue(false)
    }

    private fun startAction() {
        running = true
        gesture?.cancel()
        timer?.cancel()
        startService(Intent(this, OverlayService::class.java))
        CoroutineScope(Dispatchers.Default).launch {
            gesture = launch {
                delay(WAIT_TIME)
                if (ArkData.hasGestureData) performCustomizedGestures()
                else performGestures()
            }
            var time = ArkPref.timerTime
            if (time > 0) {
                timer = launch {
                    while (time > 0) {
                        showTimeLeft(time--)
                        delay(MINUTE_TIME)
                    }
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    pauseAction()
                }
            } else showGestureTip(R.string.info_gesture_connected)
        }
    }

    private fun pauseAction() {
        if (ArkPref.noBackground) ArkMaid.disableSelf(this, ::stopAction) else stopAction()
    }

    private fun stopAction() {
        running = false
        gesture?.cancel()
        timer?.cancel()
        runCatching { OverlayToast.show(R.string.info_gesture_disconnected, OverlayToast.LENGTH_SHORT) }
    }

    private suspend fun performGestures() {
        var process = 0
        while (true) {
            when (process++) {
                0 -> ArkMaid.performClick(this, ArkPref.blueX, ArkPref.blueY)
                1 -> if (ArkPref.greenPoint) ArkMaid.performClick(this, ArkPref.greenX, ArkPref.greenY)
                2 -> ArkMaid.performClick(this, ArkPref.redX, ArkPref.redY)
                3 -> if (ArkPref.greenPoint) ArkMaid.performClick(this, ArkPref.greenX, ArkPref.greenY)
                4 -> if (ArkPref.greenPoint) ArkMaid.performClick(this, ArkPref.greenX, ArkPref.greenY)
            }
            if (process > 4) process = 0
            delay(Random.randomTime(ArkPref.updateTime))
        }
    }

    private suspend fun performCustomizedGestures() {
        val array = ArkData.getGestureData()
        var process = 0
        while (true) {
            val obj = array.getJSONObject(process)
            when (obj.optString(ArkData.KEY_NAME)) {
                ArkData.KEY_TAP ->
                    ArkMaid.performClick(this, obj.optInt(ArkData.KEY_X), obj.optInt(ArkData.KEY_Y))
            }
            if (++process == array.length()) process = 0
            delay(Random.randomTime(if (obj.optInt(ArkData.KEY_DELAY) > 0) obj.optInt(ArkData.KEY_DELAY) else ArkPref.updateTime))
        }
    }

    private suspend fun showGestureTip(resId: Int) = withContext(Dispatchers.Main) { OverlayToast.show(resId, OverlayToast.LENGTH_SHORT) }

    private suspend fun showTimeLeft(time: Int) = withContext(Dispatchers.Main) { OverlayToast.show(getString(R.string.info_gesture_running, time), OverlayToast.LENGTH_SHORT) }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (BuildConfig.DEBUG) Log.d(javaClass.simpleName, "onAccessibilityEvent: $event")
    }

    override fun onInterrupt() {
        if (BuildConfig.DEBUG) Log.d(javaClass.simpleName, "onInterrupt")
    }

    override fun onUnbind(intent: Intent): Boolean {
        action.removeObserver(this)
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