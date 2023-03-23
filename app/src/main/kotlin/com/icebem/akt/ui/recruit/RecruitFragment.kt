package com.icebem.akt.ui.recruit

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.icebem.akt.R
import com.icebem.akt.databinding.FragmentRecruitBinding
import com.icebem.akt.model.RecruitViewer
import com.icebem.akt.util.ArkMaid
import com.icebem.akt.util.ArkPref

class RecruitFragment : Fragment(), MenuProvider {
    private var viewer: RecruitViewer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        return FragmentRecruitBinding.inflate(layoutInflater).run {
            try {
                viewer = RecruitViewer(requireContext(), this)
            } catch (e: Exception) {
                ArkMaid.showLogDialog(requireActivity(), e.toString())
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
        if (viewer != null && ArkPref.multiPackage) menu.findItem(R.id.action_language).isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewer = null
    }
}