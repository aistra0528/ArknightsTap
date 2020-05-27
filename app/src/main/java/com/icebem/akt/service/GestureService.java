package com.icebem.akt.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.app.CompatOperations;
import com.icebem.akt.app.GestureActionReceiver;
import com.icebem.akt.app.PreferenceManager;
import com.icebem.akt.overlay.OverlayToast;
import com.icebem.akt.util.RandomUtil;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class GestureService extends AccessibilityService {
    private static final long MINUTE_TIME = 60000;
    private static final String THREAD_GESTURE = "gesture";
    private static final String THREAD_TIMER = "timer";
    private int time;
    private boolean running;
    private Handler handler;
    private Thread gestureThread;
    private Timer timer;
    private PreferenceManager manager;
    private GestureActionReceiver gestureActionReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private static WeakReference<GestureService> currentInstance;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        manager = PreferenceManager.getInstance(this);
        if (CompatOperations.requireOverlayPermission(this) || manager.unsupportedResolution()) {
            disableSelfCompat();
            if (CompatOperations.requireOverlayPermission(this)) {
                OverlayToast.show(this, R.string.state_permission_request, OverlayToast.LENGTH_SHORT);
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } else if (manager.unsupportedResolution())
                OverlayToast.show(this, R.string.state_resolution_unsupported, OverlayToast.LENGTH_SHORT);
            return;
        }
        handler = new Handler(Looper.getMainLooper());
        gestureActionReceiver = new GestureActionReceiver(this::dispatchCurrentAction);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(gestureActionReceiver, new IntentFilter(GestureActionReceiver.ACTION));
        localBroadcastManager.sendBroadcast(new Intent(GestureActionReceiver.ACTION));
    }

    private void dispatchCurrentAction() {
        currentInstance = new WeakReference<>(this);
        if (running)
            pauseAction();
        else
            startAction();
    }

    private void startAction() {
        running = true;
        if (manager.rootMode())
            CompatOperations.checkRootPermission();
        if (manager.launchGame())
            launchGame();
        if (gestureThread == null || !gestureThread.isAlive())
            gestureThread = new Thread(this::performGestures, THREAD_GESTURE);
        if (!gestureThread.isAlive())
            gestureThread.start();
        time = manager.getTimerTime();
        if (time > 0) {
            timer = new Timer(THREAD_TIMER);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (time > 0)
                        handler.post(GestureService.this::showTimeLeft);
                    else
                        pauseAction();
                }
            }, 0, MINUTE_TIME);
        } else
            OverlayToast.show(this, R.string.info_gesture_connected, OverlayToast.LENGTH_SHORT);
    }

    private void pauseAction() {
        if (manager.keepAccessibility())
            stopAction();
        else disableSelfCompat();
    }

    private void stopAction() {
        running = false;
        if (timer != null)
            timer.cancel();
    }

    private void performGestures() {
        SystemClock.sleep(manager.getUpdateTime());
        if (running) {
            startService(new Intent(this, OverlayService.class));
            int process = 0;
            while (running) {
                switch (process) {
                    case 0:
                        CompatOperations.performClick(this, manager.getBlueX(), manager.getBlueY());
                        break;
                    case 2:
                        CompatOperations.performClick(this, manager.getRedX(), manager.getRedY());
                        break;
                    default:
                        CompatOperations.performClick(this, manager.getGreenX(), manager.getGreenY());
                }
                if (++process > 3) process = 0;
                SystemClock.sleep(RandomUtil.randomT(manager.getUpdateTime()));
            }
        }
        handler.post(this::showActionFinished);
    }

    private void showActionFinished() {
        OverlayToast.show(this, R.string.info_gesture_disconnected, OverlayToast.LENGTH_SHORT);
    }

    private void showTimeLeft() {
        OverlayToast.show(this, getString(R.string.info_gesture_running, time--), OverlayToast.LENGTH_SHORT);
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
        stopAction();
        if (localBroadcastManager != null)
            localBroadcastManager.unregisterReceiver(gestureActionReceiver);
        return super.onUnbind(intent);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (running && event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
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
            return currentInstance.get().running;
        return false;
    }

    private void disableSelfCompat() {
        CompatOperations.disableSelf(this, this::stopAction);
    }
}