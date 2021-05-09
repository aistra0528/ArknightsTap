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

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.icebem.akt.R
import com.icebem.akt.adapter.MaterialAdapter
import com.icebem.akt.app.*
import com.icebem.akt.model.HeadhuntCounter
import com.icebem.akt.model.RecruitViewer
import com.icebem.akt.overlay.OverlayToast
import com.icebem.akt.overlay.OverlayView

class OverlayService : Service() {
    companion object {
        private const val COUNT_SPAN = 6
    }

    private var screenSize = 0
    private var mtlEnabled = false
    private var viewer: RecruitViewer? = null
    private lateinit var manager: PreferenceManager
    private lateinit var current: OverlayView
    private lateinit var fab: OverlayView
    private lateinit var menu: OverlayView
    private lateinit var recruit: OverlayView
    private lateinit var counter: OverlayView
    private lateinit var material: OverlayView

    override fun onCreate() {
        super.onCreate()
        setTheme(R.style.AppTheme_Dark)
        screenSize = ResolutionConfig.getAbsoluteHeight(this)
        manager = PreferenceManager.getInstance(this)
        createRecruitView()
        createMaterialView()
        createCounterView()
        createMenuView()
        current = menu
        createFabView()
        if (manager.launchGame) launchGame()
        showTargetView(fab)
        CompatOperations.createOverlayChannel(this)
        startForeground(1, NotificationCompat.Builder(this, javaClass.simpleName).setSmallIcon(R.drawable.ic_akt).setContentTitle(getString(R.string.info_overlay_connected)).build())
    }

