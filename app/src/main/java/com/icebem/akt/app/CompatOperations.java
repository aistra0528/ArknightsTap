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
     * Overlay permission is only required for Marshmallow (API 23) and above.
     * In previous APIs this permission is provided by default.
     */
    public static boolean requireOverlayPermission(Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context);
    }

    /**
     * 停用无障碍服务
     *
     * @param service        要停用的服务
     * @param fallbackAction API 不支持时的回退方案
     */
    public static void disableSelf(AccessibilityService service, Runnable fallbackAction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            service.disableSelf();
        else fallbackAction.run();
    }

    public static void disableKeepScreen(AccessibilityService service) {
        service.performGlobalAction(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN : AccessibilityService.GLOBAL_ACTION_HOME);
    }

    /**
     * 执行点击操作
     */
    public static void performClick(AccessibilityService service, int x, int y) {
        x = RandomUtil.randomPoint(x);
        y = RandomUtil.randomPoint(y);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path path = new Path();
            path.moveTo(x, y);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, RandomUtil.randomTime(GESTURE_DURATION)));
            service.dispatchGesture(builder.build(), null, null);
        } else if (PreferenceManager.getInstance(service).rootMode()) {
            executeCommand(String.format("input tap %s %s", x, y));
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
}
