package com.icebem.akt.object;

import android.content.Context;
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
import java.util.Collections;

public class HRViewer {
    private static final int TAG_STAR_MIN = 1;
    private static final int TAG_CHECKED_MAX = 5;
    private static final int[] CHECKED_STARS_ID = {R.id.tag_star_3, R.id.tag_star_4, R.id.tag_star_5};
    private Context context;
    private TextView tip;
    private ViewGroup root, resultContainer;
    private CharacterInfo[] infos;
    private ArrayList<CheckBox> stars, qualifications, sexes, types, checkedStars, checkedTags;
    private ArrayList<CharacterInfo> checkedInfos;

    public HRViewer(Context context, ViewGroup root) throws IOException, JSONException {
        this.context = context;
        this.root = root;
        tip = root.findViewById(R.id.txt_hr_tips);
        resultContainer = root.findViewById(R.id.container_hr_result);
        stars = findBoxesById(R.id.tag_star_1);
        qualifications = findBoxesById(R.id.tag_qualification_1);
        sexes = findBoxesById(R.id.tag_sex_1);
        types = findBoxesById(R.id.tag_type_1);
        setOnCheckedChangeListener(stars);
        setOnCheckedChangeListener(qualifications);
        setOnCheckedChangeListener(findBoxesById(R.id.tag_position_1));
        setOnCheckedChangeListener(sexes);
        setOnCheckedChangeListener(types);
        setOnCheckedChangeListener(findBoxesById(R.id.tag_tags_1));
        infos = CharacterInfo.fromAssets(context);
        checkedStars = new ArrayList<>();
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
                if (!isChecked && checkedStars.size() == TAG_STAR_MIN) {
                    tag.setChecked(true);
                } else {
                    if (tag.getId() == R.id.tag_star_6 && findBoxById(R.id.tag_qualification_3).isChecked() != isChecked)
                        findBoxById(R.id.tag_qualification_3).setChecked(isChecked);
                    updateCheckedInfos((CheckBox) tag, isChecked);
                }
            } else if (isChecked && checkedTags.size() >= TAG_CHECKED_MAX) {
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
        if (isChecked)
            checkedTags.add(tag);
        else
            checkedTags.remove(tag);
        updateHRResult();
    }

    private void updateCheckedInfos(CheckBox star, boolean isChecked) {
        if (isChecked)
            checkedStars.add(star);
        else
            checkedStars.remove(star);
        for (CharacterInfo info : infos) {
            if (star.getText().toString().contains(String.valueOf(info.getStar()))) {
                if (isChecked)
                    checkedInfos.add(info);
                else
                    checkedInfos.remove(info);
            }
        }
        Collections.sort(checkedInfos);
        updateHRResult();
    }

    private void updateHRResult() {
        resultContainer.removeAllViews();
        if (checkedTags.size() == 0) {
            HorizontalScrollView scroll = (HorizontalScrollView) LayoutInflater.from(context).inflate(R.layout.scroll_overlay, resultContainer, false);
            LinearLayout layout = new LinearLayout(context);
            for (CharacterInfo info : checkedInfos) {
                layout.addView(getInfoView(info, layout));
            }
            scroll.addView(layout);
            resultContainer.addView(scroll);
            tip.setText(R.string.tip_hr_result_1);
        } else {
            updateMatchedResult();
        }
    }

    private void updateMatchedResult() {
        ArrayList<CharacterInfo> matchedInfos = new ArrayList<>();
        for (CharacterInfo info : checkedInfos) {
            boolean matched = true;
            for (CheckBox tag : checkedTags) {
                if (matched) {
                    if (qualifications.contains(tag)) {
                        matched = (tag.getId() == R.id.tag_qualification_1 && info.getStar() == 2) || (tag.getId() == R.id.tag_qualification_2 && info.getStar() == 5) || (tag.getId() == R.id.tag_qualification_3 && info.getStar() == 6);
                    } else if (sexes.contains(tag)) {
                        matched = tag.getText().toString().equals(info.getSex());
                    } else if (types.contains(tag)) {
                        matched = tag.getText().toString().equals(info.getType());
                    } else {
                        matched = info.includeTag(tag.getText().toString());
                    }
                }
            }
            if (matched) matchedInfos.add(info);
        }
        HorizontalScrollView scroll = new HorizontalScrollView(context);
        LinearLayout layout = new LinearLayout(context);
        for (CharacterInfo info : matchedInfos) {
            layout.addView(getInfoView(info, layout));
        }
        scroll.addView(layout);
        resultContainer.addView(scroll);
        if (matchedInfos.size() > 0) {
            switch (matchedInfos.get(matchedInfos.size() - 1).getStar()) {
                case 6:
                    tip.setText(R.string.tip_hr_result_4);
                    break;
                case 5:
                    tip.setText(R.string.tip_hr_result_3);
                    break;
                case 4:
                    tip.setText(R.string.tip_hr_result_2);
                    break;
                default:
                    tip.setText(R.string.tip_hr_result_1);
            }
        } else {
            tip.setText(R.string.tip_hr_result_0);
        }
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