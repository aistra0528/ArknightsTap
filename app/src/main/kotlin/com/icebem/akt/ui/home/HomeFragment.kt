package com.icebem.akt.ui.home

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.icebem.akt.R
import com.icebem.akt.databinding.DialogInputBinding
import com.icebem.akt.databinding.FragmentHomeBinding
import com.icebem.akt.util.ArkData
import com.icebem.akt.util.ArkMaid
import com.icebem.akt.util.ArkPref
import com.icebem.akt.util.Resolution
import org.json.JSONArray
import java.text.SimpleDateFormat

class HomeFragment : Fragment(), MenuProvider, OnClickListener {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val callback by lazy { AnimCallback() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        _binding = FragmentHomeBinding.inflate(layoutInflater)
        ArkData.updateResult.observe(viewLifecycleOwner, ::handleUpdateResult)
        binding.txtData.text = SimpleDateFormat.getDateTimeInstance().format(ArkPref.checkLastTime)
        binding.navRecruit.setOnClickListener(this)
        binding.navSettings.setOnClickListener(this)
        binding.navAbout.setOnClickListener(this)
        binding.actionWiki.setOnClickListener(this)
        binding.actionPenguin.setOnClickListener(this)
        return binding.root
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.txt_tips -> {
                ArkPref.isPro = true
                Snackbar.make(view, R.string.version_type_changed, Snackbar.LENGTH_INDEFINITE).setAction(R.string.action_reinstall) { ArkMaid.reinstallSelf(requireContext()) }.show()
            }
            R.id.img_state -> {
                view.tag = (view.tag as? Int)?.plus(1) ?: 1
                if (view.tag in 3 until 15) {
                    if (view.tag == 3) {
                        binding.imgState.setImageResource(R.drawable.ic_state_error_anim)
                        binding.txtState.setText(R.string.error_occurred)
                        binding.txtTips.setText(R.string.error_slogan)
                    }
                    (binding.imgState.drawable as Animatable).start()
                } else if (view.tag == 15) {
                    view.tag = 0
                    callback.onAnimationEnd(binding.imgState.drawable)
                }
            }
            R.id.action_wiki -> ArkMaid.startUrl(requireActivity(), ArkMaid.URL_PRTS_WIKI)
            R.id.action_penguin -> ArkMaid.startUrl(requireActivity(), ArkMaid.URL_PENGUIN_STATS)
            else -> findNavController().navigate(view.id)
        }
    }

    override fun onStart() {
        super.onStart()
        if (ArkPref.autoUpdate) ArkMaid.startUpdate(lifecycleScope, binding.root) else onStateEnd()
        if (ArkPref.isPro) binding.imgState.setOnLongClickListener {
            val input = DialogInputBinding.inflate(layoutInflater, FrameLayout(requireActivity()), true)
            input.inputLayout.setHint(R.string.customize_points)
            input.editText.setText(ArkData.getGestureData().toString())
            MaterialAlertDialogBuilder(requireActivity()).run {
                setView(input.root.parent as View)
                setPositiveButton(android.R.string.ok) { _, _ -> if (!ArkData.setGestureData(input.editText.text.toString())) Snackbar.make(it, R.string.wrong_format, Snackbar.LENGTH_LONG).show() }
                setNeutralButton(R.string.action_reset) { _, _ -> ArkData.resetGestureData() }
                setNegativeButton(android.R.string.cancel, null)
                show()
            }
            true
        }
    }

    private fun handleUpdateResult(result: JSONArray?) = ArkMaid.showUpdateResult(requireActivity(), binding.root, result) {
        binding.txtData.text = SimpleDateFormat.getDateTimeInstance().format(ArkPref.checkLastTime)
        onStateEnd()
    }

    private fun onStateEnd() {
        if (ArkPref.isPro && ArkPref.unsupportedResolution) {
            binding.imgState.setImageResource(R.drawable.ic_state_running)
            binding.txtState.setText(R.string.state_resolution_unsupported)
            val res = Resolution.absoluteResolution
            MaterialAlertDialogBuilder(requireActivity()).run {
                setTitle(R.string.state_resolution_unsupported)
                setMessage(getString(R.string.msg_resolution_unsupported, res[0], res[1]))
                setPositiveButton(R.string.got_it, null)
                setNeutralButton(R.string.action_update) { _, _ -> ArkMaid.startUpdate(lifecycleScope, binding.root) }
                show()
            }
        } else {
            binding.imgState.setImageResource(R.drawable.ic_state_running_anim)
            binding.txtState.setText(R.string.state_loading)
            AnimatedVectorDrawableCompat.registerAnimationCallback(binding.imgState.drawable, callback)
            (binding.imgState.drawable as Animatable).start()
        }
    }

    private inner class AnimCallback : Animatable2Compat.AnimationCallback() {
        override fun onAnimationEnd(drawable: Drawable) {
            binding.imgState.setImageResource(R.drawable.ic_state_ready_anim)
            binding.txtState.setText(R.string.state_ready)
            if (ArkPref.isPro && !ArkPref.isActivated) {
                binding.txtTips.setText(R.string.tip_not_found)
                binding.txtTips.setOnClickListener(this@HomeFragment)
            } else binding.txtTips.text = ArkPref.nextSlogan
            (binding.imgState.drawable as Animatable).start()
            if (!binding.imgState.hasOnClickListeners()) binding.imgState.setOnClickListener(this@HomeFragment)
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_timer -> MaterialAlertDialogBuilder(requireActivity()).run {
                setTitle(R.string.action_timer)
                setSingleChoiceItems(ArkPref.timerStrings, ArkPref.timerPosition) { dialog, which ->
                    dialog.dismiss()
                    ArkPref.timerTime = which
                    Snackbar.make(binding.root, getString(R.string.info_timer_set, if (ArkPref.timerTime == 0) getString(R.string.info_timer_none) else getString(R.string.info_timer_min, ArkPref.timerTime)), Snackbar.LENGTH_LONG).show()
                }
                setNegativeButton(android.R.string.cancel, null)
                show()
            }
            R.id.action_night -> AppCompatDelegate.setDefaultNightMode(if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else AppCompatDelegate.MODE_NIGHT_YES)
        }
        return false
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
        if (ArkPref.isPro) menu.findItem(R.id.action_timer).isVisible = true
    }

    override fun onDestroyView() {
        AnimatedVectorDrawableCompat.clearAnimationCallbacks(binding.imgState.drawable)
        super.onDestroyView()
        _binding = null
    }
}