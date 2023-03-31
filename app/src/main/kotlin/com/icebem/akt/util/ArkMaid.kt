package com.icebem.akt.util

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.icebem.akt.ArkApp.Companion.app
import com.icebem.akt.BuildConfig
import com.icebem.akt.R
import com.icebem.akt.service.GestureService
import com.icebem.akt.service.OverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File

/**
 * 杂务管理
 */
object ArkMaid {
    const val URL_ALIPAY_API = "alipays://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/tsx02922ajwj6xekqyd1rbf"
    const val URL_PAYPAL = "https://paypal.me/aistra0528"
    const val URL_QQ_API = "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Fk%3DN_OjFuCOkERq58jO2KoJEDD2a48vzB53"
    const val URL_PROJECT = "https://github.com/aistra0528/ArknightsTap"
    const val URL_COOLAPK = "https://www.coolapk.com/apk/${BuildConfig.APPLICATION_ID}"
    const val URL_WHY_FREE_SOFTWARE = "https://www.gnu.org/philosophy/free-software-even-more-important.html"
    const val URL_RELEASE_LATEST = "https://github.com/aistra0528/ArknightsTap/releases/latest"
    const val URL_RELEASE_LATEST_API = "https://api.github.com/repos/aistra0528/ArknightsTap/releases/latest"
    const val URL_PRTS_WIKI = "https://prts.wiki/"
    const val URL_PENGUIN_STATS = "https://penguin-stats.cn/"

    /**
     * 覆盖安装应用，仅用于强制更新无障碍服务状态
     *
     * @return 是否成功打开打包安装程序
     */
    fun reinstallSelf(context: Context): Boolean {
        Intent(Intent.ACTION_VIEW).apply {
            val source = File(context.applicationInfo.sourceDir)
            setDataAndType(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val target = File("${context.cacheDir}/apk/base.apk")
                ArkIO.copy(source.path, target.path)
                FileProvider.getUriForFile(context, "com.icebem.akt.fileprovider", target)
            } else Uri.fromFile(source), "application/vnd.android.package-archive")
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
     * 执行点击操作
     */
    fun performClick(service: AccessibilityService, x: Int, y: Int) {
        val rX = Random.randomPoint(x)
        val rY = Random.randomPoint(y)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path()
            path.moveTo(rX.toFloat(), rY.toFloat())
            val builder = GestureDescription.Builder()
            builder.addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            service.dispatchGesture(builder.build(), null, null)
        } else execSU("input tap $rX $rY")
    }

    /**
     * 执行 shell 命令
     *
     * @param command 命令内容
     */
    private fun execSU(command: String): Boolean = runCatching {
        Runtime.getRuntime().exec("su").run {
            outputStream.use {
                it.write(command.toByteArray())
            }
            (waitFor() == 0).also {
                destroy()
            }
        }
    }.getOrDefault(false)

    val requireRootPermission: Boolean get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.N && !execSU("whoami")

    /**
     * 停用无障碍服务
     *
     * @param service 要停用的服务
     * @param stopAction API 不支持时的回退方案
     */
    fun disableSelf(service: AccessibilityService, stopAction: () -> Unit) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) service.disableSelf() else stopAction()

    @Suppress("DEPRECATION")
    private fun isServiceRunning(name: String): Boolean {
        val am = ContextCompat.getSystemService(app, ActivityManager::class.java)!!
        for (info in am.getRunningServices(Int.MAX_VALUE)) if (info.service.className == name) return true
        return false
    }

    val isOverlayServiceRunning: Boolean get() = isServiceRunning(OverlayService::class.java.name)
    val isGestureServiceRunning: Boolean get() = isServiceRunning(GestureService::class.java.name)
    val isGestureServiceEnabled: Boolean
        get() = Settings.Secure.getString(app.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).let {
            it != null && it.contains(app.packageName + "/" + GestureService::class.java.name)
        }

    fun startUpdate(view: View) = CoroutineScope(Dispatchers.Main).launch {
        Snackbar.make(view, R.string.version_checking, Snackbar.LENGTH_LONG).show()
        ArkData.requireUpdate()
    }

    fun showUpdateResult(activity: Activity, view: View, result: JSONArray?, updateView: () -> Unit) {
        result ?: return
        val id = result.getInt(0)
        val log = result.getString(1)
        val url = result.getString(2)
        when {
            id == R.string.version_update -> showUpdateDialog(activity, log, url)
            log.isNotEmpty() -> Snackbar.make(view, id, Snackbar.LENGTH_INDEFINITE).setAction(R.string.action_details) { showLogDialog(activity, log) }.show()
            else -> Snackbar.make(view, id, Snackbar.LENGTH_LONG).show()
        }
        updateView()
        ArkData.updateResult.postValue(null)
    }

    private fun showAlertDialog(activity: Activity, title: String, msg: String) {
        MaterialAlertDialogBuilder(activity).run {
            setTitle(title)
            setMessage(msg)
            setPositiveButton(android.R.string.ok, null)
            show()
        }
    }

    private fun showUpdateDialog(activity: Activity, log: String, url: String) {
        MaterialAlertDialogBuilder(activity).run {
            setTitle(R.string.version_update)
            setMessage(log)
            setPositiveButton(R.string.action_update) { _, _ -> startUrl(activity, url) }
            setNegativeButton(R.string.no_thanks, null)
            show()
        }
    }

    fun showLogDialog(activity: Activity, msg: String) = showAlertDialog(activity, activity.getString(R.string.error_occurred), msg)
    fun showRootDialog(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) showAlertDialog(activity, activity.getString(R.string.gesture_label), activity.getString(R.string.root_mode_msg))
    }

    val requireOverlayPermission: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(app)

    fun startUrl(activity: Activity, url: String) = activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    private fun startManageActivity(context: Context, action: String) = context.startActivity(Intent(action).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

    @RequiresApi(Build.VERSION_CODES.M)
    fun startManageOverlay(context: Context) = startManageActivity(context, Settings.ACTION_MANAGE_OVERLAY_PERMISSION)

    fun startManageAccessibility(context: Context) = startManageActivity(context, Settings.ACTION_ACCESSIBILITY_SETTINGS)

    fun requireOverlayService(activity: Activity) {
        if (requireOverlayPermission) MaterialAlertDialogBuilder(activity).run {
            setTitle(R.string.state_permission_request)
            setMessage(R.string.msg_permission_overlay)
            setPositiveButton(R.string.permission_permit) { _, _ -> startManageOverlay(activity) }
            setNegativeButton(R.string.no_thanks, null)
            show()
        } else {
            activity.startService(Intent(activity, OverlayService::class.java))
            activity.finishAndRemoveTask()
        }
    }

    fun launchGame(service: Service) {
        if (!ArkPref.launchGame) return
        ArkPref.defaultPackage?.let {
            service.packageManager.getLaunchIntentForPackage(it)
        }?.also {
            service.startActivity(it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}