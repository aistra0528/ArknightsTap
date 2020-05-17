package com.icebem.akt.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.app.BaseApplication;
import com.icebem.akt.app.PreferenceManager;
import com.icebem.akt.app.GestureActionReceiver;
import com.icebem.akt.overlay.OverlayToast;
import com.icebem.akt.util.RandomUtil;

import java.lang.ref.WeakReference;

public class GestureService extends AccessibilityService {
    private static final int GESTURE_DURATION = 120;
    private static final long LONG_MIN = 60000;
    private static final String THREAD_GESTURE = "gesture";
    private static final String THREAD_TIMER = "timer";
    private int time;
    private boolean timerTimeout = true;
    private PreferenceManager manager;
    private GestureActionReceiver gestureActionReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private static WeakReference<GestureService> currentInstance = new WeakReference<>(null);

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
        gestureActionReceiver = new GestureActionReceiver(this::dispatchCurrentAction);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(gestureActionReceiver, new IntentFilter(GestureActionReceiver.ACTION));
    }

    private void dispatchCurrentAction() {
        currentInstance = new WeakReference<>(this);
        if (timerTimeout) {
            timerTimeout = false;
            startAction();
        } else {
            stopAction();
        }
    }

    private void startAction() {
        new Thread(this::performGestures, THREAD_GESTURE).start();
        time = manager.getTimerTime();
        if (time > 0) {
            Handler handler = new TimerHandler();
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

    private void stopAction() {
        timerTimeout = true;
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
        new Handler(Looper.getMainLooper()).post(this::showActionFinished);
    }

    private void showActionFinished() {
        if (Settings.canDrawOverlays(this))
            OverlayToast.show(this, R.string.info_gesture_disconnected, OverlayToast.LENGTH_SHORT);
        else
            Toast.makeText(this, R.string.info_gesture_disconnected, Toast.LENGTH_SHORT).show();
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
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
            stopAction();
        return super.onKeyEvent(event);
    }

    /**
     * Is gesture action running
     */
    public static boolean isGestureRunning() {
        GestureService service = currentInstance.get();
        if (service != null)
            return !service.timerTimeout;
        return false;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (!manager.resolutionSupported()) {
            if (Settings.canDrawOverlays(this))
                OverlayToast.show(this, R.string.state_resolution_unsupported, OverlayToast.LENGTH_SHORT);
            else
                Toast.makeText(this, R.string.state_resolution_unsupported, Toast.LENGTH_SHORT).show();
        } else if (localBroadcastManager != null)
            localBroadcastManager.unregisterReceiver(gestureActionReceiver);
        return super.onUnbind(intent);
    }

    private static final class TimerHandler extends Handler {
        private TimerHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (currentInstance.get() != null) {
                if (Settings.canDrawOverlays(currentInstance.get()))
                    OverlayToast.show(currentInstance.get(), currentInstance.get().getString(R.string.info_gesture_running, msg.what), OverlayToast.LENGTH_SHORT);
                else
                    Toast.makeText(currentInstance.get(), currentInstance.get().getString(R.string.info_gesture_running, msg.what), Toast.LENGTH_SHORT).show();
            }
        }
    }
}