package com.icebem.akt.model

import android.view.View
import android.widget.TextView
import com.icebem.akt.R
import com.icebem.akt.app.PreferenceManager

class HeadhuntCounter(private val manager: PreferenceManager, root: View) {
    private var limited: Boolean = manager.getHeadhuntCount(true) > 0

    init {
        val title = root.findViewById<TextView>(R.id.txt_counter_title)
        title.setText(if (limited) R.string.counter_limited else R.string.counter_normal)
        val tip = root.findViewById<TextView>(R.id.txt_counter_tips)
        root.findViewById<View>(R.id.action_toggle).setOnClickListener { toggle(title, tip) }
        root.findViewById<View>(R.id.action_minus).run {
            setOnClickListener { update(tip, manager.getHeadhuntCount(limited), -1) }
            setOnLongClickListener { update(tip, 0, 0) }
        }
        root.findViewById<View>(R.id.action_plus).run {
            setOnClickListener { update(tip, manager.getHeadhuntCount(limited), 1) }
            setOnLongClickListener { update(tip, manager.getHeadhuntCount(limited), 10) }
        }
        update(tip, manager.getHeadhuntCount(limited), 0)
    }

    private fun toggle(title: TextView, tip: TextView) {
        limited = !limited
        title.setText(if (limited) R.string.counter_limited else R.string.counter_normal)
        update(tip, manager.getHeadhuntCount(limited), 0)
    }

    private fun update(tip: TextView, count: Int, delta: Int): Boolean {
        var mCount = count
        mCount += delta
        if (mCount < 0) mCount += 99 else if (mCount >= 99) mCount -= 99
        manager.setHeadhuntCount(mCount, limited)
        var possibility = 2
        if (mCount > 49) possibility += mCount - 49 shl 1
        tip.text = tip.context.getString(R.string.tip_counter_default, mCount, possibility)
        return true
    }
}