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
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.icebem.akt.BuildConfig
import com.icebem.akt.R
import com.icebem.akt.app.CompatOperations
import com.icebem.akt.app.GestureActionReceiver
import com.icebem.akt.app.PreferenceManager
import com.icebem.akt.overlay.OverlayToast
import java.lang.ref.WeakReference
import java.util.*

class GestureService : AccessibilityService() {
    companion object {
        private const val WAIT_TIME: Long = 3500
        private const val MINUTE_TIME: Long = 60000
        private const val THREAD_GESTURE = "gesture"
        private const val THREAD_TIMER = "timer"
        private var instance: WeakReference<GestureService?>? = null

        val isGestureRunning: Boolean
            get() = if (instance != null && instance!!.get() != null) instance!!.get()!!.running else false
    }

    private lateinit var handler: Handler
    private lateinit var manager: PreferenceManager
    private lateinit var gestureActionReceiver: GestureActionReceiver
    private var time = 0
    private var running = false
    private var thread: Thread? = null
    private var timer: Timer? = null
    private var localBroadcastManager: LocalBroadcastManager? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        manager = PreferenceManager.getInstance(this)
        if (!manager.isActivated) manager.setActivatedId()
        if (CompatOperations.requireOverlayPermission(this) || manager.unsupportedResolution) {
            disableSelfCompat()
            if (CompatOperations.requireOverlayPermission(this)) {
                Toast.makeText(this, R.string.state_permission_request, Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } else if (manager.unsupportedResolution) OverlayToast.show(this, R.string.state_resolution_unsupported, OverlayToast.LENGTH_SHORT)
            return
        }
        handler = Handler(Looper.getMainLooper())
        gestureActionReceiver = GestureActionReceiver { dispatchCurrentAction() }
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager!!.registerReceiver(gestureActionReceiver, IntentFilter(GestureActionReceiver.ACTION))
        if (manager.noBackground) localBroadcastManager!!.sendBroadcast(Intent(GestureActionReceiver.ACTION))
    }

    private fun dispatchCurrentAction() {
        instance = WeakReference(this)
        if (running) pauseAction() else startAction()
    }

    private fun startAction() {
        running = true
        if (manager.rootMode) CompatOperations.checkRootPermission()
        if (manager.launchGame) launchGame()
        if (thread == null || !thread!!.isAlive) thread = Thread({ performGestures() }, THREAD_GESTURE)
        if (!thread!!.isAlive) thread!!.start()
        time = manager.timerTime
        if (time > 0) {
            timer = Timer(THREAD_TIMER)
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    if (time > 0) {
                        handler.post { showTimeLeft() }
                    } else {
                        pauseAction()
                        CompatOperations.disableKeepScreen(this@GestureService)
                    }
                }
            }, 0, MINUTE_TIME)
        } else OverlayToast.show(this, R.string.info_gesture_connected, OverlayToast.LENGTH_SHORT)
    }

    private fun pauseAction() = if (manager.noBackground) disableSelfCompat() else stopAction()

    private fun stopAction() {
        running = false
        timer?.run { cancel() }
    }

    private fun performGestures() {
        SystemClock.sleep(WAIT_TIME)
        if (running) {
            startService(Intent(this, OverlayService::class.java))
            var process = 0
            while (running) {
                when (process) {
                    0 -> CompatOperations.performClick(this, manager.blueX, manager.blueY)
                    2 -> CompatOperations.performClick(this, manager.redX, manager.redY)
                    else -> CompatOperations.performClick(this, manager.greenX, manager.greenY)
                }
                if (++process > 4) process = 0
                SystemClock.sleep(manager.updateTime)
            }
        }
        handler.post { showActionFinished() }
    }

    private fun showActionFinished() = OverlayToast.show(this, R.string.info_gesture_disconnected, OverlayToast.LENGTH_SHORT)

    private fun showTimeLeft() = OverlayToast.show(this, getString(R.string.info_gesture_running, time--), OverlayToast.LENGTH_SHORT)

    private fun disableSelfCompat() = CompatOperations.disableSelf(this) { stopAction() }

    private fun launchGame() {
        manager.defaultPackage?.let {
            packageManager.getLaunchIntentForPackage(it)?.run {
                startActivity(setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (BuildConfig.DEBUG) Log.d(javaClass.simpleName, "onAccessibilityEvent: $event")
    }

    override fun onInterrupt() {
        if (BuildConfig.DEBUG) Log.d(javaClass.simpleName, "onInterrupt")
    }

    override fun onUnbind(intent: Intent): Boolean {
        stopAction()
        localBroadcastManager?.run { unregisterReceiver(gestureActionReceiver) }
        return super.onUnbind(intent)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (running && event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && manager.volumeControl) {
            pauseAction()
            return true
        }
        return false
    }
}