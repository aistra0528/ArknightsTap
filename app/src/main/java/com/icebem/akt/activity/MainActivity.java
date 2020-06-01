package com.icebem.akt.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.icebem.akt.R;
import com.icebem.akt.app.BaseApplication;
import com.icebem.akt.app.CompatOperations;
import com.icebem.akt.app.GestureActionReceiver;
import com.icebem.akt.app.PreferenceManager;
import com.icebem.akt.service.OverlayService;
import com.icebem.akt.util.AppUtil;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView subtitle;
    private FloatingActionButton fab;
    private AppBarConfiguration barConfig;
    private NavController navController;
    private PreferenceManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        manager = PreferenceManager.getInstance(this);
        fab = findViewById(R.id.fab);
        barConfig = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_tools, R.id.nav_settings).setDrawerLayout(findViewById(R.id.drawer_layout)).build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationView navigationView = findViewById(R.id.nav_view);
        subtitle = navigationView.getHeaderView(0).findViewById(R.id.txt_header_subtitle);
        updateSubtitleTime();
        NavigationUI.setupActionBarWithNavController(this, navController, barConfig);
        NavigationUI.setupWithNavController(navigationView, navController);
        navController.addOnDestinationChangedListener((controller, destination, bundle) -> onDestinationChanged(destination));
        fab.setOnClickListener(v -> showOverlay());
        if (manager.isPro())
            fab.setOnLongClickListener(this::startGestureAction);
    }

    public void showOverlay() {
        if (CompatOperations.requireOverlayPermission(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.state_permission_request);
            builder.setMessage(R.string.msg_permission_overlay);
            builder.setPositiveButton(R.string.permission_permit, (dialog, which) -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)));
            builder.setNegativeButton(R.string.no_thanks, null);
            builder.create().show();
        } else
            startService(new Intent(this, OverlayService.class));
    }

    private boolean startGestureAction(View view) {
        if (((BaseApplication) getApplication()).isGestureServiceRunning()) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(GestureActionReceiver.ACTION));
        } else if (((BaseApplication) getApplication()).isGestureServiceEnabled()) {
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(AppUtil.URL_PACKAGE)));
        } else {
            Toast.makeText(this, R.string.info_gesture_request, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }
        return true;
    }

    public void updateSubtitleTime() {
        subtitle.setText(new SimpleDateFormat(AppUtil.DATE_FORMAT, Locale.getDefault()).format(manager.getCheckLastTime()));
    }

    private void onDestinationChanged(NavDestination destination) {
        if (destination.getId() == R.id.nav_home) {
            if (fab.isOrWillBeHidden())
                fab.show();
        } else if (fab.isOrWillBeShown())
            fab.hide();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, barConfig) || super.onSupportNavigateUp();
    }
}