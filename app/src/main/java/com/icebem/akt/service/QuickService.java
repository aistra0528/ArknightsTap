package com.icebem.akt.service;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.icebem.akt.R;
import com.icebem.akt.app.BaseApplication;

@RequiresApi(api = Build.VERSION_CODES.N)
public class QuickService extends TileService {
    @Override
    public void onStartListening() {
        super.onStartListening();
        getQsTile().setIcon(Icon.createWithResource(this, Settings.canDrawOverlays(this) ? R.drawable.ic_akt : R.drawable.ic_error_outline));
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
                ContextCompat.startForegroundService(this, intent);
                getQsTile().setState(Tile.STATE_ACTIVE);
            }
            getQsTile().updateTile();
        } else
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}