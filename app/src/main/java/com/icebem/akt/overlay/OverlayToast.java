package com.icebem.akt.overlay;

import android.content.Context;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatTextView;

import com.icebem.akt.R;
import com.icebem.akt.app.ResolutionConfig;

import java.lang.ref.WeakReference;

public class OverlayToast {
    private static final int LENGTH_INDEFINITE = 0;
    public static final int LENGTH_SHORT = 2000;
    public static final int LENGTH_LONG = 3000;

    private static Runnable runnable;
    private static WeakReference<OverlayView> toast;

    public static void show(Context context, CharSequence text, int duration) {
        AppCompatTextView view;
        if (toast == null || toast.get() == null) {
            context = new ContextThemeWrapper(context.getApplicationContext(), R.style.Theme_AppCompat_Light);
            view = new AppCompatTextView(context);
            int padding = context.getResources().getDimensionPixelOffset(R.dimen.view_padding);
            view.setPadding(padding, padding, padding, padding);
            view.setBackgroundResource(R.drawable.bg_toast);
            view.setTextAppearance(context, R.style.TextAppearance_AppCompat);
            view.setOnClickListener(v -> {
                view.removeCallbacks(runnable);
                toast.get().remove();
            });
            toast = new WeakReference<>(new OverlayView(view));
            toast.get().setRelativePosition(0, ResolutionConfig.getAbsoluteHeight(context) >> 2);
        } else {
            view = (AppCompatTextView) toast.get().getView();
        }
        if (runnable == null) {
            runnable = toast.get()::remove;
        } else {
            view.removeCallbacks(runnable);
            toast.get().remove();
        }
        view.setText(text);
        toast.get().show();
        if (duration > LENGTH_INDEFINITE)
            view.postDelayed(runnable, duration);
    }

    public static void show(Context context, int resId, int duration) {
        show(context, context.getString(resId), duration);
    }
}