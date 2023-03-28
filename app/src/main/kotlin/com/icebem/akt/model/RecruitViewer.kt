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
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.snackbar.Snackbar
import com.icebem.akt.R
import com.icebem.akt.databinding.FragmentRecruitBinding
import com.icebem.akt.overlay.OverlayToast
import com.icebem.akt.ui.main.MainActivity
import com.icebem.akt.util.ArkData
import com.icebem.akt.util.ArkPref

class RecruitViewer(private val context: Context, private val binding: FragmentRecruitBinding) {
    companion object {
        private const val TAG_CHECKED_MAX = 5
        private const val TAG_COMBINED_MAX = 3
        private const val CHECKED_TIME_ID = R.id.tag_time_3
        private val CHECKED_STARS_ID = arrayOf(intArrayOf(R.id.tag_time_3, R.id.tag_star_3, R.id.tag_star_4, R.id.tag_star_5), intArrayOf(R.id.tag_time_2, R.id.tag_star_2, R.id.tag_star_3, R.id.tag_star_4, R.id.tag_star_5), intArrayOf(R.id.tag_time_1, R.id.tag_star_1, R.id.tag_star_2, R.id.tag_star_3, R.id.tag_star_4))
    }

    private val infos: Array<OperatorInfo> = OperatorInfo.array
    private val tagArray: SparseArray<Array<String>> = RecruitTag.array
    private val stars: List<CheckBox> = findBoxesById(R.id.tag_star_1)
    private val qualifications: List<CheckBox> = findBoxesById(R.id.tag_qualification_1)
    private val positions: List<CheckBox> = findBoxesById(R.id.tag_position_melee)
    private val types: List<CheckBox> = findBoxesById(R.id.tag_type_vanguard)
    private val affixes: List<CheckBox> = findBoxesById(R.id.tag_affix_survival)
    private val checkedStars: MutableList<CheckBox> = mutableListOf()
    private val checkedTags: MutableList<CheckBox> = mutableListOf()
    private val combinedTags: MutableList<CheckBox> = mutableListOf()
    private val checkedInfoList: MutableList<OperatorInfo> = mutableListOf()
    private val resultList: MutableList<ItemContainer> = mutableListOf()
    private var index = 0
    private var autoAction = false

    init {
        setBoxesText()
        binding.actionRecruitReset.setOnClickListener { resetTags(it) }
        binding.groupRecruitTime.setOnCheckedChangeListener { group, checkedId -> onCheckedChange(group, checkedId) }
        setOnCheckedChangeListener(stars)
        setOnCheckedChangeListener(qualifications)
        setOnCheckedChangeListener(positions)
        setOnCheckedChangeListener(types)
        setOnCheckedChangeListener(affixes)
        resetTags(null)
    }

    private fun findBoxById(id: Int): CheckBox = binding.containerRecruitTags.findViewById(id)

    private fun findBoxesById(id: Int): List<CheckBox> {
        val boxes = mutableListOf<CheckBox>()
        val group = binding.containerRecruitTags.findViewById<View>(id).parent as ViewGroup
        for (i in 0 until group.childCount) if (group.getChildAt(i) is CheckBox) boxes.add(group.getChildAt(i) as CheckBox)
        return boxes
    }

    private fun setBoxesText() {
        index = ArkPref.translationIndex
        for (box in stars) box.text = tagArray[box.id][index]
        for (box in qualifications) box.text = tagArray[box.id][index]
        for (box in positions) box.text = tagArray[box.id][index]
        for (box in types) box.text = tagArray[box.id][index]
        for (box in affixes) box.text = tagArray[box.id][index]
    }

