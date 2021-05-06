package com.icebem.akt.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.icebem.akt.BuildConfig
import com.icebem.akt.R
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

object AppUtil {
    const val THREAD_UPDATE = "update"
    const val MARKET_COOLAPK = "com.coolapk.market"
    const val DATE_FORMAT = "yyyy-MM-dd HH:mm"
    const val URL_ALIPAY_API = "intent://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/tsx02922ajwj6xekqyd1rbf#Intent;scheme=alipayqr;package=com.eg.android.AlipayGphone;end"
    const val URL_PAYPAL = "https://paypal.me/aistra0528"
    const val URL_QQ_API = "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3DN_OjFuCOkERq58jO2KoJEDD2a48vzB53"
    const val URL_MARKET = "market://details?id=" + BuildConfig.APPLICATION_ID
    const val URL_PROJECT = "https://github.com/aistra0528/ArknightsTap"
    const val URL_COOLAPK = "https://www.coolapk.com/apk/" + BuildConfig.APPLICATION_ID
    const val URL_FREE_ANDROID = "https://www.gnu.org/philosophy/free-software-even-more-important.html"
    const val URL_RELEASE_LATEST = "https://github.com/aistra0528/ArknightsTap/releases/latest"
    const val URL_RELEASE_LATEST_API = "https://api.github.com/repos/aistra0528/ArknightsTap/releases/latest"
    private const val URL_RELEASE_DATA = "https://gitee.com/aistra0528/ArknightsTap/raw/master/app/release/output-metadata.json"

    @JvmStatic
    @get:Throws(IOException::class, JSONException::class)
    val isLatestVersion: Boolean
        get() = BuildConfig.VERSION_CODE >= JSONObject(IOUtil.stream2String(IOUtil.fromWeb(URL_RELEASE_DATA))).getJSONArray("elements").getJSONObject(0).getInt("versionCode")

    @JvmStatic
    @Throws(JSONException::class)
    fun getChangelog(json: JSONObject): String = json.getString("name") + System.lineSeparator() + json.getString("body")

    @JvmStatic
    @Throws(JSONException::class)
    fun getDownloadUrl(json: JSONObject): String = json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url")

    @JvmStatic
    fun showAlertDialog(context: Context, title: String, msg: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton(android.R.string.ok, null)
        builder.create().show()
    }

    @JvmStatic
    fun showLogDialog(context: Context, msg: String) = showAlertDialog(context, context.getString(R.string.error_occurred), msg)
}