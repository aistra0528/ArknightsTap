package com.icebem.akt.object;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.icebem.akt.R;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class HRViewer {
    private static final int TAG_MIN = 1;
    private static final int TAG_MAX = 5;
    private static final int[] CHECKED_STARS_ID = {R.id.tag_star_3, R.id.tag_star_4, R.id.tag_star_5};
    private Context context;
    private ViewGroup root, resultContainer;
    private CharacterInfo[] infos;
    private ArrayList<String> checkedStars, checkedQualifications, checkedPositions, checkedSexes, checkedTypes, checkedTags;
    private ArrayList<CheckBox> stars, qualifications, positions, sexes, types, tags;
    private ArrayList<CharacterInfo> checkedInfos;

    public HRViewer(Context context, ViewGroup root) throws IOException, JSONException {
        this.context = context;
        this.root = root;
        resultContainer = root.findViewById(R.id.container_hr_result);
        stars = findBoxesById(R.id.tag_star_1);
        qualifications = findBoxesById(R.id.tag_qualification_1);
        positions = findBoxesById(R.id.tag_position_1);
        sexes = findBoxesById(R.id.tag_sex_1);
        types = findBoxesById(R.id.tag_type_1);
        tags = findBoxesById(R.id.tag_tags_1);
        setOnCheckedChangeListener(stars);
        setOnCheckedChangeListener(qualifications);
        setOnCheckedChangeListener(positions);
        setOnCheckedChangeListener(sexes);
        setOnCheckedChangeListener(types);
        setOnCheckedChangeListener(tags);
        infos = CharacterInfo.fromAssets(context);
        checkedStars = new ArrayList<>();
        checkedQualifications = new ArrayList<>();
        checkedPositions = new ArrayList<>();
        checkedSexes = new ArrayList<>();
        checkedTypes = new ArrayList<>();
        checkedTags = new ArrayList<>();
        checkedInfos = new ArrayList<>();
        setCheckedStars();
    }

    private CheckBox findBoxById(int id) {
        return root.findViewById(id);
    }

    private ArrayList<CheckBox> findBoxesById(int id) {
        ArrayList<CheckBox> list = new ArrayList<>();
        ViewGroup group = (ViewGroup) root.findViewById(id).getParent();
        for (int i = 0; i < group.getChildCount(); i++)
            if (group.getChildAt(i) instanceof CheckBox)
                list.add((CheckBox) group.getChildAt(i));
        return list;
    }

    private void setOnCheckedChangeListener(ArrayList<CheckBox> boxes) {
        for (CheckBox box : boxes)
            box.setOnCheckedChangeListener(this::onCheckedChange);
    }

    private void onCheckedChange(CompoundButton tag, boolean isChecked) {
        if (tag instanceof CheckBox) {
            if (stars.contains(tag)) {
                if (!isChecked && checkedStars.size() == TAG_MIN) {
                    tag.setChecked(true);
                } else {
                    if (tag.getId() == R.id.tag_star_6 && findBoxById(R.id.tag_qualification_3).isChecked() != isChecked)
                        findBoxById(R.id.tag_qualification_3).setChecked(isChecked);
                    updateCheckedInfos((CheckBox) tag, isChecked);
                }
            } else if (isChecked && checkedQualifications.size() + checkedPositions.size() + checkedSexes.size() + checkedTypes.size() + checkedTags.size() >= TAG_MAX) {
                tag.setChecked(false);
            } else {
                if (qualifications.contains(tag)) {
                    if (tag.getId() == R.id.tag_qualification_1 && isChecked && !findBoxById(R.id.tag_star_2).isChecked())
                        findBoxById(R.id.tag_star_2).setChecked(true);
                    else if (tag.getId() == R.id.tag_qualification_2 && isChecked && !findBoxById(R.id.tag_star_5).isChecked())
                        findBoxById(R.id.tag_star_5).setChecked(true);
                    else if (tag.getId() == R.id.tag_qualification_3 && findBoxById(R.id.tag_star_6).isChecked() != isChecked)
                        findBoxById(R.id.tag_star_6).setChecked(isChecked);
                }
                updateCheckedTags((CheckBox) tag, isChecked);
            }
        }
    }

    private void setCheckedStars() {
        for (int i = 0; i < stars.size(); i++)
            stars.get(i).setChecked(Arrays.binarySearch(CHECKED_STARS_ID, stars.get(i).getId()) >= 0);
    }

    private void updateCheckedTags(CheckBox tag, boolean isChecked) {
        if (qualifications.contains(tag)) {
            if (isChecked)
                checkedQualifications.add(tag.getText().toString());
            else
                checkedQualifications.remove(tag.getText().toString());
            Log.d(getClass().getSimpleName(), checkedQualifications.toString());
        } else if (positions.contains(tag)) {
            if (isChecked)
                checkedPositions.add(tag.getText().toString());
            else
                checkedPositions.remove(tag.getText().toString());
            Log.d(getClass().getSimpleName(), checkedPositions.toString());
        } else if (sexes.contains(tag)) {
            if (isChecked)
                checkedSexes.add(tag.getText().toString());
            else
                checkedSexes.remove(tag.getText().toString());
            Log.d(getClass().getSimpleName(), checkedSexes.toString());
        } else if (types.contains(tag)) {
            if (isChecked)
                checkedTypes.add(tag.getText().toString());
            else
                checkedTypes.remove(tag.getText().toString());
            Log.d(getClass().getSimpleName(), checkedTypes.toString());
        } else if (tags.contains(tag)) {
            if (isChecked)
                checkedTags.add(tag.getText().toString());
            else
                checkedTags.remove(tag.getText().toString());
            Log.d(getClass().getSimpleName(), checkedTags.toString());
        }
        updateHRResult();
    }

    private void updateCheckedInfos(CheckBox star, boolean isChecked) {
        if (isChecked)
            checkedStars.add(star.getText().toString());
        else
            checkedStars.remove(star.getText().toString());
        for (CharacterInfo info : infos) {
            if (star.getText().toString().contains(String.valueOf(info.getStar()))) {
                if (isChecked)
                    checkedInfos.add(info);
                else
                    checkedInfos.remove(info);
            }
        }
        updateHRResult();
    }

    private void updateHRResult() {
        resultContainer.removeAllViews();
        HorizontalScrollView scroll = new HorizontalScrollView(context);
        LinearLayout layout = new LinearLayout(context);
        for (CharacterInfo info : checkedInfos) {
            layout.addView(getInfoView(info, layout));
        }
        scroll.addView(layout);
        resultContainer.addView(scroll);
    }

    private TextView getInfoView(CharacterInfo info, ViewGroup container) {
        TextView view = (TextView) LayoutInflater.from(context).inflate(R.layout.tag_overlay, container, false);
        view.setText(info.getName());
        switch (info.getStar()) {
            case 1:
                view.setBackgroundResource(R.drawable.bg_tag_star_1);
                break;
            case 2:
                view.setBackgroundResource(R.drawable.bg_tag_star_2);
                break;
            case 3:
                view.setBackgroundResource(R.drawable.bg_tag_star_3);
                break;
            case 4:
                view.setBackgroundResource(R.drawable.bg_tag_star_4);
                break;
            case 5:
                view.setBackgroundResource(R.drawable.bg_tag_star_5);
                break;
            case 6:
                view.setBackgroundResource(R.drawable.bg_tag_star_6);
                break;
        }
        return view;
    }
}