    private fun setOnCheckedChangeListener(boxes: List<CheckBox>) {
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
            if (binding.tagQualification6.isChecked) findBoxById(R.id.tag_star_6).isChecked = true
            if (findBoxById(R.id.tag_qualification_5).isChecked && !findBoxById(R.id.tag_star_5).isChecked) findBoxById(R.id.tag_star_5).isChecked = true
            autoAction = false
            updateRecruitResult()
        }
    }

    private fun onCheckedChange(tag: CompoundButton, isChecked: Boolean) {
        if (tag is CheckBox) {
            if (!stars.contains(tag) && isChecked && checkedTags.size >= TAG_CHECKED_MAX) {
                tag.isChecked = false
            } else {
                val state = autoAction
                autoAction = true
                if (tag.id == R.id.tag_qualification_6 && findBoxById(R.id.tag_star_6).isChecked != isChecked) findBoxById(R.id.tag_star_6).isChecked = isChecked
                else if (tag.getId() == R.id.tag_qualification_5 && findBoxById(R.id.tag_star_5).isChecked != isChecked && (isChecked || binding.tagTime1.isChecked)) findBoxById(R.id.tag_star_5).isChecked = isChecked
                autoAction = state
                updateCheckedTags(tag, isChecked)
            }
        }
    }

    fun resetTags(view: View? = null) {
        view?.isClickable = false
        autoAction = true
        while (checkedTags.isNotEmpty()) checkedTags[0].isChecked = false
        if (index != ArkPref.translationIndex) setBoxesText()
        val timeTag = binding.containerRecruitTags.findViewById<RadioButton>(CHECKED_TIME_ID)
        if (timeTag.isChecked) onCheckedChange(timeTag.parent as RadioGroup, CHECKED_TIME_ID)
        else timeTag.isChecked = true
        binding.root.post { binding.root.smoothScrollTo(0, if (context is MainActivity) 0 else (binding.tagQualification6.parent as ViewGroup).top) }
        view?.isClickable = true
    }

    fun toggleServer(view: View? = null) {
        val packages = ArkPref.availablePackages
        var index = ArkPref.gamePackagePosition
        if (++index == packages.size) index = 0
        ArkPref.setGamePackage(packages[index])
        resetTags(view)
    }

    private fun updateCheckedTags(tag: CheckBox, isChecked: Boolean) {
        if (stars.contains(tag)) {
            if (isChecked) checkedStars.add(tag) else checkedStars.remove(tag)
            for (info in infos) {
                if (tag.text.contains(info.star.toString())) {
                    if (isChecked) checkedInfoList.add(info) else checkedInfoList.remove(info)
                }
            }
            checkedInfoList.sortWith(::compareInfo)
        } else {
            if (isChecked) checkedTags.add(tag) else checkedTags.remove(tag)
        }
        if (!autoAction) updateRecruitResult()
    }

    private fun updateRecruitResult() {
        binding.containerRecruitResult.removeAllViews()
        if (checkedTags.isEmpty()) {
            val flex = FlexboxLayout(context)
            flex.flexWrap = FlexWrap.WRAP
            for (info in checkedInfoList) if (hasPossibility(info)) flex.addView(getInfoView(info, flex))
            binding.containerRecruitResult.addView(flex)
            binding.txtRecruitTips.setText(if (checkedInfoList.isEmpty()) R.string.tip_recruit_result_none else R.string.tip_recruit_result_default)
        } else {
            resultList.clear()
            checkedTags.sortWith(::compareTags)
            for (i in checkedTags.size.coerceAtMost(TAG_COMBINED_MAX) downTo 1) combineTags(0, combinedTags.size, i)
            resultList.sort()
            for (container in resultList) binding.containerRecruitResult.addView(container)
            if (checkedTags.size == TAG_CHECKED_MAX && ArkPref.scrollToResult) binding.root.post { binding.root.smoothScrollTo(0, binding.containerRecruitTags.height) }
            binding.txtRecruitTips.run {
                when (if (resultList.isEmpty()) 0 else resultList[0].minStar) {
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
        val matchedInfoList = mutableListOf<OperatorInfo>()
        for (info in checkedInfoList) {
            var matched = hasPossibility(info)
            for (tag in combinedTags) {
                matched = if (matched) {
                    when {
                        qualifications.contains(tag) -> {
                            tag.id == R.id.tag_qualification_1 && info.star == 1 || tag.id == R.id.tag_qualification_2 && info.star == 2 || tag.id == R.id.tag_qualification_5 && info.star == 5 || tag.id == R.id.tag_qualification_6 && info.star == 6
                        }
                        types.contains(tag) -> info.type == tagArray[tag.id][0]
                        else -> info.containsTag(tagArray[tag.id][0])
                    }
                } else break
            }
            if (matched) matchedInfoList.add(info)
        }
        if (matchedInfoList.isNotEmpty()) addResultToList(matchedInfoList)
    }

    private fun addResultToList(matchedInfoList: MutableList<OperatorInfo>) {
        val tagContainer = LinearLayout(context)
        for (box in combinedTags) tagContainer.addView(getTagView(box, tagContainer))
        val flex = FlexboxLayout(context)
        flex.flexWrap = FlexWrap.WRAP
        for (info in matchedInfoList) flex.addView(getInfoView(info, flex))
        val startStar = matchedInfoList[0].star
        val endStar = matchedInfoList[matchedInfoList.size - 1].star
        resultList.add(ItemContainer().apply {
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

    private fun getInfoView(info: OperatorInfo, container: ViewGroup): TextView {
        return (LayoutInflater.from(context).inflate(R.layout.tag_overlay, container, false) as TextView).apply {
            text = info.getName(index)
            setOnClickListener {
                val str = buildString {
                    val space = ' '
                    append(info.getName(index))
                    append(space)
                    append(info.getName(if (index == ArkData.INDEX_CN) ArkData.INDEX_EN else ArkData.INDEX_CN))
                    if (context is MainActivity) append(space) else appendLine()
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
                        append(RecruitTag.getTagName(tag, index))
                    }
                }
                if (context is MainActivity) Snackbar.make(container, str, Snackbar.LENGTH_LONG).show() else OverlayToast.show(str, OverlayToast.LENGTH_LONG)
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

    private fun hasPossibility(info: OperatorInfo): Boolean {
        return (info.star != 6 || combinedTags.contains(binding.tagQualification6)) && (ArkPref.recruitPreview || !info.getName(index).endsWith(ArkData.FLAG_UNRELEASED))
    }

    private fun compareInfo(a: OperatorInfo, b: OperatorInfo): Int {
        return if (ArkPref.ascendingStar) a.star - b.star else b.star - a.star
    }

    private fun compareTags(a: CheckBox, b: CheckBox): Int {
        return if (a.parent === b.parent) (a.parent as ViewGroup).indexOfChild(a) - (a.parent as ViewGroup).indexOfChild(b)
        else (a.parent.parent as ViewGroup).indexOfChild(a.parent as View) - (b.parent.parent as ViewGroup).indexOfChild(b.parent as View)
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