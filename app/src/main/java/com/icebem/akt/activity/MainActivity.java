package com.icebem.akt.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.app.BaseApplication;
import com.icebem.akt.app.PreferenceManager;
import com.icebem.akt.service.OverlayService;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration barConfig;
    private NavController navController;
    private PreferenceManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        barConfig = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_tools, R.id.nav_settings).setDrawerLayout(findViewById(R.id.drawer_layout)).build();
        NavigationUI.setupActionBarWithNavController(this, navController, barConfig);
        NavigationUI.setupWithNavController((NavigationView) findViewById(R.id.nav_view), navController);
        findViewById(R.id.fab).setOnClickListener(this::onClick);
        manager = new PreferenceManager(this);
    }

    private void onClick(View view) {
        if (navController.getCurrentDestination() == null) return;
        switch (navController.getCurrentDestination().getId()) {
            case R.id.nav_home:
                if (manager.isPro()) {
                    if (((BaseApplication) getApplication()).isGestureServiceRunning()) {
                        ((BaseApplication) getApplication()).getGestureService().disableSelf();
                    } else {
                        Toast.makeText(this, R.string.info_gesture_request, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    }
                    break;
                }
            default:
                if (Settings.canDrawOverlays(this)) {
                    startService(new Intent(this, OverlayService.class));
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.state_permission_request);
                    builder.setMessage(R.string.msg_permission_overlay);
                    builder.setPositiveButton(R.string.go_to_settings, (dialog, which) -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()))));
                    builder.setNegativeButton(R.string.no_thanks, null);
                    builder.create().show();
                }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        TextView subtitle = findViewById(R.id.txt_header_subtitle);
        subtitle.setText(BuildConfig.VERSION_NAME);
        return NavigationUI.navigateUp(navController, barConfig) || super.onSupportNavigateUp();
    }
}