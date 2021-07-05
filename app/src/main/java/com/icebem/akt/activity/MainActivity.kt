package com.icebem.akt.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.icebem.akt.R
import com.icebem.akt.app.BaseApplication
import com.icebem.akt.app.CompatOperations
import com.icebem.akt.app.GestureActionReceiver
import com.icebem.akt.app.PreferenceManager
import com.icebem.akt.service.OverlayService
import com.icebem.akt.util.AppUtil
import com.icebem.akt.util.DataUtil
import com.icebem.akt.util.RandomUtil
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    val list by lazy { arrayListOf<Int>() }
    val array by lazy {
        try {
            DataUtil.getSloganData(this)
        } catch (e: Exception) {
            JSONArray()
        }
    }
    lateinit var fab: ExtendedFloatingActionButton private set
    private lateinit var subtitle: TextView
    private lateinit var barConfig: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var manager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        manager = PreferenceManager.getInstance(this)
        barConfig = AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_tools, R.id.nav_settings).setOpenableLayout(findViewById(R.id.drawer_layout)).build()
        fab = findViewById(R.id.fab)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener { _, destination: NavDestination, _ -> onDestinationChanged(destination) }
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        subtitle = navigationView.getHeaderView(0).findViewById(R.id.txt_header_subtitle)
        NavigationUI.setupActionBarWithNavController(this, navController, barConfig)
        NavigationUI.setupWithNavController(navigationView, navController)
        fab.setOnClickListener { showOverlay() }
        if (manager.isPro) fab.setOnLongClickListener { startGestureAction() }
    }

    override fun onStart() {
        super.onStart()
        updateSubtitleTime()
    }

    val slogan: String
        get() {
            return if (array.length() > 0) {
                if (list.isEmpty()) {
                    for (i in 0 until array.length())
                        list.add(i)
                }
                val i = RandomUtil.randomIndex(list.size)
                val obj = array.getJSONObject(list[i])
                list.removeAt(i)
                getString(R.string.operator_slogan, obj.getString("slogan"), obj.getString("name"))
            } else getString(R.string.error_slogan)
        }

    fun showOverlay() {
        if (CompatOperations.requireOverlayPermission(this)) {
            AlertDialog.Builder(this).run {
                setTitle(R.string.state_permission_request)
                setMessage(R.string.msg_permission_overlay)
                setPositiveButton(R.string.permission_permit) { _, _ -> startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)) }
                setNegativeButton(R.string.no_thanks, null)
                create().show()
            }
        } else {
            startService(Intent(this, OverlayService::class.java))
            finishAndRemoveTask()
        }
    }

    private fun startGestureAction(): Boolean {
        when {
            (application as BaseApplication).isGestureServiceRunning -> LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(GestureActionReceiver.ACTION))
            (application as BaseApplication).isGestureServiceEnabled -> {
                AlertDialog.Builder(this).run {
                    setTitle(R.string.error_occurred)
                    setMessage(R.string.error_accessibility_killed)
                    setPositiveButton(R.string.action_reinstall) { _, _ -> CompatOperations.reinstallSelf(context) }
                    setNegativeButton(android.R.string.cancel, null)
                    create().show()
                }
            }
            else -> startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        return true
    }

    fun updateSubtitleTime() {
        subtitle.text = SimpleDateFormat(AppUtil.DATE_FORMAT, Locale.getDefault()).format(manager.checkLastTime)
    }

    private fun onDestinationChanged(destination: NavDestination) {
        if (destination.id == R.id.nav_home) {
            if (!fab.isShown) fab.show()
        } else if (fab.isShown) fab.hide()
    }

    override fun onSupportNavigateUp(): Boolean = NavigationUI.navigateUp(navController, barConfig) || super.onSupportNavigateUp()
}