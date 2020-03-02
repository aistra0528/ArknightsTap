package com.icebem.akt.model;

import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.icebem.akt.R;
import com.icebem.akt.adapter.MaterialAdapter;
import com.icebem.akt.app.PreferenceManager;

import org.json.JSONException;

import java.io.IOException;

public class MaterialGuide {
    private static final int COUNT_SPAN = 6;

    public MaterialGuide(PreferenceManager manager, View root) throws IOException, JSONException {
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(manager.getContext(), COUNT_SPAN));
        recyclerView.setAdapter(new MaterialAdapter(manager, COUNT_SPAN));
    }
}