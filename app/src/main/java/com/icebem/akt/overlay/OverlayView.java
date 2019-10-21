package com.icebem.akt.overlay;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class OverlayView {
    private int x, y;
    private boolean showing, moving;
    private View view;
    private View.OnClickListener listener;
    private WindowManager manager;
    private WindowManager.LayoutParams params;

    public OverlayView(Context context, View view, int gravity, boolean mobilizable, View.OnClickListener listener) {
        this.view = view;
        if (mobilizable) {
            this.listener = listener;
            view.setOnTouchListener(this::onTouch);
        }
        manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        params.x = 0;
        params.y = 0;
        params.width = mobilizable ? WindowManager.LayoutParams.WRAP_CONTENT : context.getResources().getDisplayMetrics().widthPixels;
        params.height = mobilizable ? WindowManager.LayoutParams.WRAP_CONTENT : context.getResources().getDisplayMetrics().widthPixels;
        params.gravity = gravity;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        params.format = PixelFormat.RGBA_8888;
        params.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        params.windowAnimations = android.R.style.Animation_Toast;
    }

    public View getView() {
        return view;
    }

    public OverlayView show() {
        if (!showing) {
            manager.addView(view, params);
            showing = true;
        }
        return this;
    }

    public void remove() {
        if (showing) {
            manager.removeView(view);
            showing = false;
        }
    }

    private boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (moving) moving = false;
                else listener.onClick(view);
                break;
            case MotionEvent.ACTION_DOWN:
                x = (int) event.getRawX();
                y = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!moving && Math.abs((int) event.getRawX() - x) < view.getWidth() / 4 && Math.abs((int) event.getRawY() - y) < view.getHeight() / 4)
                    break;
                moving = true;
                params.x += (int) event.getRawX() - x;
                params.y += (int) event.getRawY() - y;
                x = (int) event.getRawX();
                y = (int) event.getRawY();
                manager.updateViewLayout(view, params);
                break;
        }
        return true;
    }
}