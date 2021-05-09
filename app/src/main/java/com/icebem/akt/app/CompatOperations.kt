package com.icebem.akt.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import com.icebem.akt.util.RandomUtil

/**
 * 兼容性 API 管理
 */
object CompatOperations {
    private const val GESTURE_DURATION = 120

    fun requireOverlayPermission(context: Context): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)

    fun getDisplayMetrics(context: Context): DisplayMetrics {
        val metric = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && context.display != null)
            context.display!!.getRealMetrics(metric)
        else
            (context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealMetrics(metric)
        return metric
    }

    /**
     * 停用无障碍服务
     *
     * @param service        要停用的服务
     * @param fallbackAction API 不支持时的回退方案
     */
    fun disableSelf(service: AccessibilityService, fallbackAction: Runnable) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) service.disableSelf() else fallbackAction.run()

    fun disableKeepScreen(service: AccessibilityService) {
        service.performGlobalAction(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN else AccessibilityService.GLOBAL_ACTION_HOME)
    }

    /**
     * 执行点击操作
     */
    fun performClick(service: AccessibilityService, x: Int, y: Int) {
        val rX = RandomUtil.randomPoint(x)
        val rY = RandomUtil.randomPoint(y)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path()
            path.moveTo(rX.toFloat(), rY.toFloat())
            val builder = GestureDescription.Builder()
            builder.addStroke(GestureDescription.StrokeDescription(path, 0, RandomUtil.randomTime(GESTURE_DURATION)))
            service.dispatchGesture(builder.build(), null, null)
        } else if (PreferenceManager.getInstance(service).rootMode()) {
            executeCommand(String.format("input tap %s %s", rX, rY))
        }
    }

    /**
     * 执行 shell 命令
     *
     * @param command 命令内容
     */
    private fun executeCommand(command: String) {
        try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", command))
        } catch (e: Exception) {
        }
    }

    fun checkRootPermission() = executeCommand("su")
}