package com.icebem.akt.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.provider.Settings;
import android.widget.TextView;

import com.icebem.akt.util.RandomUtil;

/**
 * 兼容性 API 管理
 */
public class CompatOperations {

    /**
     * 根据描述，在不支持该权限的版本上直接返回 true
     */
    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        } else {
            // Overlay permission is only required for Marshmallow (API 23) and above.
            // In previous APIs this permission is provided by default.
            return true;
        }
    }

    /**
     * 禁止一个服务
     *
     * @param service        要禁止的服务
     * @param fallbackAction API 不支持时的回退方案
     */
    public static void disableSelf(AccessibilityService service, Runnable fallbackAction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            service.disableSelf();
        } else {
            fallbackAction.run();
        }
    }

    private static final int GESTURE_DURATION = 120;

    /**
     * 执行点击操作
     */
    public static void performClick(AccessibilityService service, int x, int y) {
        if (PreferenceManager.getInstance(service).rootCompatible()) {
            executeCommand(String.format("input tap %s %s", x, y));
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Path path = new Path();
            path.moveTo(x, y);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, RandomUtil.randomP(GESTURE_DURATION)));
            service.dispatchGesture(builder.build(), null, null);
        }
    }

    /**
     * 执行一个 shell command
     *
     * @param command command 内容
     */
    private static void executeCommand(String command) {
        try {
            Runtime.getRuntime().exec(new String[]{"su", "-c", command});
        } catch (Exception ignored) {
        }
    }

    public static void checkRootPermission() {
        executeCommand("su");
    }

    public static void setTextAppearance(Context context, TextView view, int style) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.setTextAppearance(style);
        } else {
            view.setTextAppearance(context, style);
        }
    }
}
