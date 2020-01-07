package com.icebem.akt.ui.tools;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.icebem.akt.R;
import com.icebem.akt.model.RecruitViewer;

import org.json.JSONException;

import java.io.IOException;

public class ToolsFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tools, container, false);
        try {
            new RecruitViewer(getContext(), root);
        } catch (IOException | JSONException e) {
            Log.e(getClass().getSimpleName(), Log.getStackTraceString(e));
        }
        return root;
    }
}