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
import android.widget.Button;
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
    private ImageView img_status;
    private TextView txt_status, txt_tips;
    private Button btn_timer, btn_service;
    private PreferencesManager manager;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setActionBar(findViewById(R.id.toolbar_main));
        loadViews();
        loadPreferences();
    }

    private void loadViews() {
        img_status = findViewById(R.id.img_service_status);
        txt_status = findViewById(R.id.txt_service_status);
        txt_tips = findViewById(R.id.txt_service_tips);
        btn_timer = findViewById(R.id.btn_timer);
        btn_service = findViewById(R.id.btn_service);
        findViewById(R.id.btn_overlay).setOnClickListener(this::onClick);
        btn_timer.setOnClickListener(this::onClick);
        btn_service.setOnClickListener(this::onClick);
    }

    private void loadPreferences() {
        manager = new PreferencesManager(this);
        if (manager.pointsAdapted()) {
            btn_service.setEnabled(true);
        } else {
            builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.title_resolution_unsupported);
            builder.setMessage(String.format(getString(R.string.msg_resolution_unsupported), ResolutionConfig.getResolution(this)[0], ResolutionConfig.getResolution(this)[1]));
            builder.setPositiveButton(R.string.got_it, null);
            builder.create().show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViews();
    }

    private void updateViews() {
        if (manager.pointsAdapted()) {
            img_status.setImageDrawable(getDrawable(isCoreServiceEnabled() ? R.drawable.ic_service : R.drawable.ic_done));
            txt_status.setText(isCoreServiceEnabled() ? R.string.info_service_running : R.string.info_service_ready);
            txt_tips.setText(isCoreServiceEnabled() ? getString(R.string.tip_service_running) : String.format(getString(R.string.tip_timer_time), manager.getTimerTime()));
            btn_timer.setEnabled(!isCoreServiceEnabled());
            btn_service.setText(isCoreServiceEnabled() ? R.string.action_service_disable : R.string.action_service_enable);
            ((AnimatedVectorDrawable) img_status.getDrawable()).start();
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_overlay:
                if (Settings.canDrawOverlays(this)) {
                    startService(new Intent(this, OverlayService.class));
                } else {
                    builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.title_permission_overlay);
                    builder.setMessage(R.string.msg_permission_overlay);
                    builder.setPositiveButton(R.string.go_to_settings, (dialog, which) -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()))));
                    builder.setNegativeButton(R.string.no_thanks, null);
                    builder.create().show();
                }
                break;
            case R.id.btn_timer:
                timer_position = manager.getTimerPosition();
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.action_timer);
                builder.setSingleChoiceItems(manager.getTimerStrings(this), timer_position, this::onCheck);
                builder.setPositiveButton(android.R.string.ok, this::onCheck);
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.create().show();
                break;
            case R.id.btn_service:
                if (isCoreServiceEnabled()) {
                    ((CoreApplication) getApplication()).getCoreService().disableSelf();
                    updateViews();
                } else {
                    Toast.makeText(this, R.string.info_service_request, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                }
                break;
        }
    }

    private void onCheck(DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
            manager.setTimerTime(timer_position);
            if (!isCoreServiceEnabled())
                txt_tips.setText(String.format(getString(R.string.tip_timer_time), manager.getTimerTime()));
        } else timer_position = which;
    }

    private void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case AlertDialog.BUTTON_POSITIVE:
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.lanzous.com/b943175")));
                break;
            case AlertDialog.BUTTON_NEUTRAL:
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/IcebemAst/ArknightsTap")));
                break;
        }
    }

    private boolean isCoreServiceEnabled() {
        return ((CoreApplication) getApplication()).isCoreServiceEnabled();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        builder = new AlertDialog.Builder(this);
        switch (item.getItemId()) {
            case R.id.action_donate:
                builder.setTitle(R.string.action_donate);
                builder.setMessage(R.string.msg_donate);
                builder.setPositiveButton(R.string.action_donate_alipay, (dialog, which) -> {
                    try {
                        startActivity(Intent.parseUri("intent://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/tsx00051lrjyg1ylmp9h359#Intent;scheme=alipayqr;package=com.eg.android.AlipayGphone;end", Intent.URI_INTENT_SCHEME));
                    } catch (Exception e) {
                        Log.w(getClass().getSimpleName(), e);
                    }
                    Toast.makeText(this, R.string.info_donate_thanks, Toast.LENGTH_LONG).show();
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                break;
            case R.id.action_about:
                builder.setTitle(getString(R.string.app_name) + BuildConfig.VERSION_NAME);
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