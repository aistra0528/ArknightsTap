package com.icebem.akt.service;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.view.ContextThemeWrapper;

import com.icebem.akt.R;
import com.icebem.akt.app.BaseApplication;
import com.icebem.akt.app.PreferenceManager;
import com.icebem.akt.app.ResolutionConfig;
import com.icebem.akt.model.MaterialGuide;
import com.icebem.akt.model.RecruitViewer;
import com.icebem.akt.overlay.OverlayToast;
import com.icebem.akt.overlay.OverlayView;

public class OverlayService extends Service {
    private int screenSize;
    private MaterialGuide guide;
    private RecruitViewer viewer;
    private PreferenceManager manager;
    private OverlayView current, fab, menu, recruit, counter, material;

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.AppTheme_Dark);
        screenSize = ResolutionConfig.getAbsoluteHeight(this);
        initRecruitView();
        manager = viewer == null ? new PreferenceManager(this) : viewer.getManager();
        initCounterView();
        initMaterialView();
        initMenuView();
        current = menu;
        initFabView();
        showTargetView(fab);
        if (!((BaseApplication) getApplication()).isGestureServiceRunning())
            OverlayToast.show(this, R.string.info_overlay_connected, OverlayToast.LENGTH_SHORT);
    }

    private void initRecruitView() {
        recruit = new OverlayView(this, R.layout.overlay_recruit);
        recruit.setGravity(Gravity.END | Gravity.TOP);
        recruit.resize(screenSize, screenSize);
        try {
            viewer = new RecruitViewer(this, recruit.getView());
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), Log.getStackTraceString(e));
        }
        if (viewer == null) return;
        recruit.getView().findViewById(R.id.txt_title).setOnTouchListener(this::updateRecruitView);
        recruit.getView().findViewById(R.id.action_menu).setOnClickListener(v -> showTargetView(menu));
        recruit.getView().findViewById(R.id.action_server).setOnClickListener(view -> {
            int index = viewer.getManager().getGamePackagePosition();
            String[] values = getResources().getStringArray(R.array.game_server_values);
            if (++index == values.length) index = 0;
            viewer.getManager().setGamePackage(values[index]);
            resetRecruitView(view);
            OverlayToast.show(this, getResources().getStringArray(R.array.game_server_entries)[index], OverlayToast.LENGTH_SHORT);
        });
        ImageButton collapse = recruit.getView().findViewById(R.id.action_collapse);
        collapse.setOnClickListener(v -> showTargetView(fab));
        collapse.setOnLongClickListener(this::stopSelf);
    }

    private boolean updateRecruitView(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                viewer.getRootView().setVisibility(View.INVISIBLE);
                break;
            case MotionEvent.ACTION_UP:
                resetRecruitView(view);
                viewer.getRootView().setVisibility(View.VISIBLE);
                break;
        }
        return view != null;
    }

    private void resetRecruitView(View view) {
        viewer.resetTags(view);
    }

    private void initCounterView() {
        counter = new OverlayView(this, R.layout.overlay_counter);
        counter.setMobilizable(true);
        TextView tip = counter.getView().findViewById(R.id.txt_counter_tips);
        counter.getView().findViewById(R.id.action_menu).setOnClickListener(v -> showTargetView(menu));
        ImageButton minus = counter.getView().findViewById(R.id.action_minus);
        minus.setOnClickListener(v -> updateCounterView(tip, manager.getHeadhuntCount(), -1));
        minus.setOnLongClickListener(v -> updateCounterView(tip, 0, 0));
        ImageButton plus = counter.getView().findViewById(R.id.action_plus);
        plus.setOnClickListener(v -> updateCounterView(tip, manager.getHeadhuntCount(), 1));
        plus.setOnLongClickListener(v -> updateCounterView(tip, manager.getHeadhuntCount(), 10));
        ImageButton collapse = counter.getView().findViewById(R.id.action_collapse);
        collapse.setOnClickListener(v -> showTargetView(fab));
        collapse.setOnLongClickListener(this::stopSelf);
        updateCounterView(tip, manager.getHeadhuntCount(), 0);
    }

    private boolean updateCounterView(TextView tip, int count, int delta) {
        count += delta;
        if (count < 0)
            count += 99;
        else if (count >= 99)
            count -= 99;
        manager.setHeadhuntCount(count);
        int possibility = 2;
        if (count > 49)
            possibility += count - 49 << 1;
        tip.setText(getString(R.string.tip_counter_default, count, possibility));
        return true;
    }

    private void initMaterialView() {
        material = new OverlayView(this, R.layout.overlay_material);
        material.setGravity(Gravity.START | Gravity.TOP);
        material.setMobilizable(true);
        material.resize(screenSize, screenSize);
        try {
            guide = new MaterialGuide(manager, material.getView());
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), Log.getStackTraceString(e));
        }
        if (guide == null) return;
        material.getView().findViewById(R.id.action_menu).setOnClickListener(v -> showTargetView(menu));
        ImageButton collapse = material.getView().findViewById(R.id.action_collapse);
        collapse.setOnClickListener(v -> showTargetView(fab));
        collapse.setOnLongClickListener(this::stopSelf);
    }

    private void initMenuView() {
        menu = new OverlayView(this, R.layout.overlay_menu);
        View root = menu.getView();
        root.setBackgroundResource(R.drawable.bg_radius);
        root.setElevation(getResources().getDimensionPixelOffset(R.dimen.overlay_elevation));
        root.findViewById(R.id.action_recruit).setOnClickListener(v -> {
            if (viewer == null)
                OverlayToast.show(this, R.string.error_occurred, OverlayToast.LENGTH_SHORT);
            else
                showTargetView(recruit);
        });
        root.findViewById(R.id.action_counter).setOnClickListener(v -> showTargetView(counter));
        root.findViewById(R.id.action_material).setOnClickListener(v -> {
            if (guide == null)
                OverlayToast.show(this, R.string.error_occurred, OverlayToast.LENGTH_SHORT);
            else
                showTargetView(material);
        });
        if (manager.isPro()) {
            View gesture = root.findViewById(R.id.action_gesture);
            gesture.setVisibility(View.VISIBLE);
            gesture.setOnClickListener(v -> {
                showTargetView(fab);
                if (((BaseApplication) getApplication()).isGestureServiceRunning()) {
                    ((BaseApplication) getApplication()).getGestureService().disableSelf();
                } else {
                    Toast.makeText(this, R.string.info_gesture_request, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            });
        }
        root.findViewById(R.id.action_collapse).setOnClickListener(v -> showTargetView(fab));
        root.findViewById(R.id.action_disconnect).setOnClickListener(this::stopSelf);
    }

    private void updateMenuView() {
        if (manager.isPro()) {
            TextView desc = menu.getView().findViewById(R.id.action_gesture_desc);
            if (((BaseApplication) getApplication()).isGestureServiceRunning()) {
                desc.setTextAppearance(R.style.TextAppearance_AppCompat_Widget_Button_Borderless_Colored);
                desc.setTextColor(getColor(R.color.colorError));
                desc.setText(R.string.action_disconnect);
            } else {
                desc.setTextAppearance(R.style.TextAppearance_AppCompat_Body1);
                desc.setText(manager.getTimerTime() == 0 ? getString(R.string.info_timer_none) : getString(R.string.info_timer_min, manager.getTimerTime()));
            }
        }
    }

    private void initFabView() {
        ImageButton btn = new ImageButton(new ContextThemeWrapper(this, R.style.ThemeOverlay_AppCompat_Light));
        btn.setImageResource(R.drawable.ic_akt);
        btn.setBackgroundResource(R.drawable.bg_oval);
        btn.setPadding(0, 0, 0, 0);
        btn.setElevation(getResources().getDimensionPixelOffset(R.dimen.fab_elevation));
        int size = getResources().getDimensionPixelOffset(R.dimen.fab_mini_size);
        btn.setMinimumWidth(size);
        btn.setMinimumHeight(size);
        btn.setOnClickListener(v -> showTargetView(current));
        btn.setOnLongClickListener(this::stopSelf);
        fab = new OverlayView(this, btn);
        fab.setGravity(Gravity.END | Gravity.TOP);
        fab.setRelativePosition(screenSize - size >> 1, 0);
        fab.setMobilizable(true);
    }

    private void showTargetView(OverlayView target) {
        if (target == fab)
            target.show(current);
        else {
            if (target == current) {
                fab.remove();
                if (target == recruit)
                    resetRecruitView(fab.getView());
            }
            if (target == menu)
                updateMenuView();
            current = target.show(current);
        }
    }

    private boolean stopSelf(View view) {
        stopSelf();
        return view != null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration cfg) {
        super.onConfigurationChanged(cfg);
        fab.setRelativePosition(screenSize - fab.getView().getWidth() >> 1, 0);
        counter.setRelativePosition(0, 0);
        material.setRelativePosition(0, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fab.remove();
        current.remove();
        if (((BaseApplication) getApplication()).isGestureServiceRunning())
            ((BaseApplication) getApplication()).getGestureService().disableSelf();
        else
            OverlayToast.show(this, R.string.info_overlay_disconnected, OverlayToast.LENGTH_SHORT);
    }
}