package com.icebem.akt.ui.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.icebem.akt.R;
import com.icebem.akt.activity.AboutActivity;
import com.icebem.akt.app.PreferenceManager;
import com.icebem.akt.util.ResolutionConfig;

public class HomeFragment extends Fragment {
    private int timer_position;
    private PreferenceManager manager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        ImageView img_state = root.findViewById(R.id.img_service_state);
        manager = new PreferenceManager(inflater.getContext());
        if (manager.isPro() && !manager.dataUpdated()) {
            img_state.setImageDrawable(manager.getContext().getDrawable(R.drawable.ic_state_update));
            TextView state = root.findViewById(R.id.txt_service_state);
            state.setText(R.string.state_update_request);
            int[] res = ResolutionConfig.getResolution(manager.getContext());
            AlertDialog.Builder builder = new AlertDialog.Builder(manager.getContext());
            builder.setTitle(R.string.info_resolution_unsupported);
            builder.setMessage(String.format(getString(R.string.msg_resolution_unsupported), res[0], res[1]));
            builder.setPositiveButton(R.string.got_it, null);
            builder.create().show();
        }
        if (img_state.getDrawable() instanceof AnimatedVectorDrawable)
            ((AnimatedVectorDrawable) img_state.getDrawable()).start();
        return root;
    }

    private void onCheck(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                manager.setTimerTime(timer_position);
                if (getView() != null)
                    Snackbar.make(getView(), String.format(getString(R.string.info_timer_set), manager.getTimerTime() == 0 ? getString(R.string.info_timer_none) : String.format(getString(R.string.info_timer_min), manager.getTimerTime())), Snackbar.LENGTH_LONG).show();
                break;
            default:
                timer_position = which;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_timer:
                timer_position = manager.getTimerPosition();
                AlertDialog.Builder builder = new AlertDialog.Builder(manager.getContext());
                builder.setTitle(R.string.action_timer);
                builder.setSingleChoiceItems(manager.getTimerStrings(getContext()), timer_position, this::onCheck);
                builder.setPositiveButton(android.R.string.ok, this::onCheck);
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.create().show();
                break;
            case R.id.action_about:
                startActivity(new Intent(manager.getContext(), AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        if (!manager.isPro())
            menu.findItem(R.id.action_timer).setVisible(false);
    }
}