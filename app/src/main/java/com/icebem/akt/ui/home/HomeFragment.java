package com.icebem.akt.ui.home;

import android.content.Intent;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.activity.AboutActivity;
import com.icebem.akt.activity.MainActivity;
import com.icebem.akt.app.PreferenceManager;
import com.icebem.akt.util.AppUtil;
import com.icebem.akt.util.IOUtil;
import com.icebem.akt.app.ResolutionConfig;

import org.json.JSONObject;

public class HomeFragment extends Fragment {
    private int i;
    private TextView state;
    private PreferenceManager manager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        ImageView stateImg = root.findViewById(R.id.img_state);
        state = root.findViewById(R.id.txt_state);
        manager = new PreferenceManager(getActivity());
        if (manager.isPro() && !manager.dataUpdated()) {
            stateImg.setImageResource(R.drawable.ic_state_running);
            state.setText(R.string.state_resolution_unsupported);
            int[] res = ResolutionConfig.getResolution(manager.getContext());
            AlertDialog.Builder builder = new AlertDialog.Builder(manager.getContext());
            builder.setTitle(R.string.state_resolution_unsupported);
            builder.setMessage(getString(R.string.msg_resolution_unsupported, res[0], res[1]));
            builder.setPositiveButton(R.string.got_it, null);
            builder.create().show();
        } else {
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable) stateImg.getDrawable();
            avd.registerAnimationCallback(new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    stateImg.setImageResource(R.drawable.ic_state_ready_anim);
                    state.setText(R.string.state_ready);
                    ((AnimatedVectorDrawable) stateImg.getDrawable()).start();
                    if (!stateImg.isClickable())
                        stateImg.setOnClickListener(v -> {
                            if (++i >= 3 && i < 15) {
                                if (i == 3) {
                                    stateImg.setImageResource(R.drawable.ic_state_error_anim);
                                    state.setText(R.string.error_occurred);
                                }
                                ((AnimatedVectorDrawable) stateImg.getDrawable()).start();
                            } else if (i >= 3) {
                                i = 0;
                                onAnimationEnd(stateImg.getDrawable());
                            }
                        });
                }
            });
            avd.start();
        }
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (BuildConfig.DEBUG) {
            Snackbar.make(state, R.string.version_type_beta, Snackbar.LENGTH_INDEFINITE).show();
        } else if (manager.autoUpdate()) {
            new Thread(this::checkVersionUpdate, AppUtil.THREAD_UPDATE).start();
            Snackbar.make(state, R.string.version_checking, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_timer:
                AlertDialog.Builder builder = new AlertDialog.Builder(manager.getContext());
                builder.setTitle(R.string.action_timer);
                builder.setSingleChoiceItems(manager.getTimerStrings(getActivity()), manager.getTimerPosition(), (dialog, which) -> {
                    dialog.cancel();
                    manager.setTimerTime(which);
                    Snackbar.make(state, getString(R.string.info_timer_set, manager.getTimerTime() == 0 ? getString(R.string.info_timer_none) : getString(R.string.info_timer_min, manager.getTimerTime())), Snackbar.LENGTH_LONG).show();
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.create().show();
                break;
            case R.id.action_night:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM : AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case R.id.action_about:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkVersionUpdate() {
        if (!(getActivity() instanceof MainActivity)) return;
        int id;
        String l = null, u = null;
        try {
            if (AppUtil.isLatestVersion(getActivity())) {
                id = R.string.version_latest;
            } else {
                id = R.string.version_update;
                JSONObject json = new JSONObject(IOUtil.stream2String(IOUtil.fromWeb(AppUtil.URL_RELEASE_LATEST_API)));
                l = AppUtil.getChangelog(json);
                u = AppUtil.getDownloadUrl(json);
            }
            manager.setCheckLastTime();
        } catch (Exception e) {
            id = R.string.version_checking_failed;
        }
        int result = id;
        String log = l, url = u;
        getActivity().runOnUiThread(() -> {
            if (result != R.string.version_checking_failed)
                ((MainActivity) getActivity()).updateSubtitleTime();
            if (result == R.string.version_update) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(result);
                builder.setMessage(log);
                builder.setPositiveButton(R.string.action_update, (dialog, which) -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
                builder.setNegativeButton(R.string.no_thanks, null);
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