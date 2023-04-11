package com.icebem.akt.ui.about

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.icebem.akt.BuildConfig
import com.icebem.akt.R
import com.icebem.akt.databinding.FragmentAboutBinding
import com.icebem.akt.model.ContributorInfo
import com.icebem.akt.util.ArkData
import com.icebem.akt.util.ArkMaid
import com.icebem.akt.util.ArkPref
import org.json.JSONArray

class AboutFragment : Fragment(), OnClickListener {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAboutBinding.inflate(layoutInflater)
        ArkData.updateResult.observe(viewLifecycleOwner, ::handleUpdateResult)
        binding.txtVersionStateDesc.text = BuildConfig.VERSION_NAME
        binding.txtVersionTypeDesc.setText(if (ArkPref.isPro) R.string.version_type_pro else R.string.version_type_lite)
        binding.containerVersionState.setOnClickListener(this)
        binding.containerVersionState.setOnLongClickListener { resetDataDialog() }
        binding.containerVersionType.setOnClickListener(this)
        binding.containerVersionType.setOnLongClickListener { ArkMaid.reinstallSelf(requireContext()) }
        binding.containerComment.setOnClickListener(this)
        binding.containerProject.setOnClickListener(this)
        binding.containerDiscuss.setOnClickListener(this)
        binding.containerSupport.setOnClickListener(this)
        binding.actionShare.setOnClickListener(this)
        binding.actionDonate.setOnClickListener(this)
        val contributors = ContributorInfo.array
        var contributorsText = ""
        contributors.forEach { contributor ->
            contributorsText = if (contributorsText.isEmpty()) {
                contributor.toLocalizedString(resources)
            } else {
               String.format("%s\n%s", contributorsText, contributor.toLocalizedString(resources))
            }
        }
        binding.contributorsText.text = contributorsText
        return binding.root
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.container_version_state -> {
                view.isClickable = false
                view.isLongClickable = false
                ArkMaid.startUpdate(lifecycleScope, view)
            }
            R.id.container_version_type -> {
                view.tag = (view.tag as? Int)?.plus(1) ?: 1
                if (view.tag == 15) {
                    view.tag = 0
                    ArkPref.isPro = !ArkPref.isPro
                    binding.txtVersionTypeDesc.setText(if (ArkPref.isPro) R.string.version_type_pro else R.string.version_type_lite)
                    Snackbar.make(view, R.string.version_type_changed, Snackbar.LENGTH_INDEFINITE).setAction(R.string.action_reinstall) { ArkMaid.reinstallSelf(requireContext()) }.show()
                    if (ArkPref.isPro) ArkMaid.showRootDialog(requireActivity())
                }
            }
            R.id.container_comment -> ArkMaid.startUrl(requireActivity(), ArkMaid.URL_COOLAPK)
            R.id.container_project -> ArkMaid.startUrl(requireActivity(), ArkMaid.URL_PROJECT)
            R.id.container_discuss -> runCatching {
                ArkMaid.startUrl(requireActivity(), ArkMaid.URL_QQ_API)
            }
            R.id.container_support -> ArkMaid.startUrl(requireActivity(), ArkMaid.URL_WHY_FREE_SOFTWARE)
            R.id.action_share -> startActivity(Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, ArkMaid.URL_COOLAPK).setType("text/plain"))
            R.id.action_donate -> onDonate()
        }
    }

    private fun resetDataDialog(): Boolean {
        MaterialAlertDialogBuilder(requireActivity()).run {
            setTitle(R.string.action_reset)
            setMessage(R.string.msg_data_reset)
            setPositiveButton(R.string.action_reset) { _, _ ->
                var id = R.string.data_reset_done
                runCatching {
                    ArkData.resetData()
                    ArkPref.setCheckLastTime(false)
                }.onFailure {
                    Log.e(javaClass.simpleName, it.toString())
                    id = R.string.error_occurred
                }
                Snackbar.make(binding.root, id, Snackbar.LENGTH_LONG).show()
            }
            setNegativeButton(android.R.string.cancel, null)
            show()
        }
        return true
    }

    private fun onDonate() {
        MaterialAlertDialogBuilder(requireActivity()).run {
            setTitle(R.string.action_donate)
            setSingleChoiceItems(resources.getStringArray(R.array.donate_payment_entries), 0) { dialog, which ->
                dialog.dismiss()
                when (which) {
                    0 -> runCatching {
                        startActivity(Intent.parseUri(ArkMaid.URL_ALIPAY_API, Intent.URI_INTENT_SCHEME))
                    }.onFailure {
                        showQRDialog(true)
                    }
                    1 -> showQRDialog(false)
                    2 -> ArkMaid.startUrl(requireActivity(), ArkMaid.URL_PAYPAL)
                }
                Snackbar.make(binding.root, R.string.info_donate_thanks, Snackbar.LENGTH_INDEFINITE).show()
            }
            setNegativeButton(R.string.not_now, null)
            show()
        }
    }

    private fun showQRDialog(isAlipay: Boolean) {
        MaterialAlertDialogBuilder(requireActivity()).run {
            setTitle(R.string.action_donate)
            setView(if (isAlipay) R.layout.qr_alipay else R.layout.qr_wechat)
            setPositiveButton(R.string.not_now, null)
            show()
        }
    }

    private fun handleUpdateResult(result: JSONArray?) = ArkMaid.showUpdateResult(requireActivity(), binding.root, result) {
        binding.containerVersionState.isClickable = true
        binding.containerVersionState.isLongClickable = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}