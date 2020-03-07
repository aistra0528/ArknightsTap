package com.icebem.akt.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
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
import com.icebem.akt.overlay.OverlayToast;
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
        if (!manager.resolutionSupported()) {
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
        } else if (Settings.canDrawOverlays(this)) {
            OverlayToast.show(this, R.string.info_gesture_connected, OverlayToast.LENGTH_SHORT);
        } else {
            Toast.makeText(this, R.string.info_gesture_connected, Toast.LENGTH_SHORT).show();
        }
    }

    private void performGestures() {
        SystemClock.sleep(manager.getUpdateTime());
        if (Settings.canDrawOverlays(this))
            startService(new Intent(this, OverlayService.class));
        int process = 0;
        Path path = new Path();
        while (!timerTimeout) {
            switch (process) {
                case 0:
                    path.moveTo(RandomUtil.randomP(manager.getBlueX()), RandomUtil.randomP(manager.getBlueY()));
                    break;
                case 2:
                    path.moveTo(RandomUtil.randomP(manager.getRedX()), RandomUtil.randomP(manager.getRedY()));
                    break;
                default:
                    path.moveTo(RandomUtil.randomP(manager.getGreenX()), RandomUtil.randomP(manager.getGreenY()));
            }
            if (++process > 3) process = 0;
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, RandomUtil.randomP(GESTURE_DURATION)));
            dispatchGesture(builder.build(), null, null);
            SystemClock.sleep(RandomUtil.randomT(manager.getUpdateTime()));
        }
        disableSelf();
    }

    private void launchGame() {
        String packageName = manager.getDefaultPackage();
        Intent intent = packageName == null ? null : getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null)
            startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
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
        if (Settings.canDrawOverlays(this))
            OverlayToast.show(this, manager.resolutionSupported() ? R.string.info_gesture_disconnected : R.string.state_resolution_unsupported, OverlayToast.LENGTH_SHORT);
        else
            Toast.makeText(this, manager.resolutionSupported() ? R.string.info_gesture_disconnected : R.string.state_resolution_unsupported, Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }

    private static final class UIHandler extends Handler {
        private final WeakReference<GestureService> ref;

        private UIHandler(GestureService service) {
            super(Looper.getMainLooper());
            ref = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (ref.get() != null) {
                if (Settings.canDrawOverlays(ref.get()))
                    OverlayToast.show(ref.get(), ref.get().getString(R.string.info_gesture_running, msg.what), OverlayToast.LENGTH_SHORT);
                else
                    Toast.makeText(ref.get(), ref.get().getString(R.string.info_gesture_running, msg.what), Toast.LENGTH_SHORT).show();
            }
        }
    }
}