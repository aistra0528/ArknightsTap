package com.icebem.akt.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.icebem.akt.R
import com.icebem.akt.service.OverlayService
import com.icebem.akt.util.IOUtil
import com.icebem.akt.util.RandomUtil
import java.io.File

/**
 * 兼容性 API 管理
 */
object CompatOperations {
    private const val GESTURE_DURATION = 120

    fun requireOverlayPermission(context: Context): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)

    fun createOverlayChannel(service: OverlayService) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = (service.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager)
            nm.createNotificationChannel(NotificationChannel(service.javaClass.simpleName, service.getString(R.string.overlay_label), NotificationManager.IMPORTANCE_LOW))
        }
    }

    fun getDisplayMetrics(context: Context): DisplayMetrics {
        val metric = DisplayMetrics()
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && context.display != null)
            context.display!!.getRealMetrics(metric)
        else*/
        (context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealMetrics(metric)
        return metric
    }

    /**
     * 覆盖安装应用，仅用于强制更新无障碍服务状态
     *
     * @return 是否成功打开打包安装程序
     */
    fun reinstallSelf(context: Context): Boolean {
        Intent(Intent.ACTION_VIEW).apply {
            val apk = File(context.applicationInfo.sourceDir)
            setDataAndType(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, "com.icebem.akt.fileprovider",
                        IOUtil.stream2File(IOUtil.fromFile(apk), "${context.cacheDir}/apk/base.apk"))
            } else {
                Uri.fromFile(apk)
            }, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }.also {
            it.resolveActivity(context.packageManager)?.run {
                context.startActivity(it)
                return true
            }
        }
        return false
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
        } else if (PreferenceManager.getInstance(service).rootMode) {
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

    fun showRootModeDialog(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            AlertDialog.Builder(context).run {
                setCancelable(false)
                setTitle(R.string.root_mode_title)
                setMessage(R.string.root_mode_msg)
                setPositiveButton(R.string.got_it, null)
                create().show()
            }
        }
    }
}