    private fun createRecruitView() {
        recruit = OverlayView(this, R.layout.overlay_recruit)
        recruit.setGravity(Gravity.END or Gravity.TOP)
        recruit.resize(screenSize, screenSize)
        try {
            viewer = RecruitViewer(this, recruit.view)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, Log.getStackTraceString(e))
        }
        if (viewer == null) return
        recruit.view.findViewById<View>(R.id.txt_title).setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> viewer!!.rootView.visibility = View.INVISIBLE
                MotionEvent.ACTION_UP -> {
                    resetRecruitView(view)
                    viewer!!.rootView.visibility = View.VISIBLE
                }
                else -> view.performClick()
            }
            true
        }
        recruit.view.findViewById<View>(R.id.action_menu).setOnClickListener { showTargetView(menu) }
        if (manager.multiPackage) {
            recruit.view.findViewById<View>(R.id.action_server).apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    val packages = manager.availablePackages
                    var index = manager.gamePackagePosition
                    if (++index == packages.size) index = 0
                    manager.setGamePackage(packages[index])
                    resetRecruitView(it)
                }
            }
        }
        recruit.view.findViewById<View>(R.id.action_collapse).apply {
            setOnClickListener { showTargetView(fab) }
            setOnLongClickListener { disableSelf() }
        }
    }

    private fun resetRecruitView(view: View) = viewer!!.resetTags(view)

    private fun createCounterView() {
        counter = OverlayView(this, R.layout.overlay_counter)
        counter.setMobilizable(true)
        HeadhuntCounter(manager, counter.view)
        counter.view.findViewById<View>(R.id.action_menu).setOnClickListener { showTargetView(menu) }
        counter.view.findViewById<View>(R.id.action_collapse).apply {
            setOnClickListener { showTargetView(fab) }
            setOnLongClickListener { disableSelf() }
        }
    }

    private fun createMaterialView() {
        material = OverlayView(this, R.layout.overlay_material)
        material.setGravity(Gravity.START or Gravity.TOP)
        material.setMobilizable(true)
        material.resize(screenSize, screenSize)
        material.view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = GridLayoutManager(context, COUNT_SPAN)
            try {
                adapter = MaterialAdapter(manager, COUNT_SPAN)
                mtlEnabled = true
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, Log.getStackTraceString(e))
            }
        }
        if (!mtlEnabled) return
        material.view.findViewById<View>(R.id.action_menu).setOnClickListener { showTargetView(menu) }
        material.view.findViewById<View>(R.id.action_collapse).apply {
            setOnClickListener { showTargetView(fab) }
            setOnLongClickListener { disableSelf() }
        }
    }

    private fun createMenuView() {
        menu = OverlayView(themeWrapper, R.layout.overlay_menu)
        menu.view.apply {
            setBackgroundResource(R.drawable.bg_floating)
            elevation = resources.getDimensionPixelOffset(R.dimen.overlay_elevation).toFloat()
            findViewById<View>(R.id.action_recruit).setOnClickListener { if (viewer == null) OverlayToast.show(context, R.string.error_occurred, OverlayToast.LENGTH_SHORT) else showTargetView(recruit) }
            findViewById<View>(R.id.action_counter).setOnClickListener { showTargetView(counter) }
            findViewById<View>(R.id.action_material).setOnClickListener { if (mtlEnabled) showTargetView(material) else OverlayToast.show(context, R.string.error_occurred, OverlayToast.LENGTH_SHORT) }
            if (manager.isPro) {
                findViewById<View>(R.id.action_gesture).apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        showTargetView(fab)
                        startGestureAction()
                    }
                }
            }
            findViewById<View>(R.id.action_collapse).setOnClickListener { showTargetView(fab) }
            findViewById<View>(R.id.action_disconnect).setOnClickListener { stopSelf() }
        }
    }

    private fun updateMenuView() {
        if (manager.isPro) {
            menu.view.findViewById<MaterialTextView>(R.id.action_gesture_desc).apply {
                if (GestureService.isGestureRunning) {
                    TextViewCompat.setTextAppearance(this, R.style.TextAppearance_MaterialComponents_Button)
                    setTextColor(ContextCompat.getColor(context, R.color.colorError))
                    setText(R.string.action_disconnect)
                } else {
                    TextViewCompat.setTextAppearance(this, R.style.TextAppearance_AppCompat_Small)
                    text = if (manager.timerTime == 0) getString(R.string.info_timer_none) else getString(R.string.info_timer_min, manager.timerTime)
                }
            }
        }
    }

    private fun createFabView() {
        fab = OverlayView(AppCompatImageButton(themeWrapper).apply {
            if (manager.antiBurnIn) alpha = 0.5f
            setImageResource(R.drawable.ic_akt)
            setBackgroundResource(R.drawable.bg_oval)
            setPadding(0, 0, 0, 0)
            elevation = resources.getDimensionPixelOffset(R.dimen.fab_elevation).toFloat()
            val size = resources.getDimensionPixelOffset(R.dimen.fab_mini_size)
            minimumWidth = size
            minimumHeight = size
            setOnClickListener { showTargetView(current) }
            setOnLongClickListener { disableSelf() }
        })
        fab.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP)
        setFabPosition(isPortrait)
        fab.setMobilizable(true)
    }

    private fun launchGame() {
        val packageName = manager.defaultPackage
        val intent = if (packageName == null) null else packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun startGestureAction() {
        when {
            (application as BaseApplication).isGestureServiceRunning -> {
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(GestureActionReceiver.ACTION))
            }
            (application as BaseApplication).isGestureServiceEnabled -> {
                OverlayToast.show(this, R.string.error_accessibility_killed, OverlayToast.LENGTH_INDEFINITE)
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            else -> {
                OverlayToast.show(this, R.string.info_gesture_request, OverlayToast.LENGTH_SHORT)
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }

    private fun showTargetView(target: OverlayView) {
        if (target === fab) target.show(current)
        else {
            if (target === current) {
                fab.remove()
                if (target === recruit) resetRecruitView(fab.view)
            }
            if (target === menu) updateMenuView()
            current = target.show(current)
        }
    }

    private fun disableSelf(): Boolean {
        stopSelf()
        return true
    }

    private fun setFabPosition(isPortrait: Boolean) = fab.setRelativePosition(if (isPortrait) 0 else manager.spriteX, if (isPortrait) 0 else manager.spriteY)

    private val isPortrait: Boolean get() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    override fun onBind(intent: Intent): IBinder? = null

    override fun onConfigurationChanged(cfg: Configuration) {
        super.onConfigurationChanged(cfg)
        if (isPortrait) manager.setSpritePosition(fab.relativeX, fab.relativeY)
        setFabPosition(isPortrait)
        counter.setRelativePosition(0, 0)
        material.setRelativePosition(0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isPortrait) manager.setSpritePosition(fab.relativeX, fab.relativeY)
        fab.remove()
        current.remove()
        if (GestureService.isGestureRunning) startGestureAction() else OverlayToast.show(this, R.string.info_overlay_disconnected, OverlayToast.LENGTH_SHORT)
    }

    private val themeWrapper: Context
        get() = if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) this else ContextThemeWrapper(this, R.style.ThemeOverlay_AppCompat_DayNight)
}