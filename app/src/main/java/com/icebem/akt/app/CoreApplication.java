package com.icebem.akt.app;

import android.app.ActivityManager;
import android.app.Application;
import android.os.Build;

import com.icebem.akt.service.CoreService;

public class CoreApplication extends Application {
    private CoreService service;

    public void setCoreService(CoreService service) {
        this.service = service;
    }

    public CoreService getCoreService() {
        return service;
    }

    public boolean isCoreServiceEnabled() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (am != null)
            //getRunningServices方法在Android 8.0已过时，只会获取应用自身正在运行的服务，所以列表条数最大值不用太大
            for (ActivityManager.RunningServiceInfo info : am.getRunningServices(Build.VERSION.SDK_INT < Build.VERSION_CODES.O ? Integer.MAX_VALUE : 5)) {
                if (info.service.getClassName().equals(CoreService.class.getName())) {
                    return true;
                }
            }
        return false;
    }
}