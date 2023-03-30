package com.icebem.akt.service

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.icebem.akt.R
import com.icebem.akt.util.ArkMaid

@RequiresApi(Build.VERSION_CODES.N)
class QSTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        qsTile.icon = Icon.createWithResource(this, if (ArkMaid.requireOverlayPermission) R.drawable.ic_error else R.drawable.ic_akt)
        qsTile.label = getString(if (ArkMaid.requireOverlayPermission) R.string.state_permission_request else R.string.overlay_label)
        qsTile.state = if (ArkMaid.isOverlayServiceRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (ArkMaid.requireOverlayPermission) {
            ArkMaid.startManageOverlay(this)
        } else {
            val intent = Intent(this, OverlayService::class.java)
            if (ArkMaid.isOverlayServiceRunning) {
                stopService(intent)
                qsTile.state = Tile.STATE_INACTIVE
            } else {
                ContextCompat.startForegroundService(this, intent)
                qsTile.state = Tile.STATE_ACTIVE
            }
            qsTile.updateTile()
        }
    }
}