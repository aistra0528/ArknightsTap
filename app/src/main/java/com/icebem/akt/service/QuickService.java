package com.icebem.akt.service;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.icebem.akt.R;
import com.icebem.akt.app.BaseApplication;

public class QuickService extends TileService {
    @Override
    public void onStartListening() {
        super.onStartListening();
        getQsTile().setIcon(Icon.createWithResource(this, Settings.canDrawOverlays(this) ? R.drawable.ic_fab_akt : R.drawable.ic_error_outline));
        getQsTile().setLabel(Settings.canDrawOverlays(this) ? getString(R.string.overlay_label) : getString(R.string.state_permission_request));
        getQsTile().setState(((BaseApplication) getApplication()).isOverlayServiceRunning() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        getQsTile().updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        if (Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(this, OverlayService.class);
            if (((BaseApplication) getApplication()).isOverlayServiceRunning()) {
                stopService(intent);
                getQsTile().setState(Tile.STATE_INACTIVE);
            } else {
                startService(intent);
                getQsTile().setState(Tile.STATE_ACTIVE);
            }
            getQsTile().updateTile();
        } else
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}