package com.icebem.akt.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.icebem.akt.BuildConfig
import com.icebem.akt.R

object AppUtil {
    const val THREAD_UPDATE = "update"
    const val DATE_FORMAT = "yyyy-MM-dd HH:mm"
    const val URL_ALIPAY_API = "alipays://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/tsx02922ajwj6xekqyd1rbf"
    const val URL_PAYPAL = "https://paypal.me/aistra0528"
    const val URL_QQ_API = "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Fk%3DN_OjFuCOkERq58jO2KoJEDD2a48vzB53"
    const val URL_PROJECT = "https://github.com/aistra0528/ArknightsTap"
    const val URL_COOLAPK = "https://www.coolapk.com/apk/${BuildConfig.APPLICATION_ID}"
    const val URL_FREE_ANDROID = "https://www.gnu.org/philosophy/free-software-even-more-important.html"
    const val URL_RELEASE_LATEST = "https://github.com/aistra0528/ArknightsTap/releases/latest"
    const val URL_RELEASE_LATEST_API = "https://api.github.com/repos/aistra0528/ArknightsTap/releases/latest"
    const val URL_PENGUIN_STATS = "https://penguin-stats.cn/"

    private fun showAlertDialog(context: Context, title: String, msg: String) {
        AlertDialog.Builder(context).run {
            setTitle(title)
            setMessage(msg)
            setPositiveButton(android.R.string.ok, null)
            create().show()
        }
    }

    fun showLogDialog(context: Context, msg: String) = showAlertDialog(context, context.getString(R.string.error_occurred), msg)
}