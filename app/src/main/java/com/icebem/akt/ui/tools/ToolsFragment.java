package com.icebem.akt.ui.tools;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.icebem.akt.R;
import com.icebem.akt.activity.AboutActivity;
import com.icebem.akt.activity.MainActivity;
import com.icebem.akt.model.RecruitViewer;
import com.icebem.akt.util.AppUtil;

import java.util.ArrayList;

public class ToolsFragment extends Fragment {
    private RecruitViewer viewer;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_tools, container, false);
        try {
            viewer = new RecruitViewer(getActivity(), root);
        } catch (Exception e) {
            if (getActivity() instanceof MainActivity)
                AppUtil.showLogDialog(getActivity(), e);
        }
        return root;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_overlay:
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).showOverlay();
                break;
            case R.id.action_language:
                if (viewer == null) break;
                ArrayList<String> packages = viewer.getManager().getAvailablePackages();
                int index = viewer.getManager().getGamePackagePosition();
                if (++index == packages.size()) index = 0;
                viewer.getManager().setGamePackage(packages.get(index));
                viewer.resetTags(null);
                break;
            case R.id.action_about:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_tools, menu);
        if (viewer != null && viewer.getManager().multiPackage())
            menu.findItem(R.id.action_language).setVisible(true);
    }
}