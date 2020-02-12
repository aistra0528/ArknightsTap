package com.icebem.akt.overlay;

import android.content.Context;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;

import com.icebem.akt.R;

import java.lang.ref.WeakReference;

public class OverlayToast {
    private static final int LENGTH_INDEFINITE = 0;
    public static final int LENGTH_SHORT = 2000;
    public static final int LENGTH_LONG = 3000;

    private static WeakReference<OverlayView> ref;

    public static void show(Context context, CharSequence text, int duration) {
        TextView view = new TextView(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light));
        int padding = context.getResources().getDimensionPixelOffset(R.dimen.view_padding);
        view.setPadding(padding, padding, padding, padding);
        view.setBackgroundResource(R.drawable.bg_toast);
        view.setTextAppearance(R.style.TextAppearance_AppCompat);
        view.setText(text);
        view.setOnClickListener(v -> ref.get().remove());
        if (ref != null && ref.get() != null)
            ref.get().remove();
        ref = new WeakReference<>(new OverlayView(context, view));
        ref.get().setRelativeY(Math.min(context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels) >> 2);
        ref.get().show();
        if (duration != LENGTH_INDEFINITE)
            view.postDelayed(ref.get()::remove, duration);
    }

    public static void show(Context context, int resId, int duration) {
        show(context, context.getString(resId), duration);
    }
}