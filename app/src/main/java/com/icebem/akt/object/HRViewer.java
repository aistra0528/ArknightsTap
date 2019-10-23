package com.icebem.akt.object;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.icebem.akt.R;

public class HRViewer {
    private CheckBox[] stars, qualifications;

    public HRViewer(View view) {
        stars = new CheckBox[6];
        stars[0] = view.findViewById(R.id.tag_star_1);
        stars[1] = view.findViewById(R.id.tag_star_2);
        stars[2] = view.findViewById(R.id.tag_star_3);
        stars[3] = view.findViewById(R.id.tag_star_4);
        stars[4] = view.findViewById(R.id.tag_star_5);
        stars[5] = view.findViewById(R.id.tag_star_6);
        setOnCheckedChangeListener(stars);
        qualifications = new CheckBox[3];
        qualifications[0] = view.findViewById(R.id.tag_qualification_1);
        qualifications[1] = view.findViewById(R.id.tag_qualification_2);
        qualifications[2] = view.findViewById(R.id.tag_qualification_3);
        setOnCheckedChangeListener(qualifications);
    }

    private void setOnCheckedChangeListener(CheckBox[] boxes) {
        for (CheckBox box : boxes) {
            box.setOnCheckedChangeListener(this::onCheckedChange);
        }
    }

    private void onCheckedChange(CompoundButton button, boolean isChecked) {
        switch (button.getId()) {
            case R.id.tag_star_6:
                if (qualifications[2].isChecked() != isChecked) {
                    qualifications[2].setChecked(isChecked);
                }
                break;
            case R.id.tag_qualification_1:
                if (isChecked && !stars[1].isChecked()) {
                    stars[1].setChecked(true);
                }
                break;
            case R.id.tag_qualification_2:
                if (isChecked && !stars[4].isChecked()) {
                    stars[4].setChecked(true);
                }
                break;
            case R.id.tag_qualification_3:
                if (stars[5].isChecked() != isChecked) {
                    stars[5].setChecked(isChecked);
                }
                break;
        }
    }
}