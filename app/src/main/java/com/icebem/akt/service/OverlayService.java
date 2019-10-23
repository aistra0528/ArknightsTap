package com.icebem.akt.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.icebem.akt.R;
import com.icebem.akt.app.CoreApplication;
import com.icebem.akt.object.HRViewer;
import com.icebem.akt.overlay.OverlayView;

public class OverlayService extends Service {
    private OverlayView[] views;

    @Override
    public void onCreate() {
        super.onCreate();
        views = new OverlayView[2];
        views[0] = new OverlayView(this, LayoutInflater.from(this).inflate(R.layout.content_overlay, null), Gravity.END | Gravity.TOP, false, null);
        views[0].getView().findViewById(R.id.action_disconnect).setOnClickListener(view -> {
            stopSelf();
            Toast.makeText(this, R.string.info_overlay_disconnected, Toast.LENGTH_LONG).show();
        });
        views[0].getView().findViewById(R.id.action_collapse).setOnClickListener(view -> {
            views[0].remove();
            views[1].show();
        });
        new HRViewer(views[0].getView());
        views[1] = new OverlayView(this, LayoutInflater.from(this).inflate(R.layout.fab_overlay, null), Gravity.CENTER_HORIZONTAL | Gravity.TOP, true, view -> {
            if (((CoreApplication) getApplication()).isCoreServiceEnabled()) {
                ((CoreApplication) getApplication()).getCoreService().disableSelf();
            } else {
                views[1].remove();
                views[0].show();
            }
        }).show();
        Toast.makeText(this, R.string.info_overlay_connected, Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (OverlayView view : views)
            view.remove();
    }
}