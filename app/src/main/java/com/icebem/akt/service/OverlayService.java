package com.icebem.akt.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;

import com.icebem.akt.R;
import com.icebem.akt.app.CoreApplication;
import com.icebem.akt.overlay.OverlayView;

public class OverlayService extends Service {
    private OverlayView[] views;

    @Override
    public void onCreate() {
        super.onCreate();
        ImageView view = new ImageView(this);
        view.setImageResource(R.drawable.ic_overlay);
        views = new OverlayView[1];
        views[0] = new OverlayView(this, view, Gravity.CENTER_HORIZONTAL | Gravity.TOP, true, (v -> {
            if (((CoreApplication) getApplication()).isCoreServiceEnabled()) {
                ((CoreApplication) getApplication()).getCoreService().disableSelf();
            } else {
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
            }
            stopSelf();
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