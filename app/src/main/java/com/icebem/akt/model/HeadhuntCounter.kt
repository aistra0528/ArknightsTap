package com.icebem.akt.model;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.icebem.akt.R;
import com.icebem.akt.app.PreferenceManager;

public class HeadhuntCounter {
    private boolean limited;
    private PreferenceManager manager;

    public HeadhuntCounter(PreferenceManager manager, View root) {
        this.manager = manager;
        TextView title = root.findViewById(R.id.txt_counter_title);
        limited = manager.getHeadhuntCount(true) > 0;
        title.setText(limited ? R.string.counter_limited : R.string.counter_normal);
        TextView tip = root.findViewById(R.id.txt_counter_tips);
        root.findViewById(R.id.action_toggle).setOnClickListener(v -> toggle(title, tip));
        ImageButton minus = root.findViewById(R.id.action_minus);
        minus.setOnClickListener(v -> update(tip, manager.getHeadhuntCount(limited), -1));
        minus.setOnLongClickListener(v -> update(tip, 0, 0));
        ImageButton plus = root.findViewById(R.id.action_plus);
        plus.setOnClickListener(v -> update(tip, manager.getHeadhuntCount(limited), 1));
        plus.setOnLongClickListener(v -> update(tip, manager.getHeadhuntCount(limited), 10));
        update(tip, manager.getHeadhuntCount(limited), 0);
    }

    private void toggle(TextView title, TextView tip) {
        limited = !limited;
        title.setText(limited ? R.string.counter_limited : R.string.counter_normal);
        update(tip, manager.getHeadhuntCount(limited), 0);
    }

    private boolean update(TextView tip, int count, int delta) {
        count += delta;
        if (count < 0)
            count += 99;
        else if (count >= 99)
            count -= 99;
        manager.setHeadhuntCount(count, limited);
        int possibility = 2;
        if (count > 49)
            possibility += count - 49 << 1;
        tip.setText(tip.getContext().getString(R.string.tip_counter_default, count, possibility));
        return true;
    }
}
