package com.icebem.akt.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.provider.Settings;

import com.icebem.akt.util.RandomUtil;

/**
 * 兼容性 API 管理
 */
public class CompatOperations {
    private static final int GESTURE_DURATION = 120;

    /**
     * 根据描述，在不支持该权限的版本上直接返回 true
     */
    public static boolean canDrawOverlays(Context context) {
        // Overlay permission is only required for Marshmallow (API 23) and above.
        // In previous APIs this permission is provided by default.
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
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

    /**
     * 执行点击操作
     */
    public static void performClick(AccessibilityService service, int x, int y) {
        if (PreferenceManager.getInstance(service).rootMode()) {
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
    private static boolean executeCommand(String command) {
        try {
            Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkRootPermission() {
        return executeCommand("su");
    }
}
