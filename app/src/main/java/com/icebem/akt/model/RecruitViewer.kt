/*
 * This file is part of ArkTap.
 * Copyright (C) 2019-2021 艾星Aistra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.icebem.akt.model

import android.content.Context
import android.text.TextUtils
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.NestedScrollView
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.snackbar.Snackbar
import com.icebem.akt.R
import com.icebem.akt.activity.MainActivity
import com.icebem.akt.app.PreferenceManager
import com.icebem.akt.overlay.OverlayToast
import com.icebem.akt.util.DataUtil
import java.util.*

class RecruitViewer(private val context: Context, root: View) {
    companion object {
        private const val TAG_CHECKED_MAX = 5
        private const val TAG_COMBINED_MAX = 3
        private const val CHECKED_TIME_ID = R.id.tag_time_3
        private val CHECKED_STARS_ID = arrayOf(
                intArrayOf(R.id.tag_time_3, R.id.tag_star_3, R.id.tag_star_4, R.id.tag_star_5),
                intArrayOf(R.id.tag_time_2, R.id.tag_star_2, R.id.tag_star_3, R.id.tag_star_4, R.id.tag_star_5),
                intArrayOf(R.id.tag_time_1, R.id.tag_star_1, R.id.tag_star_2, R.id.tag_star_3, R.id.tag_star_4))
    }

    val manager: PreferenceManager = PreferenceManager.getInstance(context)
    val rootView: NestedScrollView = root.findViewById(R.id.scroll_recruit_root)
    private val top: CheckBox = findBoxById(R.id.tag_qualification_6)
    private val tip: TextView = rootView.findViewById(R.id.txt_recruit_tips)
    private val tagsContainer: ViewGroup = rootView.findViewById(R.id.container_recruit_tags)
    private val resultContainer: ViewGroup = rootView.findViewById(R.id.container_recruit_result)
    private val infoList: Array<OperatorInfo?> = OperatorInfo.load(context)
    private val tagArray: SparseArray<Array<String>> = RecruitTag.tagArray
    private val stars: ArrayList<CheckBox> = findBoxesById(R.id.tag_star_1)
    private val qualifications: ArrayList<CheckBox> = findBoxesById(R.id.tag_qualification_1)
    private val positions: ArrayList<CheckBox> = findBoxesById(R.id.tag_position_melee)
    private val types: ArrayList<CheckBox> = findBoxesById(R.id.tag_type_vanguard)
    private val affixes: ArrayList<CheckBox> = findBoxesById(R.id.tag_affix_survival)
    private val checkedStars: ArrayList<CheckBox> = ArrayList()
    private val checkedTags: ArrayList<CheckBox> = ArrayList()
    private val combinedTags: ArrayList<CheckBox> = ArrayList()
    private val checkedInfoList: ArrayList<OperatorInfo?> = ArrayList()
    private var index = 0
    private var autoAction = false
    private var resultList: ArrayList<ItemContainer>? = null

    init {
        setBoxesText()
        rootView.findViewById<View>(R.id.action_recruit_reset).setOnClickListener { resetTags(it) }
        tagsContainer.findViewById<RadioGroup>(R.id.group_recruit_time).setOnCheckedChangeListener { group, checkedId -> onCheckedChange(group, checkedId) }
        setOnCheckedChangeListener(stars)
        setOnCheckedChangeListener(qualifications)
        setOnCheckedChangeListener(positions)
        setOnCheckedChangeListener(types)
        setOnCheckedChangeListener(affixes)
        resetTags(null)
    }

    private fun findBoxById(id: Int): CheckBox = tagsContainer.findViewById(id)

    private fun findBoxesById(id: Int): ArrayList<CheckBox> {
        val boxes = ArrayList<CheckBox>()
        val group = tagsContainer.findViewById<View>(id).parent as ViewGroup
        for (i in 0 until group.childCount)
            if (group.getChildAt(i) is CheckBox) boxes.add(group.getChildAt(i) as CheckBox)
        return boxes
    }

    private fun setBoxesText() {
        index = manager.translationIndex
        for (box in stars) box.text = tagArray[box.id][index]
        for (box in qualifications) box.text = tagArray[box.id][index]
        for (box in positions) box.text = tagArray[box.id][index]
        for (box in types) box.text = tagArray[box.id][index]
        for (box in affixes) box.text = tagArray[box.id][index]
    }

    private fun setOnCheckedChangeListener(boxes: ArrayList<CheckBox>) {
        for (box in boxes) box.setOnCheckedChangeListener { tag, isChecked -> onCheckedChange(tag, isChecked) }
    }

    private fun onCheckedChange(group: RadioGroup, checkedId: Int) {
        if (group.id == R.id.group_recruit_time) {
            autoAction = true
            while (checkedStars.isNotEmpty()) checkedStars[0].isChecked = false
            for (stars in CHECKED_STARS_ID) {
                if (stars[0] == checkedId) {
                    for (i in 1 until stars.size) findBoxById(stars[i]).isChecked = true
                }
            }
            if (top.isChecked) findBoxById(R.id.tag_star_6).isChecked = true
            if (findBoxById(R.id.tag_qualification_5).isChecked && !findBoxById(R.id.tag_star_5).isChecked) findBoxById(R.id.tag_star_5).isChecked = true
            autoAction = false
            updateRecruitResult()
        }
    }

    private fun onCheckedChange(tag: CompoundButton, isChecked: Boolean) {
        if (tag is CheckBox) {
            if (!stars.contains(tag) && isChecked && checkedTags.size >= TAG_CHECKED_MAX) {
                tag.setChecked(false)
            } else {
                val state = autoAction
                autoAction = true
                if (tag === top && findBoxById(R.id.tag_star_6).isChecked != isChecked)
                    findBoxById(R.id.tag_star_6).isChecked = isChecked
                else if (tag.getId() == R.id.tag_qualification_5 && findBoxById(R.id.tag_star_5).isChecked != isChecked
                        && (isChecked || tagsContainer.findViewById<RadioButton>(R.id.tag_time_1).isChecked))
                    findBoxById(R.id.tag_star_5).isChecked = isChecked
                autoAction = state
                updateCheckedTags(tag, isChecked)
            }
        }
    }

    fun resetTags(view: View?) {
        if (view != null) view.isClickable = false
        autoAction = true
        while (checkedTags.isNotEmpty()) checkedTags[0].isChecked = false
        if (index != manager.translationIndex) setBoxesText()
        val timeTag = tagsContainer.findViewById<RadioButton>(CHECKED_TIME_ID)
        if (timeTag.isChecked)
            onCheckedChange(timeTag.parent as RadioGroup, CHECKED_TIME_ID)
        else
            timeTag.isChecked = true
        rootView.post { rootView.smoothScrollTo(0, if (context is MainActivity) 0 else (top.parent as ViewGroup).top) }
        if (view != null) view.isClickable = true
    }

    private fun updateCheckedTags(tag: CheckBox, isChecked: Boolean) {
        if (stars.contains(tag)) {
            if (isChecked) checkedStars.add(tag) else checkedStars.remove(tag)
            for (info in infoList) {
                if (tag.text.toString().contains(info!!.star.toString())) {
                    if (isChecked) checkedInfoList.add(info) else checkedInfoList.remove(info)
                }
            }
            checkedInfoList.sortWith { i1, i2 -> compareInfo(i1!!, i2!!) }
        } else {
            if (isChecked) checkedTags.add(tag) else checkedTags.remove(tag)
        }
        if (!autoAction) updateRecruitResult()
    }

    private fun updateRecruitResult() {
        resultContainer.removeAllViews()
        if (checkedTags.isEmpty()) {
            val flex = FlexboxLayout(context)
            flex.flexWrap = FlexWrap.WRAP
            for (info in checkedInfoList) if (hasPossibility(info)) flex.addView(getInfoView(info, flex))
            resultContainer.addView(flex)
            tip.setText(if (checkedInfoList.isEmpty()) R.string.tip_recruit_result_none else R.string.tip_recruit_result_default)
        } else {
            resultList = ArrayList()
            checkedTags.sortWith { t1, t2 -> compareTags(t1, t2) }
            for (i in checkedTags.size.coerceAtMost(TAG_COMBINED_MAX) downTo 1) combineTags(0, combinedTags.size, i)
            resultList!!.sort()
            for (container in resultList!!) resultContainer.addView(container)
            if (checkedTags.size == TAG_CHECKED_MAX && manager.scrollToResult) rootView.post { rootView.smoothScrollTo(0, tagsContainer.height) }
            tip.apply {
                when (if (resultList!!.isEmpty()) 0 else resultList!![0].minStar) {
                    6 -> {
                        setText(R.string.tip_recruit_result_6)
                        ellipsize = TextUtils.TruncateAt.MARQUEE
                        marqueeRepeatLimit = -1
                        isSingleLine = true
                        isSelected = true
                        isFocusable = true
                        isFocusableInTouchMode = true
                    }
                    5 -> setText(R.string.tip_recruit_result_5)
                    4 -> setText(R.string.tip_recruit_result_4)
                    0 -> setText(if (checkedTags.contains(findBoxById(R.id.tag_qualification_1))) R.string.tip_recruit_result_1 else R.string.tip_recruit_result_none)
                    else -> setText(if (checkedTags.contains(findBoxById(R.id.tag_qualification_1))) R.string.tip_recruit_result_1 else R.string.tip_recruit_result_default)
                }
            }
        }
    }

    private fun combineTags(index: Int, size: Int, targetSize: Int) {
        if (size == targetSize) {
            matchInfoList()
        } else {
            for (i in index until checkedTags.size) {
                if (!combinedTags.contains(checkedTags[i])) {
                    combinedTags.add(checkedTags[i])
                    combineTags(i, combinedTags.size, targetSize)
                    combinedTags.remove(checkedTags[i])
                }
            }
        }
    }

    private fun matchInfoList() {
        val matchedInfoList = ArrayList<OperatorInfo?>()
        for (info in checkedInfoList) {
            var matched = hasPossibility(info)
            for (tag in combinedTags) {
                matched = if (matched) {
                    when {
                        qualifications.contains(tag) -> {
                            tag.id == R.id.tag_qualification_1 && info!!.star == 1
                                    || tag.id == R.id.tag_qualification_2 && info!!.star == 2
                                    || tag.id == R.id.tag_qualification_5 && info!!.star == 5
                                    || tag === top && info!!.star == 6
                        }
                        types.contains(tag) -> info!!.type == tagArray[tag.id][0]
                        else -> info!!.containsTag(tagArray[tag.id][0])
                    }
                } else break
            }
            if (matched) matchedInfoList.add(info)
        }
        if (matchedInfoList.isNotEmpty()) addResultToList(matchedInfoList)
    }

    private fun addResultToList(matchedInfoList: ArrayList<OperatorInfo?>) {
        val tagContainer = LinearLayout(context)
        for (box in combinedTags) tagContainer.addView(getTagView(box, tagContainer))
        val flex = FlexboxLayout(context)
        flex.flexWrap = FlexWrap.WRAP
        for (info in matchedInfoList) flex.addView(getInfoView(info, flex))
        val startStar = matchedInfoList[0]!!.star
        val endStar = matchedInfoList[matchedInfoList.size - 1]!!.star
        resultList!!.add(ItemContainer().apply {
            setStar(startStar.coerceAtMost(endStar), startStar.coerceAtLeast(endStar))
            addView(tagContainer)
            addView(flex)
        })
    }

    private fun getTagView(box: CheckBox, container: ViewGroup): TextView {
        return (LayoutInflater.from(context).inflate(R.layout.tag_overlay, container, false) as TextView).apply {
            setPadding(paddingLeft, paddingTop shr 1, paddingRight, paddingBottom shr 1)
            setBackgroundResource(when (box.id) {
                R.id.tag_qualification_1 -> R.drawable.bg_tag_star_1
                R.id.tag_qualification_2 -> R.drawable.bg_tag_star_2
                R.id.tag_qualification_5 -> R.drawable.bg_tag_star_5
                R.id.tag_qualification_6 -> R.drawable.bg_tag_star_6
                else -> R.drawable.bg_tag
            })
            text = box.text
        }
    }

    private fun getInfoView(info: OperatorInfo?, container: ViewGroup): TextView {
        return (LayoutInflater.from(context).inflate(R.layout.tag_overlay, container, false) as TextView).apply {
            text = info!!.getName(index)
            setOnClickListener {
                val str = StringBuilder().apply {
                    val space = ' '
                    append(info.getName(index))
                    append(space)
                    append(info.getName(if (index == DataUtil.INDEX_CN) DataUtil.INDEX_EN else DataUtil.INDEX_CN))
                    append(if (context is MainActivity) space else System.lineSeparator())
                    when (info.star) {
                        1 -> {
                            append(RecruitTag.QUALIFICATION_1[index])
                            append(space)
                        }
                        5 -> {
                            append(RecruitTag.QUALIFICATION_5[index])
                            append(space)
                        }
                        6 -> {
                            append(RecruitTag.QUALIFICATION_6[index])
                            append(space)
                        }
                    }
                    append(RecruitTag.getTagName(info.type, index))
                    for (tag in info.tags) {
                        append(space)
                        append(RecruitTag.getTagName(tag!!, index))
                    }
                }.toString()
                if (context is MainActivity) Snackbar.make(container, str, Snackbar.LENGTH_LONG).show() else OverlayToast.show(context, str, OverlayToast.LENGTH_LONG)
            }
            setBackgroundResource(when (info.star) {
                1 -> R.drawable.bg_tag_star_1
                2 -> R.drawable.bg_tag_star_2
                3 -> R.drawable.bg_tag_star_3
                4 -> R.drawable.bg_tag_star_4
                5 -> R.drawable.bg_tag_star_5
                else -> R.drawable.bg_tag_star_6
            })
        }
    }

    private fun hasPossibility(info: OperatorInfo?): Boolean {
        return (info!!.star != 6 || combinedTags.contains(top)) && (manager.recruitPreview || !info.getName(index).endsWith(DataUtil.FLAG_UNRELEASED))
    }

    private fun compareInfo(i1: OperatorInfo, i2: OperatorInfo): Int {
        return if (manager.ascendingStar) i1.star - i2.star else i2.star - i1.star
    }

    private fun compareTags(t1: CheckBox, t2: CheckBox): Int {
        return if (t1.parent === t2.parent)
            (t1.parent as ViewGroup).indexOfChild(t1) - (t1.parent as ViewGroup).indexOfChild(t2)
        else
            (t1.parent.parent as ViewGroup).indexOfChild(t1.parent as View) - (t2.parent.parent as ViewGroup).indexOfChild(t2.parent as View)
    }

    private inner class ItemContainer : LinearLayout(context), Comparable<ItemContainer> {
        var minStar = 0
        private var maxStar = 0

        init {
            orientation = VERTICAL
            setPadding(0, 0, 0, context.resources.getDimensionPixelOffset(R.dimen.control_padding))
        }

        fun setStar(min: Int, max: Int) {
            minStar = min
            maxStar = max
        }

        override fun compareTo(other: ItemContainer): Int {
            var i = other.minStar - minStar
            if (i == 0) i = other.maxStar - maxStar
            return i
        }
    }
}