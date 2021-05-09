package com.icebem.akt.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.icebem.akt.BuildConfig

/**
 * 戳戳乐™开始/结束行动本地广播接收器
 */
class GestureActionReceiver(private val runnable: Runnable) : BroadcastReceiver() {
    companion object {
        const val ACTION = "${BuildConfig.APPLICATION_ID}.START_ACTION"
    }

    override fun onReceive(context: Context, intent: Intent) = runnable.run()
}