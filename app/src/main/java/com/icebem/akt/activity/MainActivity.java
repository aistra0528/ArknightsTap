package com.icebem.akt.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.app.CoreApplication;
import com.icebem.akt.service.OverlayService;
import com.icebem.akt.object.PreferencesManager;
import com.icebem.akt.util.ResolutionConfig;

public class MainActivity extends Activity {
    private int timer_position;
    private ImageView img_state;
    private PreferencesManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setActionBar(findViewById(R.id.toolbar_main));
        loadViews();
    }

    private void loadViews() {
        img_state = findViewById(R.id.img_service_state);
        findViewById(R.id.btn_overlay).setOnClickListener(this::onClick);
        manager = new PreferencesManager(this);
        if (manager.isPro()) {
            if (manager.dataUpdated()) {
                ImageButton fab = findViewById(R.id.fab_service);
                fab.setOnClickListener(this::onClick);
                fab.setVisibility(View.VISIBLE);
            } else {
                img_state.setImageDrawable(getDrawable(R.drawable.ic_update_black_24dp));
                TextView state = findViewById(R.id.txt_service_state);
                state.setText(R.string.state_update_request);
                int[] res = ResolutionConfig.getResolution(this);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.info_resolution_unsupported);
                builder.setMessage(String.format(getString(R.string.msg_resolution_unsupported), res[0], res[1]));
                builder.setPositiveButton(R.string.got_it, null);
                builder.create().show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (img_state.getDrawable() instanceof AnimatedVectorDrawable)
            ((AnimatedVectorDrawable) img_state.getDrawable()).start();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_overlay:
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
                break;
            case R.id.fab_service:
                if (((CoreApplication) getApplication()).isGestureServiceRunning()) {
                    ((CoreApplication) getApplication()).getGestureService().disableSelf();
                } else {
                    Toast.makeText(this, R.string.info_gesture_request, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                }
                break;
        }
    }

    private void onCheck(DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_POSITIVE)
            manager.setTimerTime(timer_position);
        else
            timer_position = which;
    }

    private void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case AlertDialog.BUTTON_POSITIVE:
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/IcebemAst/ArknightsTap/releases/latest")));
                break;
            case AlertDialog.BUTTON_NEUTRAL:
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/IcebemAst/ArknightsTap")));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (!manager.isPro())
            menu.findItem(R.id.action_timer).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (item.getItemId()) {
            case R.id.action_timer:
                timer_position = manager.getTimerPosition();
                builder.setTitle(R.string.action_timer);
                builder.setSingleChoiceItems(manager.getTimerStrings(this), timer_position, this::onCheck);
                builder.setPositiveButton(android.R.string.ok, this::onCheck);
                builder.setNegativeButton(android.R.string.cancel, null);
                break;
            case R.id.action_donate:
                builder.setTitle(R.string.action_donate);
                builder.setMessage(R.string.msg_donate);
                builder.setNeutralButton(R.string.action_donate_alipay, (dialog, which) -> {
                    try {
                        startActivity(Intent.parseUri("intent://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/tsx02922ajwj6xekqyd1rbf#Intent;scheme=alipayqr;package=com.eg.android.AlipayGphone;end", Intent.URI_INTENT_SCHEME));
                        Toast.makeText(this, R.string.info_donate_thanks, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.w(getClass().getSimpleName(), e);
                    }
                });
                builder.setPositiveButton(R.string.no_way, null);
                builder.setNegativeButton(android.R.string.cancel, null);
                break;
            case R.id.action_about:
                builder.setTitle(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
                builder.setMessage(R.string.msg_about);
                builder.setPositiveButton(R.string.action_update, this::onClick);
                builder.setNegativeButton(R.string.got_it, null);
                builder.setNeutralButton(R.string.action_project, this::onClick);
                break;
        }
        builder.create().show();
        return super.onOptionsItemSelected(item);
    }
}