package com.icebem.akt.ui.tools

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.icebem.akt.R
import com.icebem.akt.activity.AboutActivity
import com.icebem.akt.activity.MainActivity
import com.icebem.akt.model.RecruitViewer
import com.icebem.akt.util.AppUtil

class ToolsFragment : Fragment() {
    private var viewer: RecruitViewer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val root = inflater.inflate(R.layout.fragment_tools, container, false)
        try {
            viewer = RecruitViewer(requireContext(), root)
        } catch (e: Exception) {
            AppUtil.showLogDialog(requireContext(), e.toString())
        }
        return root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_overlay -> (requireContext() as MainActivity).showOverlay()
            R.id.action_language -> {
                val packages = viewer!!.manager.availablePackages
                var index = viewer!!.manager.gamePackagePosition
                if (++index == packages.size) index = 0
                viewer!!.manager.setGamePackage(packages[index])
                viewer!!.resetTags(null)
            }
            R.id.action_about -> startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_tools, menu)
        if (viewer != null && viewer!!.manager.multiPackage) menu.findItem(R.id.action_language).isVisible = true
    }
}