package com.icebem.akt.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.icebem.akt.BuildConfig;

/**
 * 开始戳戳乐™本地广播接收器
 */
public class GestureActionReceiver extends BroadcastReceiver {

    /**
     * Broadcast Action Name
     */
    public static final String ACTION = BuildConfig.APPLICATION_ID + ".START_ACTION";

    private Runnable runnable;

    public GestureActionReceiver(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        runnable.run();
    }
}
