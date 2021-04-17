package com.icebem.akt.model

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.icebem.akt.R
import com.icebem.akt.adapter.MaterialAdapter
import com.icebem.akt.app.PreferenceManager

class MaterialGuide(manager: PreferenceManager, root: View) {
    companion object {
        private const val COUNT_SPAN = 6
    }

    init {
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(root.context, COUNT_SPAN)
        recyclerView.adapter = MaterialAdapter(manager, COUNT_SPAN)
    }
}