package com.icebem.akt.ui.recruit

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.icebem.akt.R
import com.icebem.akt.databinding.FragmentRecruitBinding
import com.icebem.akt.model.RecruitViewer
import com.icebem.akt.util.ArkMaid
import com.icebem.akt.util.ArkPref
import kotlinx.coroutines.launch

class RecruitFragment : Fragment(), MenuProvider {
    private var viewer: RecruitViewer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        return FragmentRecruitBinding.inflate(layoutInflater).run {
            root.visibility = View.INVISIBLE
            lifecycleScope.launch {
                runCatching {
                    viewer = RecruitViewer(requireContext(), this@run)
                    root.visibility = View.VISIBLE
                }.onFailure {
                    ArkMaid.showLogDialog(requireActivity(), it.toString())
                }
            }
            root
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_overlay -> ArkMaid.requireOverlayService(requireActivity())
            R.id.action_language -> viewer?.toggleServer()
        }
        return false
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_tools, menu)
        if (ArkPref.multiPackage) menu.findItem(R.id.action_language).isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewer = null
    }
}