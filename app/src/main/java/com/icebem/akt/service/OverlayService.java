package com.icebem.akt.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.icebem.akt.R;
import com.icebem.akt.app.CoreApplication;
import com.icebem.akt.overlay.OverlayView;

public class OverlayService extends Service {
    private OverlayView[] views;

    @Override
    public void onCreate() {
        super.onCreate();
        views = new OverlayView[1];
        views[0] = new OverlayView(this, LayoutInflater.from(this).inflate(R.layout.fab_overlay, null), Gravity.CENTER_HORIZONTAL | Gravity.TOP, true, (view -> {
            if (((CoreApplication) getApplication()).isCoreServiceEnabled()) {
                ((CoreApplication) getApplication()).getCoreService().disableSelf();
            } else {
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
                stopSelf();
            }
        })).show();
        Toast.makeText(this, R.string.info_overlay_showing, Toast.LENGTH_LONG).show();
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