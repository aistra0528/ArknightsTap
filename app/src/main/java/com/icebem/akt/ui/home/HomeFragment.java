package com.icebem.akt.ui.home;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.google.android.material.snackbar.Snackbar;
import com.icebem.akt.BuildConfig;
import com.icebem.akt.R;
import com.icebem.akt.activity.AboutActivity;
import com.icebem.akt.activity.MainActivity;
import com.icebem.akt.app.PreferenceManager;
import com.icebem.akt.app.ResolutionConfig;
import com.icebem.akt.util.AppUtil;
import com.icebem.akt.util.DataUtil;
import com.icebem.akt.util.IOUtil;
import com.icebem.akt.util.RandomUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class HomeFragment extends Fragment {
    private int i;
    private TextView state;
    private ImageView stateImg;
    private PreferenceManager manager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        TextView tip = root.findViewById(R.id.txt_tips);
        stateImg = root.findViewById(R.id.img_state);
        state = root.findViewById(R.id.txt_state);
        manager = PreferenceManager.getInstance(getContext());
        if (manager.isPro() && !manager.isActivated()) {
            tip.setText(R.string.tip_not_found);
            tip.setOnClickListener(v -> {
                manager.setPro(true);
                AppUtil.showAlertDialog(getActivity(), getString(R.string.reboot_device), getString(R.string.version_type_changed));
            });
        } else if (BuildConfig.DEBUG) {
            tip.setText(R.string.version_type_beta);
        } else {
            try {
                JSONArray array = DataUtil.getSloganData(getActivity());
                JSONObject obj = array.getJSONObject(RandomUtil.randomIndex(array.length()));
                tip.setSingleLine(true);
                tip.setText(getString(R.string.operator_slogan, obj.getString("slogan"), obj.getString("name")));
                tip.postDelayed(() -> {
                    tip.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                    tip.setMarqueeRepeatLimit(-1);
                    tip.setSelected(true);
                    tip.setFocusable(true);
                    tip.setFocusableInTouchMode(true);
                }, 2000);
            } catch (Exception ignored) {
                tip.setText(R.string.error_occurred);
            }
        }
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (manager.autoUpdate())
            startUpdateThread();
        else onStateEnd();
    }

    private void onStateEnd() {
        if (getActivity() == null) return;
        if (manager.isPro() && manager.unsupportedResolution()) {
            stateImg.setImageResource(R.drawable.ic_state_running);
            state.setText(R.string.state_resolution_unsupported);
            int[] res = ResolutionConfig.getAbsoluteResolution(getActivity());
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.state_resolution_unsupported);
            builder.setMessage(getString(R.string.msg_resolution_unsupported, res[0], res[1]));
            builder.setPositiveButton(R.string.got_it, null);
            builder.setNeutralButton(R.string.action_update, (dialog, which) -> startUpdateThread());
            builder.create().show();
        } else {
            stateImg.setImageResource(R.drawable.ic_state_running_anim);
            state.setText(R.string.state_loading);
            AnimatedVectorDrawableCompat.registerAnimationCallback(stateImg.getDrawable(), new Animatable2Compat.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    stateImg.setImageResource(R.drawable.ic_state_ready_anim);
                    state.setText(R.string.state_ready);
                    ((Animatable) stateImg.getDrawable()).start();
                    if (!stateImg.isClickable())
                        stateImg.setOnClickListener(v -> {
                            if (++i >= 3 && i < 15) {
                                if (i == 3) {
                                    stateImg.setImageResource(R.drawable.ic_state_error_anim);
                                    state.setText(R.string.error_occurred);
                                }
                                ((Animatable) stateImg.getDrawable()).start();
                            } else if (i >= 3) {
                                i = 0;
                                onAnimationEnd(stateImg.getDrawable());
                            }
                        });
                }
            });
            ((Animatable) stateImg.getDrawable()).start();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (getActivity() == null) return false;
        switch (item.getItemId()) {
            case R.id.action_timer:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

    private void startUpdateThread() {
        new Thread(this::checkVersionUpdate, AppUtil.THREAD_UPDATE).start();
        Snackbar.make(state, R.string.version_checking, Snackbar.LENGTH_LONG).show();
    }

    private void checkVersionUpdate() {
        if (!(getActivity() instanceof MainActivity)) return;
        int id = R.string.version_update;
        String l = null, u = null;
        try {
            if (AppUtil.isLatestVersion()) {
                id = DataUtil.updateData(manager, true) ? R.string.data_updated : R.string.version_latest;
            } else {
                JSONObject json = new JSONObject(IOUtil.stream2String(IOUtil.fromWeb(AppUtil.URL_RELEASE_LATEST_API)));
                l = AppUtil.getChangelog(json);
                u = AppUtil.getDownloadUrl(json);
            }
            manager.setCheckLastTime(false);
        } catch (Exception e) {
            id = R.string.version_checking_failed;
            if (e instanceof IOException && getActivity() != null)
                l = getActivity().getString(R.string.msg_network_error);
        }
        int result = id;
        String log = l, url = u;
        if (!(getActivity() instanceof MainActivity)) return;
        View fab = ((MainActivity) getActivity()).getFab();
        fab.post(() -> {
            if (result != R.string.version_checking_failed)
                ((MainActivity) getActivity()).updateSubtitleTime();
            if (result == R.string.version_update) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(result);
                builder.setMessage(log);
                builder.setPositiveButton(R.string.action_update, (dialog, which) -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
                builder.setNegativeButton(R.string.no_thanks, null);
                builder.create().show();
            } else if (log != null) {
                Snackbar.make(fab, result, Snackbar.LENGTH_INDEFINITE).setAction(R.string.action_details, v -> AppUtil.showLogDialog(getActivity(), log)).show();
            } else Snackbar.make(fab, result, Snackbar.LENGTH_LONG).show();
            onStateEnd();
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