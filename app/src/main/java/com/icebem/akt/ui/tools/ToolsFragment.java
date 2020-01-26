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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.icebem.akt.R;
import com.icebem.akt.activity.AboutActivity;
import com.icebem.akt.activity.MainActivity;
import com.icebem.akt.app.BaseApplication;
import com.icebem.akt.model.RecruitViewer;
import com.icebem.akt.util.AppUtil;

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
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showOverlay();
                    if (((BaseApplication) getActivity().getApplication()).isOverlayServiceRunning())
                        item.setVisible(false);
                }
                break;
            case R.id.action_language:
                if (viewer == null) break;
                AlertDialog.Builder builder = new AlertDialog.Builder(viewer.getContext());
                builder.setTitle(R.string.game_server_title);
                builder.setSingleChoiceItems(viewer.getContext().getResources().getStringArray(R.array.game_server_entries), viewer.getManager().getGamePackagePosition(), (dialog, which) -> {
                    dialog.cancel();
                    viewer.getManager().setGamePackage(viewer.getContext().getResources().getStringArray(R.array.game_server_values)[which]);
                    viewer.resetTags(null);
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.create().show();
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
        if (getActivity() instanceof MainActivity && ((BaseApplication) getActivity().getApplication()).isOverlayServiceRunning())
            menu.findItem(R.id.action_overlay).setVisible(false);
    }
}