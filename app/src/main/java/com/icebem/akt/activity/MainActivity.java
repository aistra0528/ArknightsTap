package com.icebem.akt.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.app.BaseApplication;
import com.icebem.akt.app.PreferenceManager;
import com.icebem.akt.service.OverlayService;
import com.icebem.akt.util.AppUtil;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton fab;
    private AppBarConfiguration barConfig;
    private NavController navController;
    private PreferenceManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        manager = new PreferenceManager(this);
        fab = findViewById(R.id.fab);
        barConfig = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_tools, R.id.nav_settings).setDrawerLayout(findViewById(R.id.drawer_layout)).build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationView navigationView = findViewById(R.id.nav_view);
        TextView subtitle = navigationView.getHeaderView(0).findViewById(R.id.txt_header_subtitle);
        subtitle.setText(BuildConfig.VERSION_NAME);
        NavigationUI.setupActionBarWithNavController(this, navController, barConfig);
        NavigationUI.setupWithNavController(navigationView, navController);
        navController.addOnDestinationChangedListener((controller, destination, bundle) -> onDestinationChanged(destination));
        fab.setOnClickListener(this::onClick);
        fab.setOnLongClickListener(this::onLongClick);
    }

    private void onClick(View view) {
        if (view == null || navController.getCurrentDestination() == null) return;
        if (navController.getCurrentDestination().getId() == R.id.nav_home) {
            if (manager.isPro()) {
                if (((BaseApplication) getApplication()).isGestureServiceRunning()) {
                    ((BaseApplication) getApplication()).getGestureService().disableSelf();
                } else {
                    Toast.makeText(this, R.string.info_gesture_request, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                }
                return;
            }
        }
        showOverlay();
    }

    private boolean onLongClick(View view) {
        if (view != null && manager.isPro() && navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.nav_home) {
            showOverlay();
            if (((BaseApplication) getApplication()).isOverlayServiceRunning() && fab.isOrWillBeShown() && navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.nav_home || !manager.isPro())
                fab.post(fab::hide);
            return true;
        }
        return false;
    }

    public void showOverlay() {
        if (Settings.canDrawOverlays(this)) {
            startService(new Intent(this, OverlayService.class));
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.state_permission_request);
            builder.setMessage(R.string.msg_permission_overlay);
            builder.setPositiveButton(R.string.go_to_settings, (dialog, which) -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(AppUtil.URL_SETTINGS))));
            builder.setNegativeButton(R.string.no_thanks, null);
            builder.create().show();
        }
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