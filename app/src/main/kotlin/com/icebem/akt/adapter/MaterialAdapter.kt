package com.icebem.akt.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.icebem.akt.R
import com.icebem.akt.model.MaterialInfo
import com.icebem.akt.overlay.OverlayToast
import com.icebem.akt.util.ArkPref
import com.icebem.akt.util.Resolution

class MaterialAdapter : RecyclerView.Adapter<MaterialAdapter.ViewHolder>() {
    companion object {
        const val COUNT_SPAN = 6

        private const val RES_START_MTL = "mtl_"
        private const val RES_TYPE = "mipmap"
        private val mtlId = intArrayOf(30011, 30021, 30031, 30041, 30051, 30061, 30012, 30022, 30032, 30042, 30052, 30062, 30013, 30023, 30033, 30043, 30053, 30063, 30014, 30024, 30034, 30044, 30054, 30064, 30073, 30074, 30083, 30084, 30093, 30094, 30103, 30104, 31013, 31014, 31023, 31024, 31033, 31034, 31043, 31044, 31053, 31054, 31063, 31064)
    }

    class ViewHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView)

    private val infos: Array<MaterialInfo> = MaterialInfo.array

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ImageView(parent.context)
        var size = view.context.resources.getDimensionPixelOffset(R.dimen.control_padding) shl 1
        size = (Resolution.physicalHeight - size) / COUNT_SPAN
        view.layoutParams = ViewGroup.LayoutParams(size, size)
        return ViewHolder(view)
    }

    @SuppressLint("DiscouragedApi")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = findMaterialById(mtlId[position]) ?: return
        val view = holder.itemView as ImageView
        view.setImageResource(view.resources.getIdentifier(RES_START_MTL + info.id, RES_TYPE, view.context.packageName))
        view.setOnClickListener {
            val str = buildString {
                if (info.items.isEmpty()) {
                    append(info.getName(ArkPref.translationIndex))
                } else {
                    append(it.context.getString(R.string.tip_material_workshop, info.getName(ArkPref.translationIndex)))
                    for (item in info.items) {
                        val mtl = findMaterialById(item.id) ?: break
                        appendLine()
                        append(it.context.getString(R.string.tip_material_item, mtl.getName(ArkPref.translationIndex), item.quantity))
                    }
                }
                if (info.stages.isNotEmpty()) {
                    for (mission in info.stages) {
                        appendLine()
                        val sanity = mission.sanity
                        val frequency = mission.frequency
                        append(it.context.getString(R.string.tip_material_mission, mission.mission, frequency * 100, sanity / frequency))
                    }
                }
            }
            OverlayToast.show(str, OverlayToast.LENGTH_LONG)
        }
    }

    private fun findMaterialById(id: Int): MaterialInfo? {
        for (info in infos) if (info.id == id) return info
        return null
    }

    override fun getItemCount(): Int = mtlId.size
}