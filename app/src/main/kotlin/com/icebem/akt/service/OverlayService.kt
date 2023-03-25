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

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.textview.MaterialTextView
import com.icebem.akt.R
import com.icebem.akt.adapter.MaterialAdapter
import com.icebem.akt.databinding.OverlayCounterBinding
import com.icebem.akt.databinding.OverlayMaterialBinding
import com.icebem.akt.databinding.OverlayMenuBinding
import com.icebem.akt.databinding.OverlayRecruitBinding
import com.icebem.akt.model.HeadhuntCounter
import com.icebem.akt.model.RecruitViewer
import com.icebem.akt.overlay.OverlayToast
import com.icebem.akt.overlay.OverlayView
import com.icebem.akt.util.ArkMaid
import com.icebem.akt.util.ArkPref
import com.icebem.akt.util.Resolution

class OverlayService : Service() {
    private val channelID = javaClass.simpleName

    private var screenSize = 0
    private var mtlEnabled = false
    private var viewer: RecruitViewer? = null
    private lateinit var current: OverlayView
    private lateinit var fab: OverlayView
    private lateinit var menu: OverlayView
    private lateinit var recruit: OverlayView
    private lateinit var counter: OverlayView
    private lateinit var material: OverlayView

    override fun onCreate() {
        super.onCreate()
        setTheme(R.style.Theme_MaterialComponents)
        screenSize = Resolution.absoluteHeight
        createRecruitView()
        createMaterialView()
        createCounterView()
        createMenuView()
        current = menu
        createFabView()
        ArkMaid.launchGame(this)
        showTargetView(fab)
        createNotificationChannel()
        startForeground(100, NotificationCompat.Builder(this, channelID).setSmallIcon(R.drawable.ic_akt).setContentTitle(getString(R.string.info_overlay_connected)).build())
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.overlay_label)
        val importance = NotificationManagerCompat.IMPORTANCE_LOW
        val channel = NotificationChannelCompat.Builder(channelID, importance).setName(name).build()
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createRecruitView() {
        val binding = OverlayRecruitBinding.inflate(LayoutInflater.from(this))
        recruit = OverlayView(binding.root).apply {
            setGravity(Gravity.END or Gravity.TOP)
            resize(screenSize, screenSize)
        }
        runCatching {
            viewer = RecruitViewer(this, binding.fragmentRecruit)
        }.onFailure {
            Log.e(javaClass.simpleName, it.toString())
        }
        viewer ?: return
        binding.txtTitle.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> binding.fragmentRecruit.root.visibility = View.INVISIBLE
                MotionEvent.ACTION_UP -> {
                    viewer?.resetTags(view)
                    binding.fragmentRecruit.root.visibility = View.VISIBLE
                }
            }
            true
        }
        if (ArkPref.multiPackage) binding.actionServer.run {
            visibility = View.VISIBLE
            setOnClickListener { viewer?.toggleServer(it) }
        }
        setBarButtonListeners(binding.actionMenu, binding.actionCollapse)
    }

    private fun createCounterView() {
        val binding = OverlayCounterBinding.inflate(LayoutInflater.from(this))
        counter = OverlayView(binding.root)
        counter.setMobilizable(true)
        HeadhuntCounter(binding)
        setBarButtonListeners(binding.actionMenu, binding.actionCollapse)
    }

    private fun createMaterialView() {
        val binding = OverlayMaterialBinding.inflate(LayoutInflater.from(this))
        material = OverlayView(binding.root).apply {
            setGravity(Gravity.START or Gravity.TOP)
            setMobilizable(true)
            resize(screenSize, screenSize)
        }
        binding.recyclerView.run {
            layoutManager = GridLayoutManager(context, MaterialAdapter.COUNT_SPAN)
            runCatching {
                adapter = MaterialAdapter()
                mtlEnabled = true
            }.onFailure {
                Log.e(javaClass.simpleName, it.toString())
            }
        }
        if (mtlEnabled) setBarButtonListeners(binding.actionMenu, binding.actionCollapse)
    }

    private fun createMenuView() {
        val binding = OverlayMenuBinding.inflate(LayoutInflater.from(themeWrapper))
        menu = OverlayView(binding.root.apply {
            setBackgroundResource(R.drawable.bg_floating)
            elevation = resources.getDimensionPixelOffset(R.dimen.overlay_elevation).toFloat()
        })
        binding.actionRecruit.setOnClickListener { if (viewer == null) OverlayToast.show(R.string.error_occurred, OverlayToast.LENGTH_SHORT) else showTargetView(recruit) }
        binding.actionCounter.setOnClickListener { showTargetView(counter) }
        binding.actionMaterial.setOnClickListener {
            if (mtlEnabled) showTargetView(material) else OverlayToast.show(R.string.error_occurred, OverlayToast.LENGTH_SHORT)
        }
        if (ArkPref.isPro) binding.actionGesture.run {
            visibility = View.VISIBLE
            setOnClickListener {
                showTargetView(fab)
                startGestureAction()
            }
        }
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) binding.imgCollapse.backgroundTintList = ContextCompat.getColorStateList(binding.imgCollapse.context, R.color.color_secondary_night)
        binding.actionCollapse.setOnClickListener { showTargetView(fab) }
        binding.actionDisconnect.setOnClickListener { stopSelf() }
    }

    private fun updateMenuView() {
        if (ArkPref.isPro) {
            menu.view.findViewById<MaterialTextView>(R.id.action_gesture_desc).run {
                if (GestureService.isGestureRunning) {
                    TextViewCompat.setTextAppearance(this, R.style.TextAppearance_MaterialComponents_Button)
                    setTextColor(ContextCompat.getColor(context, R.color.color_warn))
                    setText(R.string.action_disconnect)
                } else {
                    TextViewCompat.setTextAppearance(this, R.style.TextAppearance_MaterialComponents_Caption)
                    text = if (ArkPref.timerTime == 0) getString(R.string.info_timer_none) else getString(R.string.info_timer_min, ArkPref.timerTime)
                }
            }
        }
    }

    private fun createFabView() {
        fab = OverlayView(AppCompatImageButton(themeWrapper).apply {
            if (ArkPref.antiBurnIn) alpha = 0.5f
            setImageResource(R.drawable.ic_akt)
            setBackgroundResource(R.drawable.bg_oval)
            setPadding(0, 0, 0, 0)
            elevation = resources.getDimensionPixelOffset(R.dimen.fab_elevation).toFloat()
            val size = resources.getDimensionPixelOffset(R.dimen.fab_mini_size)
            minimumWidth = size
            minimumHeight = size
            setOnClickListener { showTargetView(current) }
            setOnLongClickListener { disableSelf() }
        }).apply {
            setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP)
            setMobilizable(true)
        }
        setFabPosition(isPortrait)
    }

    private fun startGestureAction() {
        when {
            ArkMaid.isGestureServiceRunning -> GestureService.toggle()
            ArkMaid.isGestureServiceEnabled -> {
                OverlayToast.show(R.string.error_accessibility_killed, OverlayToast.LENGTH_INDEFINITE)
                ArkMaid.reinstallSelf(this)
            }
            else -> {
                OverlayToast.show(R.string.info_gesture_request, OverlayToast.LENGTH_SHORT)
                ArkMaid.startManageAccessibility(this)
            }
        }
    }

    private fun setBarButtonListeners(actionMenu: View, actionCollapse: View) {
        actionMenu.setOnClickListener { showTargetView(menu) }
        actionCollapse.run {
            setOnClickListener { showTargetView(fab) }
            setOnLongClickListener { disableSelf() }
        }
    }

    private fun showTargetView(target: OverlayView) {
        if (target === fab) target.showAfterRemove(current)
        else {
            if (target === current) {
                fab.remove()
                if (target === recruit) viewer?.resetTags(fab.view)
            }
            if (target === menu) updateMenuView()
            current = target.showAfterRemove(current)
        }
    }

    private fun disableSelf(): Boolean {
        stopSelf()
        return true
    }

    private fun setFabPosition(isPortrait: Boolean) = fab.setRelativePosition(if (isPortrait) 0 else ArkPref.spriteX, if (isPortrait) 0 else ArkPref.spriteY)

    private val isPortrait: Boolean get() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    override fun onBind(intent: Intent): IBinder? = null

    override fun onConfigurationChanged(cfg: Configuration) {
        super.onConfigurationChanged(cfg)
        if (isPortrait) ArkPref.setSpritePosition(fab.relativeX, fab.relativeY)
        setFabPosition(isPortrait)
        counter.setRelativePosition(0, 0)
        material.setRelativePosition(0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isPortrait) ArkPref.setSpritePosition(fab.relativeX, fab.relativeY)
        fab.remove()
        current.remove()
        if (GestureService.isGestureRunning) startGestureAction() else OverlayToast.show(R.string.info_overlay_disconnected, OverlayToast.LENGTH_SHORT)
    }

    private val themeWrapper: Context
        get() = if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) this else ContextThemeWrapper(this, R.style.ThemeOverlay_Ark_DayNight)
}