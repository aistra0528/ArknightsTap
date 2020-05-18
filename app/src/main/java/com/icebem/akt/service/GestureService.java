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
    private static WeakReference<GestureService> currentInstance;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        manager = new PreferenceManager(this);
        if (!Settings.canDrawOverlays(this) || manager.unsupportedResolution()) {
            disableSelf();
            return;
        }
        ((BaseApplication) getApplication()).setGestureService(this);
        gestureActionReceiver = new GestureActionReceiver(this::dispatchCurrentAction);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(gestureActionReceiver, new IntentFilter(GestureActionReceiver.ACTION));
        localBroadcastManager.sendBroadcast(new Intent(GestureActionReceiver.ACTION));
    }

    private void dispatchCurrentAction() {
        currentInstance = new WeakReference<>(this);
        if (timerTimeout)
            startAction();
        else
            pauseAction();
    }

    private void startAction() {
        timerTimeout = false;
        if (manager.launchGame())
            launchGame();
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
        } else
            OverlayToast.show(this, R.string.info_gesture_connected, OverlayToast.LENGTH_SHORT);
    }

    private void pauseAction() {
        if (manager.keepAccessibility())
            stopAction();
        else disableSelf();
    }

    private void stopAction() {
        timerTimeout = true;
    }

    private void performGestures() {
        SystemClock.sleep(manager.getUpdateTime());
        if (!timerTimeout) {
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
        }
        new Handler(Looper.getMainLooper()).post(this::showActionFinished);
    }

    private void showActionFinished() {
        OverlayToast.show(this, R.string.info_gesture_disconnected, OverlayToast.LENGTH_SHORT);
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
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN && !timerTimeout) {
            pauseAction();
            return true;
        }
        return false;
    }

    /**
     * Is gesture action running
     */
    public static boolean isGestureRunning() {
        if (currentInstance != null && currentInstance.get() != null)
            return !currentInstance.get().timerTimeout;
        return false;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopAction();
        if (localBroadcastManager != null)
            localBroadcastManager.unregisterReceiver(gestureActionReceiver);
        else if (!Settings.canDrawOverlays(this))
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        else if (manager.unsupportedResolution())
            OverlayToast.show(this, R.string.state_resolution_unsupported, OverlayToast.LENGTH_SHORT);
        return super.onUnbind(intent);
    }

    private static final class TimerHandler extends Handler {
        private TimerHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (currentInstance != null && currentInstance.get() != null)
                OverlayToast.show(currentInstance.get(), currentInstance.get().getString(R.string.info_gesture_running, msg.what), OverlayToast.LENGTH_SHORT);
        }
    }
}