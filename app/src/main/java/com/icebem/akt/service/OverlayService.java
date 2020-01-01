package com.icebem.akt.service;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import com.icebem.akt.R;
import com.icebem.akt.app.BaseApplication;
import com.icebem.akt.app.PreferenceManager;
import com.icebem.akt.model.RecruitViewer;
import com.icebem.akt.overlay.OverlayView;

import org.json.JSONException;

import java.io.IOException;

public class OverlayService extends Service {
    private OverlayView[] views;

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.AppTheme_Dark);
        PreferenceManager manager = new PreferenceManager(this);
        views = new OverlayView[2];
        views[1] = new OverlayView(this, LayoutInflater.from(this).inflate(R.layout.content_overlay, null));
        views[1].setGravity(Gravity.END | Gravity.TOP);
        views[1].resize(Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels), Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels));
        views[1].getView().findViewById(R.id.action_disconnect).setOnClickListener(view -> stopSelf());
        views[1].getView().findViewById(R.id.action_collapse).setOnClickListener(view -> {
            views[1].remove();
            views[0].show();
        });
        RecruitViewer rv = null;
        try {
            rv = new RecruitViewer(this, (ViewGroup) views[1].getView());
        } catch (IOException | JSONException e) {
            Log.e(getClass().getSimpleName(), Log.getStackTraceString(e));
        }
        RecruitViewer viewer = rv;
        views[0] = new OverlayView(this, LayoutInflater.from(this).inflate(R.layout.fab_overlay, null));
        views[0].setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        views[0].setMobilizable(true);
        views[0].getView().setOnClickListener(view -> {
            if (((BaseApplication) getApplication()).isGestureServiceRunning()) {
                ((BaseApplication) getApplication()).getGestureService().disableSelf();
            } else if (viewer != null) {
                viewer.resetTags();
                views[0].remove();
                views[1].show();
            } else
                stopSelf();
        });
        views[0].getView().setOnLongClickListener(view -> {
            if (manager.isPro()) {
                if (((BaseApplication) getApplication()).isGestureServiceRunning()) {
                    ((BaseApplication) getApplication()).getGestureService().disableSelf();
                } else {
                    Toast.makeText(this, R.string.info_gesture_request, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            } else
                stopSelf();
            return true;
        });
        views[0].show();
        Toast.makeText(this, R.string.info_overlay_connected, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration cfg) {
        super.onConfigurationChanged(cfg);
        for (OverlayView view : views)
            view.onConfigurationChanged(cfg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (OverlayView view : views)
            view.remove();
        Toast.makeText(this, R.string.info_overlay_disconnected, Toast.LENGTH_SHORT).show();
    }
}