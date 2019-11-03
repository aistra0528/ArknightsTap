package com.icebem.akt.object;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
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
    private static final int TAG_COMBINED_MAX = 3;
    private static final int[] CHECKED_STARS_ID = {R.id.tag_star_3, R.id.tag_star_4, R.id.tag_star_5};
    private int minStar;
    private Context context;
    private TextView tip;
    private ViewGroup root, resultContainer;
    private CharacterInfo[] infos;
    private ArrayList<CheckBox> stars, qualifications, sexes, types, checkedStars, checkedTags, combinedTags;
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
        LinearLayout tagsContainer = root.findViewById(R.id.container_hr_tags);
        tip.setOnClickListener(view -> tagsContainer.setVisibility(tagsContainer.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE));
        root.findViewById(R.id.action_hr_reset).setOnClickListener(view -> resetTags());
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
        combinedTags = new ArrayList<>();
        resetTags();
    }

    private CheckBox findBoxById(int id) {
        return root.findViewById(id);
    }

    private ArrayList<CheckBox> findBoxesById(int id) {
        ArrayList<CheckBox> boxes = new ArrayList<>();
        ViewGroup group = (ViewGroup) root.findViewById(id).getParent();
        for (int i = 0; i < group.getChildCount(); i++)
            if (group.getChildAt(i) instanceof CheckBox)
                boxes.add((CheckBox) group.getChildAt(i));
        return boxes;
    }

    private void setOnCheckedChangeListener(ArrayList<CheckBox> boxes) {
        for (CheckBox box : boxes)
            box.setOnCheckedChangeListener(this::onCheckedChange);
    }

    private void onCheckedChange(CompoundButton tag, boolean isChecked) {
        if (tag instanceof CheckBox) {
            if (stars.contains(tag)) {
                if (!isChecked) {
                    if (checkedStars.size() == TAG_STAR_MIN)
                        tag.setChecked(true);
                    else if (tag.getId() == R.id.tag_star_6 && findBoxById(R.id.tag_qualification_3).isChecked())
                        findBoxById(R.id.tag_qualification_3).setChecked(false);
                } else {
                    updateCheckedTags((CheckBox) tag, isChecked);
                }
            } else if (isChecked && checkedTags.size() >= TAG_CHECKED_MAX) {
                tag.setChecked(false);
            } else {
                if (qualifications.contains(tag) && isChecked) {
                    if (tag.getId() == R.id.tag_qualification_1 && !findBoxById(R.id.tag_star_2).isChecked())
                        findBoxById(R.id.tag_star_2).setChecked(true);
                    else if (tag.getId() == R.id.tag_qualification_2 && !findBoxById(R.id.tag_star_5).isChecked())
                        findBoxById(R.id.tag_star_5).setChecked(true);
                    else if (tag.getId() == R.id.tag_qualification_3 && !findBoxById(R.id.tag_star_6).isChecked())
                        findBoxById(R.id.tag_star_6).setChecked(true);
                }
                updateCheckedTags((CheckBox) tag, isChecked);
            }
        }
    }

    private void resetTags() {
        for (CheckBox box : stars)
            box.setChecked(Arrays.binarySearch(CHECKED_STARS_ID, box.getId()) >= 0);
        if (checkedTags.size() > 0) {
            CheckBox[] boxes = new CheckBox[checkedTags.size()];
            for (int i = 0; i < boxes.length; i++)
                boxes[i] = checkedTags.get(i);
            for (CheckBox box : boxes)
                box.setChecked(false);
        }
    }

    private void updateCheckedTags(CheckBox tag, boolean isChecked) {
        if (stars.contains(tag)) {
            if (isChecked)
                checkedStars.add(tag);
            else
                checkedStars.remove(tag);
            for (CharacterInfo info : infos) {
                if (tag.getText().toString().contains(String.valueOf(info.getStar()))) {
                    if (isChecked)
                        checkedInfos.add(info);
                    else
                        checkedInfos.remove(info);
                }
            }
            Collections.sort(checkedInfos);
        } else {
            if (isChecked)
                checkedTags.add(tag);
            else
                checkedTags.remove(tag);
        }
        updateHRResult();
    }

    private void updateHRResult() {
        resultContainer.removeAllViews();
        if (checkedTags.size() == 0) {
            HorizontalScrollView scroll = (HorizontalScrollView) LayoutInflater.from(context).inflate(R.layout.scroll_overlay, resultContainer, false);
            LinearLayout layout = new LinearLayout(context);
            for (CharacterInfo info : checkedInfos)
                layout.addView(getInfoView(info, layout));
            scroll.addView(layout);
            resultContainer.addView(scroll);
            tip.setText(R.string.tip_hr_result_normal);
        } else {
            minStar = 0;
            for (int i = Math.min(checkedTags.size(), TAG_COMBINED_MAX); i > 0; i--)
                combineTags(0, combinedTags.size(), i);
            switch (minStar) {
                case 6:
                    tip.setText(R.string.tip_hr_result_excellent);
                    tip.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                    tip.setMarqueeRepeatLimit(-1);
                    tip.setSingleLine(true);
                    tip.setSelected(true);
                    tip.setFocusable(true);
                    tip.setFocusableInTouchMode(true);
                    break;
                case 5:
                    tip.setText(R.string.tip_hr_result_great);
                    break;
                case 4:
                    tip.setText(R.string.tip_hr_result_good);
                    break;
                case 0:
                    tip.setText(R.string.tip_hr_result_none);
                    break;
                default:
                    tip.setText(R.string.tip_hr_result_normal);
            }
        }
    }

    private void combineTags(int index, int size, int targetSize) {
        if (size == targetSize) {
            matchInfos();
        } else {
            for (int i = index; i < checkedTags.size(); i++) {
                if (!combinedTags.contains(checkedTags.get(i))) {
                    combinedTags.add(checkedTags.get(i));
                    combineTags(i, combinedTags.size(), targetSize);
                    combinedTags.remove(checkedTags.get(i));
                }
            }
        }
    }

    private void matchInfos() {
        ArrayList<CharacterInfo> matchedInfos = new ArrayList<>();
        for (CharacterInfo info : checkedInfos) {
            boolean matched = info.getStar() != 6 || findBoxById(R.id.tag_qualification_3).isChecked();
            for (CheckBox tag : combinedTags) {
                if (matched) {
                    if (qualifications.contains(tag)) {
                        matched = (tag.getId() == R.id.tag_qualification_1 && info.getStar() == 2) || (tag.getId() == R.id.tag_qualification_2 && info.getStar() == 5) || (tag.getId() == R.id.tag_qualification_3 && info.getStar() == 6);
                    } else if (sexes.contains(tag)) {
                        matched = tag.getText().toString().equals(info.getSex());
                    } else if (types.contains(tag)) {
                        matched = tag.getText().toString().equals(info.getType());
                    } else {
                        matched = info.containsTag(tag.getText().toString());
                    }
                } else break;
            }
            if (matched) matchedInfos.add(info);
        }
        if (matchedInfos.size() > 0) addResult(matchedInfos);
    }

    private void addResult(ArrayList<CharacterInfo> infos) {
        LinearLayout tagContainer = new LinearLayout(context);
        for (CheckBox box : combinedTags)
            tagContainer.addView(getTagView(box, tagContainer));
        HorizontalScrollView scroll = (HorizontalScrollView) LayoutInflater.from(context).inflate(R.layout.scroll_overlay, resultContainer, false);
        LinearLayout infoContainer = new LinearLayout(context);
        for (CharacterInfo info : infos)
            infoContainer.addView(getInfoView(info, infoContainer));
        scroll.addView(infoContainer);
        resultContainer.addView(tagContainer);
        resultContainer.addView(scroll);
        minStar = Math.max(minStar, infos.get(infos.size() - 1).getStar());
    }

    private TextView getTagView(CheckBox box, ViewGroup container) {
        TextView view = (TextView) LayoutInflater.from(context).inflate(R.layout.tag_overlay, container, false);
        view.setText(box.getText());
        switch (box.getId()) {
            case R.id.tag_qualification_1:
                view.setBackgroundResource(R.drawable.bg_tag_star_2);
                break;
            case R.id.tag_qualification_2:
                view.setBackgroundResource(R.drawable.bg_tag_star_5);
                break;
            case R.id.tag_qualification_3:
                view.setBackgroundResource(R.drawable.bg_tag_star_6);
                break;
        }
        return view;
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