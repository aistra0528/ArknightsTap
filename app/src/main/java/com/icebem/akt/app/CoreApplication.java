package com.icebem.akt.app;

import android.accessibilityservice.AccessibilityService;
import android.app.Application;

public class CoreApplication extends Application {
    private AccessibilityService service;

    public void setAccessibilityService(AccessibilityService service) {
        this.service = service;
    }

    public AccessibilityService getAccessibilityService() {
        return service;
    }
}