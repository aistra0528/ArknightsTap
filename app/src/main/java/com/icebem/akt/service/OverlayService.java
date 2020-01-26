package com.icebem.akt.service;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.view.ContextThemeWrapper;

import com.icebem.akt.R;
import com.icebem.akt.app.BaseApplication;
import com.icebem.akt.model.RecruitViewer;
import com.icebem.akt.overlay.OverlayToast;
import com.icebem.akt.overlay.OverlayView;

public class OverlayService extends Service {
    private OverlayView[] views;

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.AppTheme_Dark);
        views = new OverlayView[2];
        views[1] = new OverlayView(this, LayoutInflater.from(this).inflate(R.layout.content_overlay, new FrameLayout(this)));
        views[1].setGravity(Gravity.END | Gravity.TOP);
        views[1].setMobilizable(true);
        int size = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
        views[1].resize(size, size);
        ImageButton disconnect = views[1].getView().findViewById(R.id.action_disconnect);
        disconnect.setOnClickListener(view -> {
            views[1].remove();
            views[0].show();
        });
        disconnect.setOnLongClickListener(view -> {
            stopSelf();
            return true;
        });
        RecruitViewer rv = null;
        try {
            rv = new RecruitViewer(this, views[1].getView());
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), Log.getStackTraceString(e));
        }
        RecruitViewer viewer = rv;
        views[1].getView().findViewById(R.id.action_server).setOnClickListener(view -> {
            if (viewer == null) return;
            int index = viewer.getManager().getGamePackagePosition();
            String[] values = getResources().getStringArray(R.array.game_server_values);
            if (++index == values.length) index = 0;
            viewer.getManager().setGamePackage(values[index]);
            viewer.resetTags(view);
            OverlayToast.show(this, getResources().getStringArray(R.array.game_server_entries)[index], OverlayToast.LENGTH_SHORT);
        });
        ImageButton fab = new ImageButton(new ContextThemeWrapper(this, R.style.ThemeOverlay_AppCompat_Light));
        fab.setImageResource(R.drawable.ic_fab_akt);
        fab.setImageTintList(getColorStateList(android.R.color.black));
        fab.setBackgroundResource(R.drawable.bg_fab_mini);
        fab.setPadding(0, 0, 0, 0);
        fab.setElevation(getResources().getDimensionPixelOffset(R.dimen.fab_elevation));
        fab.setMinimumWidth(getResources().getDimensionPixelOffset(R.dimen.fab_mini_size));
        fab.setMinimumHeight(getResources().getDimensionPixelOffset(R.dimen.fab_mini_size));
        views[0] = new OverlayView(this, fab);
        views[0].setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        views[0].setMobilizable(true);
        views[0].getView().setOnClickListener(view -> {
            if (((BaseApplication) getApplication()).isGestureServiceRunning()) {
                ((BaseApplication) getApplication()).getGestureService().disableSelf();
            } else if (viewer != null) {
                viewer.resetTags(null);
                views[0].remove();
                views[1].show();
            } else
                stopSelf();
        });
        views[0].getView().setOnLongClickListener(view -> {
            if (viewer != null && viewer.getManager().isPro()) {
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
        if (!((BaseApplication) getApplication()).isGestureServiceRunning())
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