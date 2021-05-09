package com.icebem.akt.adapter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.icebem.akt.R
import com.icebem.akt.app.PreferenceManager
import com.icebem.akt.app.ResolutionConfig
import com.icebem.akt.model.MaterialInfo
import com.icebem.akt.overlay.OverlayToast

class MaterialAdapter(private val manager: PreferenceManager, private val spanCount: Int) : RecyclerView.Adapter<MaterialAdapter.ViewHolder>() {
    companion object {
        private const val RES_START_MTL = "mtl_"
        private const val RES_START_BG = "bg_mtl_t"
        private const val RES_TYPE = "mipmap"
        private val mtlId = intArrayOf(
                30011, 30021, 30031, 30041, 30051, 30061,
                30012, 30022, 30032, 30042, 30052, 30062,
                30013, 30023, 30033, 30043, 30053, 30063,
                30014, 30024, 30034, 30044, 30054, 30064,
                30073, 30074, 30083, 30084, 30093, 30094,
                30103, 30104, 31013, 31014, 31023, 31024,
                31033, 31034
        )
    }

    class ViewHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView)

    private val infoList: Array<MaterialInfo> = MaterialInfo.load(manager.applicationContext)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ImageView(parent.context)
        var size = view.context.resources.getDimensionPixelOffset(R.dimen.control_padding) shl 1
        size = (ResolutionConfig.getAbsoluteHeight(view.context) - size) / spanCount
        view.layoutParams = ViewGroup.LayoutParams(size, size)
        size = size shr 4
        view.setPadding(size, size, size, size)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = findMaterialById(mtlId[position]) ?: return
        val view = holder.itemView as ImageView
        view.setBackgroundResource(view.resources.getIdentifier(RES_START_BG + info.star, RES_TYPE, view.context.packageName))
        view.setImageResource(view.resources.getIdentifier(RES_START_MTL + info.id, RES_TYPE, view.context.packageName))
        view.setOnClickListener {
            val builder = StringBuilder()
            if (info.items == null) {
                builder.append(info.getName(manager.translationIndex))
            } else {
                builder.append(it.context.getString(R.string.tip_material_workshop, info.getName(manager.translationIndex)))
                for (item in info.items) {
                    val mtl = findMaterialById(item.id) ?: break
                    builder.append(System.lineSeparator())
                    builder.append(it.context.getString(R.string.tip_material_item, mtl.getName(manager.translationIndex), item.quantity))
                }
            }
            if (info.stages != null) {
                for (mission in info.stages) {
                    builder.append(System.lineSeparator())
                    val sanity = mission.sanity
                    val frequency = mission.frequency
                    builder.append(it.context.getString(R.string.tip_material_mission, mission.mission, frequency * 100, sanity / frequency))
                }
            }
            OverlayToast.show(it.context, builder.toString(), OverlayToast.LENGTH_LONG)
        }
    }

    private fun findMaterialById(id: Int): MaterialInfo? {
        for (info in infoList)
            if (info.id == id) return info
        return null
    }

    override fun getItemCount(): Int = mtlId.size
}