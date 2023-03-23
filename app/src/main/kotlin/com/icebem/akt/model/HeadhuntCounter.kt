package com.icebem.akt.model

import android.widget.TextView
import com.icebem.akt.R
import com.icebem.akt.databinding.OverlayCounterBinding
import com.icebem.akt.util.ArkPref

class HeadhuntCounter(binding: OverlayCounterBinding) {
    private var limited: Boolean = ArkPref.getHeadhuntCount(true) > 0

    init {
        binding.txtCounterTitle.setText(if (limited) R.string.counter_limited else R.string.counter_normal)
        binding.actionToggle.setOnClickListener { toggle(binding.txtCounterTitle, binding.txtCounterTips) }
        binding.actionMinus.run {
            setOnClickListener { update(binding.txtCounterTips, ArkPref.getHeadhuntCount(limited), -1) }
            setOnLongClickListener { update(binding.txtCounterTips, 0, 0) }
        }
        binding.actionPlus.run {
            setOnClickListener { update(binding.txtCounterTips, ArkPref.getHeadhuntCount(limited), 1) }
            setOnLongClickListener { update(binding.txtCounterTips, ArkPref.getHeadhuntCount(limited), 10) }
        }
        update(binding.txtCounterTips, ArkPref.getHeadhuntCount(limited), 0)
    }

    private fun toggle(title: TextView, tips: TextView) {
        limited = !limited
        title.setText(if (limited) R.string.counter_limited else R.string.counter_normal)
        update(tips, ArkPref.getHeadhuntCount(limited), 0)
    }

    private fun update(tip: TextView, count: Int, delta: Int): Boolean {
        var mCount = count
        mCount += delta
        if (mCount < 0) mCount += 99 else if (mCount >= 99) mCount -= 99
        ArkPref.setHeadhuntCount(mCount, limited)
        var possibility = 2
        if (mCount > 49) possibility += mCount - 49 shl 1
        tip.text = tip.context.getString(R.string.tip_counter_default, mCount, possibility)
        return true
    }
}