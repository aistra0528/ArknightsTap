package com.icebem.akt.app;

import android.app.ActivityManager;
import android.app.Application;
import android.os.Build;

import com.icebem.akt.service.GestureService;
import com.icebem.akt.service.OverlayService;

public class CoreApplication extends Application {
    private GestureService service;

    public void setGestureService(GestureService service) {
        this.service = service;
    }

    public GestureService getGestureService() {
        return service;
    }

    public boolean isGestureServiceRunning() {
        return isServiceRunning(GestureService.class.getName());
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