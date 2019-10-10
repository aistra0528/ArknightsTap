package com.icebem.akt.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.app.CoreApplication;
import com.icebem.akt.util.PreferencesManager;
import com.icebem.akt.util.RandomUtil;

public class CoreService extends AccessibilityService {
    private static final int GESTURE_DURATION = 100;
    private boolean timerTimeout;
    private PreferencesManager manager;

    @Override
    protected void onServiceConnected() {
        manager = new PreferencesManager(this);
        if (!manager.pointsAdapted()) {
            disableSelf();
            return;
        }
        ((CoreApplication) getApplication()).setCoreService(this);
        if (packageInstalled("com.hypergryph.arknights") && !packageInstalled("com.hypergryph.arknights.bilibili")) {
            try {
                startActivity(new Intent().setClassName("com.hypergryph.arknights", "com.u8.sdk.U8UnityContext").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception e) {
                Log.w(getClass().getSimpleName(), e);
            }
        }
        new Thread(this::performGestures, "gesture").start();
        new Thread(() -> {
            try {
                Thread.sleep(manager.getTimerTime() * 60000);
            } catch (Exception e) {
                Log.w(getClass().getSimpleName(), e);
            }
            timerTimeout = true;
        }, "timer").start();
        Toast.makeText(this, String.format(getString(R.string.info_service_enabled), manager.getTimerTime()), Toast.LENGTH_LONG).show();
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
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        Toast.makeText(this, manager.pointsAdapted() ? R.string.info_service_disabled : R.string.info_service_update, Toast.LENGTH_LONG).show();
        return super.onUnbind(intent);
    }

    private void performGestures() {
        Path path = new Path();
        while (!timerTimeout) {
            try {
                GestureDescription.Builder builder = new GestureDescription.Builder();
                path.moveTo(RandomUtil.randomP(manager.getA()), RandomUtil.randomP(manager.getB()));
                builder.addStroke(new GestureDescription.StrokeDescription(path, 0, RandomUtil.randomP(GESTURE_DURATION)));
                path.moveTo(RandomUtil.randomP(manager.getW()), RandomUtil.randomP(manager.getH()));
                builder.addStroke(new GestureDescription.StrokeDescription(path, RandomUtil.randomT(manager.getUpdateTime() / 2), RandomUtil.randomP(GESTURE_DURATION)));
                path.moveTo(RandomUtil.randomP(manager.getX()), RandomUtil.randomP(manager.getY()));
                builder.addStroke(new GestureDescription.StrokeDescription(path, RandomUtil.randomT(manager.getUpdateTime()), RandomUtil.randomP(GESTURE_DURATION)));
                dispatchGesture(builder.build(), null, null);
                Thread.sleep(RandomUtil.randomT(manager.getUpdateTime() * 2));
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), Log.getStackTraceString(e));
                timerTimeout = true;
            }
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