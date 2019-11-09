package com.icebem.akt.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.app.CoreApplication;
import com.icebem.akt.object.PreferencesManager;
import com.icebem.akt.util.RandomUtil;

public class GestureService extends AccessibilityService {
    private static final int GESTURE_DURATION = 120;
    private int time;
    private boolean timerTimeout;
    private PreferencesManager manager;

    @Override
    protected void onServiceConnected() {
        manager = new PreferencesManager(this);
        if (!manager.dataUpdated()) {
            disableSelf();
            return;
        }
        ((CoreApplication) getApplication()).setGestureService(this);
        if (packageInstalled("com.hypergryph.arknights") && !packageInstalled("com.hypergryph.arknights.bilibili")) {
            try {
                startActivity(new Intent().setClassName("com.hypergryph.arknights", "com.u8.sdk.U8UnityContext").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception e) {
                Log.w(getClass().getSimpleName(), e);
            }
        }
        new Thread(this::performGestures, "gesture").start();
        time = manager.getTimerTime();
        if (time > 0) {
            new Thread(() -> {
                try {
                    while (time > 0) {
                        Thread.sleep(60000);
                        time--;
                    }
                } catch (Exception e) {
                    Log.w(getClass().getSimpleName(), e);
                }
                timerTimeout = true;
            }, "timer").start();
        }
        if (Settings.canDrawOverlays(this))
            startService(new Intent(this, OverlayService.class));
        Toast.makeText(this, R.string.info_gesture_connected, Toast.LENGTH_SHORT).show();
        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (BuildConfig.DEBUG)
            Log.d(getClass().getSimpleName(), "onAccessibilityEvent: " + event.toString());
    }

    @Override
    public void onInterrupt() {
        if (BuildConfig.DEBUG)
            Log.d(getClass().getSimpleName(), "onInterrupt");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (timerTimeout)
            performGlobalAction(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN : AccessibilityService.GLOBAL_ACTION_HOME);
        Toast.makeText(this, manager.dataUpdated() ? R.string.info_gesture_disconnected : R.string.state_update_request, Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }

    private void performGestures() {
        try {
            Thread.sleep(manager.getUpdateTime());
            Path path = new Path();
            while (!timerTimeout) {
                GestureDescription.Builder builder = new GestureDescription.Builder();
                path.moveTo(RandomUtil.randomP(manager.getA()), RandomUtil.randomP(manager.getB()));
                builder.addStroke(new GestureDescription.StrokeDescription(path, 0, RandomUtil.randomP(GESTURE_DURATION)));
                path.moveTo(RandomUtil.randomP(manager.getW()), RandomUtil.randomP(manager.getH()));
                builder.addStroke(new GestureDescription.StrokeDescription(path, RandomUtil.randomT(manager.getUpdateTime() / 2), RandomUtil.randomP(GESTURE_DURATION)));
                path.moveTo(RandomUtil.randomP(manager.getX()), RandomUtil.randomP(manager.getY()));
                builder.addStroke(new GestureDescription.StrokeDescription(path, RandomUtil.randomT(manager.getUpdateTime()), RandomUtil.randomP(GESTURE_DURATION)));
                dispatchGesture(builder.build(), null, null);
                Thread.sleep(RandomUtil.randomT(manager.getUpdateTime() * 2));
            }
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), Log.getStackTraceString(e));
            timerTimeout = true;
        }
        disableSelf();
    }

    private boolean packageInstalled(String packageName) {
        try {
            return getPackageManager().getPackageGids(packageName) != null;
        } catch (Exception e) {
            return false;
        }
    }
}