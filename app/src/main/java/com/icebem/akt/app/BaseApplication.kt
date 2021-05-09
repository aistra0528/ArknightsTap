package com.icebem.akt.app;

import android.app.ActivityManager;
import android.app.Application;
import android.os.Build;
import android.provider.Settings;

import com.icebem.akt.service.GestureService;
import com.icebem.akt.service.OverlayService;

public class BaseApplication extends Application {
    public boolean isGestureServiceRunning() {
        return isServiceRunning(GestureService.class.getName());
    }

    public boolean isGestureServiceEnabled() {
        String pref = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return pref != null && pref.contains(getPackageName() + "/" + GestureService.class.getName());
    }

    public boolean isOverlayServiceRunning() {
        return isServiceRunning(OverlayService.class.getName());
    }

    public boolean isServiceRunning(String name) {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (am != null)
            // getRunningServices方法在Android 8.0已过时，只会获取应用自身正在运行的服务，所以列表条数最大值不用太大
            for (ActivityManager.RunningServiceInfo info : am.getRunningServices(Build.VERSION.SDK_INT < Build.VERSION_CODES.O ? Integer.MAX_VALUE : 5))
                if (info.service.getClassName().equals(name))
                    return true;
        return false;
    }
}