package com.icebem.akt.ui.home;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.activity.AboutActivity;
import com.icebem.akt.app.PreferenceManager;
import com.icebem.akt.util.IOUtil;
import com.icebem.akt.util.ResolutionConfig;

import org.json.JSONObject;

public class HomeFragment extends Fragment {
    private TextView state;
    private PreferenceManager manager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        ImageView img_state = root.findViewById(R.id.img_service_state);
        state = root.findViewById(R.id.txt_service_state);
        manager = new PreferenceManager(inflater.getContext());
        if (manager.isPro() && !manager.dataUpdated()) {
            int[] res = ResolutionConfig.getResolution(manager.getContext());
            AlertDialog.Builder builder = new AlertDialog.Builder(manager.getContext());
            builder.setTitle(R.string.info_resolution_unsupported);
            builder.setMessage(String.format(getString(R.string.msg_resolution_unsupported), res[0], res[1]));
            builder.setPositiveButton(R.string.got_it, null);
            builder.create().show();
        }
        ((AnimatedVectorDrawable) img_state.getDrawable()).start();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (manager.autoUpdate()) {
            new Thread(this::checkVersionUpdate, "update").start();
            Snackbar.make(state, R.string.version_checking, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_timer:
                AlertDialog.Builder builder = new AlertDialog.Builder(manager.getContext());
                builder.setTitle(R.string.action_timer);
                builder.setSingleChoiceItems(manager.getTimerStrings(getContext()), manager.getTimerPosition(), (dialog, which) -> {
                    dialog.cancel();
                    manager.setTimerTime(which);
                    Snackbar.make(state, String.format(getString(R.string.info_timer_set), manager.getTimerTime() == 0 ? getString(R.string.info_timer_none) : String.format(getString(R.string.info_timer_min), manager.getTimerTime())), Snackbar.LENGTH_LONG).show();
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.create().show();
                break;
            case R.id.action_about:
                startActivity(new Intent(manager.getContext(), AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkVersionUpdate() {
        int id;
        String l = null, u = null;
        try {
            JSONObject json = new JSONObject(IOUtil.stream2String(IOUtil.fromWeb("https://api.github.com/repos/IcebemAst/ArknightsTap/releases/latest")));
            if (json.getString("tag_name").contains(BuildConfig.VERSION_NAME)) {
                id = R.string.version_latest;
            } else {
                id = R.string.version_update;
                l = json.getString("name") + "\n" + json.getString("body");
                u = json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
            }
        } catch (Exception e) {
            id = R.string.version_checking_failed;
        }
        int result = id;
        String log = l, url = u;
        ((AppCompatActivity) manager.getContext()).runOnUiThread(() -> {
            if (result == R.string.version_update) {
                AlertDialog.Builder builder = new AlertDialog.Builder(manager.getContext());
                builder.setTitle(result);
                builder.setMessage(log);
                builder.setPositiveButton(R.string.action_update, (dialog, which) -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.create().show();
            } else Snackbar.make(state, result, Snackbar.LENGTH_LONG).show();
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_home, menu);
        if (manager.isPro())
            menu.findItem(R.id.action_timer).setVisible(true);
    }
}