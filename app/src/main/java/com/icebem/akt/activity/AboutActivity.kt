package com.icebem.akt.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.icebem.akt.BuildConfig
import com.icebem.akt.R
import com.icebem.akt.app.PreferenceManager
import com.icebem.akt.util.AppUtil
import com.icebem.akt.util.DataUtil
import com.icebem.akt.util.IOUtil
import org.json.JSONObject
import java.io.IOException

class AboutActivity : AppCompatActivity() {
    companion object {
        private const val TEXT_SPEED = 50L
    }

    private var i = 0
    private lateinit var typeDesc: TextView
    private lateinit var thanksDesc: TextView
    private lateinit var versionContainer: View
    private lateinit var manager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initView()
    }

    private fun initView() {
        findViewById<View>(R.id.fab).setOnClickListener { onClick(it) }
        findViewById<View>(R.id.container_comment).setOnClickListener { onClick(it) }
        findViewById<View>(R.id.container_discuss).setOnClickListener { onClick(it) }
        findViewById<View>(R.id.container_project).setOnClickListener { onClick(it) }
        versionContainer = findViewById(R.id.container_version_state)
        versionContainer.setOnClickListener { onClick(it) }
        versionContainer.setOnLongClickListener { onLongClick() }
        findViewById<View>(R.id.container_version_type).setOnClickListener { onClick(it) }
        findViewById<View>(R.id.container_special_thanks).setOnClickListener { onClick(it) }
        findViewById<View>(R.id.free_android).setOnClickListener { onClick(it) }
        findViewById<TextView>(R.id.txt_version_state_desc).text = BuildConfig.VERSION_NAME
        typeDesc = findViewById(R.id.txt_version_type_desc)
        thanksDesc = findViewById(R.id.special_thanks_desc)
        manager = PreferenceManager.getInstance(this)
        typeDesc.setText(if (manager.isPro) R.string.version_type_pro else R.string.version_type_lite)
    }

    private fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> {
                AlertDialog.Builder(this).apply {
                    setTitle(R.string.action_support)
                    setMessage(R.string.msg_donate)
                    setPositiveButton(R.string.action_donate) { _, _ -> onDonate() }
                    setNeutralButton(R.string.action_share) { _, _ ->
                        (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), AppUtil.URL_COOLAPK))
                        Snackbar.make(view, R.string.info_share, Snackbar.LENGTH_LONG).show()
                    }
                    setNegativeButton(R.string.not_now, null)
                    create().show()
                }
            }
            R.id.container_comment -> {
                var intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppUtil.URL_MARKET)).setPackage(AppUtil.MARKET_COOLAPK)
                if (intent.resolveActivity(packageManager) == null) intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppUtil.URL_COOLAPK))
                startActivity(intent)
            }
            R.id.container_discuss -> {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppUtil.URL_QQ_API))
                if (intent.resolveActivity(packageManager) == null) intent.data = Uri.parse(AppUtil.URL_PROJECT)
                startActivity(intent)
            }
            R.id.container_project -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppUtil.URL_PROJECT)))
            R.id.container_version_state -> {
                view.isClickable = false
                view.isLongClickable = false
                Thread({ checkVersionUpdate() }, AppUtil.THREAD_UPDATE).start()
                Snackbar.make(view, R.string.version_checking, Snackbar.LENGTH_INDEFINITE).show()
            }
            R.id.container_version_type -> if (++i >= 15) {
                i = 0
                manager.isPro = !manager.isPro
                typeDesc.setText(if (manager.isPro) R.string.version_type_pro else R.string.version_type_lite)
                Snackbar.make(view, R.string.version_type_changed, Snackbar.LENGTH_LONG).show()
            }
            R.id.container_special_thanks -> if (++i >= 5) {
                i = 0
                view.isClickable = false
                val extra = thanksDesc.text.toString() + System.lineSeparator() + System.lineSeparator() + getString(R.string.special_thanks_extra)
                Thread({
                    while (thanksDesc.text.length < extra.length) {
                        thanksDesc.post { thanksDesc.text = extra.substring(0, thanksDesc.text.length + 1) }
                        SystemClock.sleep(TEXT_SPEED)
                    }
                }, AppUtil.THREAD_UPDATE).start()
            }
            R.id.free_android -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppUtil.URL_FREE_ANDROID)))
        }
    }

    private fun onLongClick(): Boolean {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.action_reset)
            setMessage(R.string.msg_data_reset)
            setPositiveButton(R.string.action_reset) { _, _ ->
                var id = R.string.data_reset_done
                try {
                    DataUtil.updateData(context, null)
                    manager.setCheckLastTime(true)
                } catch (e: Exception) {
                    id = R.string.error_occurred
                }
                Snackbar.make(versionContainer, id, Snackbar.LENGTH_LONG).show()
            }
            setNegativeButton(android.R.string.cancel, null)
            create().show()
        }
        return true
    }

    private fun onDonate() {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.action_donate)
            setSingleChoiceItems(resources.getStringArray(R.array.donate_payment_entries), 0) { dialog, which ->
                dialog.cancel()
                when (which) {
                    0 -> try {
                        startActivity(Intent.parseUri(AppUtil.URL_ALIPAY_API, Intent.URI_INTENT_SCHEME))
                    } catch (e: Exception) {
                        showQRDialog(true)
                    }
                    1 -> showQRDialog(false)
                    2 -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppUtil.URL_PAYPAL)))
                }
                Snackbar.make(typeDesc, R.string.info_donate_thanks, Snackbar.LENGTH_INDEFINITE).show()
            }
            setNegativeButton(R.string.no_thanks, null)
            create().show()
        }
    }

    private fun showQRDialog(isAlipay: Boolean) {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.action_donate)
            setView(if (isAlipay) R.layout.qr_alipay else R.layout.qr_wechat)
            setPositiveButton(R.string.got_it, null)
            create().show()
        }
    }

    private fun checkVersionUpdate() {
        var id = R.string.version_update
        var log: String? = null
        var url = AppUtil.URL_RELEASE_LATEST
        try {
            val entry = DataUtil.getOnlineEntry()
            if (DataUtil.latestApp(entry)) {
                id = if (DataUtil.updateData(this, entry)) R.string.data_updated else R.string.version_latest
            } else {
                val json = JSONObject(IOUtil.stream2String(IOUtil.fromWeb(AppUtil.URL_RELEASE_LATEST_API)))
                log = DataUtil.getChangelog(json)
                url = DataUtil.getDownloadUrl(json)
            }
            manager.setCheckLastTime(false)
        } catch (e: Exception) {
            id = R.string.version_checking_failed
            if (e is IOException) log = getString(R.string.msg_network_error)
        }
        typeDesc.post {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            when {
                id == R.string.version_update -> {
                    AlertDialog.Builder(this).apply {
                        setTitle(id)
                        setMessage(log)
                        setPositiveButton(R.string.action_update) { _, _ -> startActivity(intent) }
                        setNegativeButton(R.string.no_thanks, null)
                        create().show()
                    }
                    Snackbar.make(typeDesc, id, Snackbar.LENGTH_LONG).show()
                }
                log != null -> Snackbar.make(typeDesc, id, Snackbar.LENGTH_INDEFINITE).setAction(R.string.action_details) { AppUtil.showLogDialog(this, log) }.show()
                else -> Snackbar.make(typeDesc, id, Snackbar.LENGTH_LONG).show()
            }
            versionContainer.setOnClickListener { startActivity(intent) }
        }
    }
}