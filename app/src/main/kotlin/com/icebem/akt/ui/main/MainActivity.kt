package com.icebem.akt.ui.main

import android.os.Bundle
import android.view.View
import android.view.View.OnLongClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.icebem.akt.R
import com.icebem.akt.databinding.ActivityMainBinding
import com.icebem.akt.service.GestureService
import com.icebem.akt.util.ArkMaid
import com.icebem.akt.util.ArkPref

class MainActivity : AppCompatActivity(), OnLongClickListener, NavController.OnDestinationChangedListener {
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        fab = binding.fab
        fab.setOnClickListener { ArkMaid.requireOverlayService(this) }
        fab.setOnLongClickListener(this)
        navController = findNavController(R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener(this)
        appBarConfiguration = AppBarConfiguration.Builder(R.id.nav_home).build()
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()


    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        if (destination.id != R.id.nav_home) fab.hide() else fab.show()
    }

    override fun onLongClick(v: View?): Boolean {
        if (!ArkPref.isPro) return false
        when {
            ArkMaid.isGestureServiceRunning -> GestureService.toggle()
            ArkMaid.isGestureServiceEnabled -> MaterialAlertDialogBuilder(this).run {
                setTitle(R.string.error_occurred)
                setMessage(R.string.error_accessibility_killed)
                setPositiveButton(R.string.action_reinstall) { _, _ -> ArkMaid.reinstallSelf(this@MainActivity) }
                setNegativeButton(android.R.string.cancel, null)
                show()
            }
            else -> ArkMaid.startManageAccessibility(this)
        }
        return true
    }
}