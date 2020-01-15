package com.icebem.akt.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.app.BaseApplication;
import com.icebem.akt.app.PreferenceManager;
import com.icebem.akt.util.RandomUtil;

import java.lang.ref.WeakReference;

public class GestureService extends AccessibilityService {
    private static final int GESTURE_DURATION = 120;
    private static final long LONG_MIN = 60000;
    private static final String THREAD_GESTURE = "gesture";
    private static final String THREAD_TIMER = "timer";
    private int time;
    private boolean timerTimeout;
    private PreferenceManager manager;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        manager = new PreferenceManager(this);
        if (!manager.dataUpdated()) {
            disableSelf();
            return;
        }
        ((BaseApplication) getApplication()).setGestureService(this);
        if (manager.launchGame())
            launchGame();
        new Thread(this::performGestures, THREAD_GESTURE).start();
        time = manager.getTimerTime();
        if (time > 0) {
            Handler handler = new UIHandler(this);
            new Thread(() -> {
                while (!timerTimeout && time > 0) {
                    handler.sendEmptyMessage(time);
                    SystemClock.sleep(LONG_MIN);
                    time--;
                }
                timerTimeout = true;
            }, THREAD_TIMER).start();
        } else {
            Toast.makeText(this, R.string.info_gesture_connected, Toast.LENGTH_SHORT).show();
        }
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
        else
            timerTimeout = true;
        Toast.makeText(this, manager.dataUpdated() ? R.string.info_gesture_disconnected : R.string.state_resolution_unsupported, Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }

    private void performGestures() {
        SystemClock.sleep(manager.getUpdateTime());
        if (Settings.canDrawOverlays(this))
            startService(new Intent(this, OverlayService.class));
        Path path = new Path();
        while (!timerTimeout) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            path.moveTo(RandomUtil.randomP(manager.getA()), RandomUtil.randomP(manager.getB()));
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, RandomUtil.randomP(GESTURE_DURATION)));
            path.moveTo(RandomUtil.randomP(manager.getX()), RandomUtil.randomP(manager.getY()));
            builder.addStroke(new GestureDescription.StrokeDescription(path, RandomUtil.randomT(manager.getUpdateTime()), RandomUtil.randomP(GESTURE_DURATION)));
            path.moveTo(RandomUtil.randomP(manager.getW()), RandomUtil.randomP(manager.getH()));
            builder.addStroke(new GestureDescription.StrokeDescription(path, RandomUtil.randomT(manager.getUpdateTime() / 2), RandomUtil.randomP(GESTURE_DURATION)));
            builder.addStroke(new GestureDescription.StrokeDescription(path, RandomUtil.randomT(manager.getUpdateTime() / 2 * 3), RandomUtil.randomP(GESTURE_DURATION)));
            dispatchGesture(builder.build(), null, null);
            SystemClock.sleep(RandomUtil.randomT(manager.getUpdateTime() * 2));
        }
        disableSelf();
    }

    private void launchGame() {
        Intent intent = manager.getLaunchPackage() == null ? null : getPackageManager().getLaunchIntentForPackage(manager.getLaunchPackage());
        if (intent == null) {
            String packageName = manager.getDefaultLaunchPackage();
            if (packageName != null)
                intent = getPackageManager().getLaunchIntentForPackage(packageName);
        }
        if (intent != null)
            startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private static final class UIHandler extends Handler {
        private final WeakReference<GestureService> ref;

        private UIHandler(GestureService service) {
            ref = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            GestureService service = ref.get();
            if (service != null)
                Toast.makeText(service, service.getString(R.string.info_gesture_running, msg.what), Toast.LENGTH_SHORT).show();
        }
    }
}