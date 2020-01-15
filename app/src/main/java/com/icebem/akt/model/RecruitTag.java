package com.icebem.akt.model;

import android.util.SparseArray;

import com.icebem.akt.R;

public class RecruitTag {
    public static final int INDEX_EN = 0;
    public static final int INDEX_CN = 1;
    public static final int INDEX_JP = 2;
    static final String FLAG_UNRELEASED = "*";
    private static final String[] STAR_1 = {"1★", "1★", "★1"};
    private static final String[] STAR_2 = {"2★", "2★", "★2"};
    private static final String[] STAR_3 = {"3★", "3★", "★3"};
    private static final String[] STAR_4 = {"4★", "4★", "★4"};
    private static final String[] STAR_5 = {"5★", "5★", "★5"};
    private static final String[] STAR_6 = {"6★", "6★", "★6"};
    static final String[] QUALIFICATION_1 = {"Support Machine", "支援机械", "ロボット"};
    private static final String[] QUALIFICATION_2 = {"Starter", "新手", "初期"};
    static final String[] QUALIFICATION_5 = {"Senior", "资深干员", "エリート"};
    static final String[] QUALIFICATION_6 = {"Advanced Senior", "高级资深干员", "上級エリート"};
    private static final String[] POSITION_MELEE = {"Melee", "近战位", "近距離"};
    private static final String[] POSITION_RANGED = {"Ranged", "远程位", "遠距離"};
    private static final String[] TYPE_VANGUARD = {"Vanguard", "先锋干员", "先鋒タイプ"};
    private static final String[] TYPE_SNIPER = {"Sniper", "狙击干员", "狙撃タイプ"};
    private static final String[] TYPE_GUARD = {"Guard", "近卫干员", "前衛タイプ"};
    private static final String[] TYPE_CASTER = {"Caster", "术师干员", "術士タイプ"};
    private static final String[] TYPE_DEFENDER = {"Defender", "重装干员", "重装タイプ"};
    private static final String[] TYPE_MEDIC = {"Medic", "医疗干员", "医療タイプ"};
    private static final String[] TYPE_SPECIAL = {"Special", "特种干员", "特殊タイプ"};
    private static final String[] TYPE_SUPPORTER = {"Supporter", "辅助干员", "補助タイプ"};
    private static final String[] AFFIX_SURVIVAL = {"Survival", "生存", "生存"};
    private static final String[] AFFIX_AOE = {"AoE", "群攻", "範囲"};
    private static final String[] AFFIX_SLOW = {"Slow", "减速", "減速"};
    private static final String[] AFFIX_HEALING = {"Healing", "治疗", "治療"};
    private static final String[] AFFIX_DPS = {"DPS", "输出", "火力"};
    private static final String[] AFFIX_DEFENSE = {"Defense", "防护", "防御"};
    private static final String[] AFFIX_RECOVERY = {"DP-Recovery", "费用回复", "COST回復"};
    private static final String[] AFFIX_REDEPLOY = {"Fast-Redeploy", "快速复活", "快速復帰"};
    private static final String[] AFFIX_DEBUFF = {"Debuff", "削弱", "弱化"};
    private static final String[] AFFIX_SUPPORT = {"Support", "支援", "支援"};
    private static final String[] AFFIX_SHIFT = {"Shift", "位移", "強制移動"};
    private static final String[] AFFIX_SUMMON = {"Summon", "召唤", "召喚"};
    private static final String[] AFFIX_NUKER = {"Nuker", "爆发", "瞬発"};
    private static final String[] AFFIX_CONTROL = {"Crowd-Control", "控场", "牽制"};
    private static final String[][] TAG_ARRAY = {
            POSITION_MELEE,
            POSITION_RANGED,
            TYPE_VANGUARD,
            TYPE_SNIPER,
            TYPE_GUARD,
            TYPE_CASTER,
            TYPE_DEFENDER,
            TYPE_MEDIC,
            TYPE_SPECIAL,
            TYPE_SUPPORTER,
            AFFIX_SURVIVAL,
            AFFIX_AOE,
            AFFIX_SLOW,
            AFFIX_HEALING,
            AFFIX_DPS,
            AFFIX_DEFENSE,
            AFFIX_RECOVERY,
            AFFIX_REDEPLOY,
            AFFIX_DEBUFF,
            AFFIX_SUPPORT,
            AFFIX_SHIFT,
            AFFIX_SUMMON,
            AFFIX_NUKER,
            AFFIX_CONTROL
    };

    static SparseArray<String[]> getTagArray() {
        SparseArray<String[]> array = new SparseArray<>();
        array.put(R.id.tag_star_1, STAR_1);
        array.put(R.id.tag_star_2, STAR_2);
        array.put(R.id.tag_star_3, STAR_3);
        array.put(R.id.tag_star_4, STAR_4);
        array.put(R.id.tag_star_5, STAR_5);
        array.put(R.id.tag_star_6, STAR_6);
        array.put(R.id.tag_qualification_1, QUALIFICATION_1);
        array.put(R.id.tag_qualification_2, QUALIFICATION_2);
        array.put(R.id.tag_qualification_5, QUALIFICATION_5);
        array.put(R.id.tag_qualification_6, QUALIFICATION_6);
        array.put(R.id.tag_position_melee, POSITION_MELEE);
        array.put(R.id.tag_position_ranged, POSITION_RANGED);
        array.put(R.id.tag_type_vanguard, TYPE_VANGUARD);
        array.put(R.id.tag_type_sniper, TYPE_SNIPER);
        array.put(R.id.tag_type_guard, TYPE_GUARD);
        array.put(R.id.tag_type_caster, TYPE_CASTER);
        array.put(R.id.tag_type_defender, TYPE_DEFENDER);
        array.put(R.id.tag_type_medic, TYPE_MEDIC);
        array.put(R.id.tag_type_special, TYPE_SPECIAL);
        array.put(R.id.tag_type_supporter, TYPE_SUPPORTER);
        array.put(R.id.tag_affix_survival, AFFIX_SURVIVAL);
        array.put(R.id.tag_affix_aoe, AFFIX_AOE);
        array.put(R.id.tag_affix_slow, AFFIX_SLOW);
        array.put(R.id.tag_affix_healing, AFFIX_HEALING);
        array.put(R.id.tag_affix_dps, AFFIX_DPS);
        array.put(R.id.tag_affix_defense, AFFIX_DEFENSE);
        array.put(R.id.tag_affix_recovery, AFFIX_RECOVERY);
        array.put(R.id.tag_affix_redeploy, AFFIX_REDEPLOY);
        array.put(R.id.tag_affix_debuff, AFFIX_DEBUFF);
        array.put(R.id.tag_affix_support, AFFIX_SUPPORT);
        array.put(R.id.tag_affix_shift, AFFIX_SHIFT);
        array.put(R.id.tag_affix_summon, AFFIX_SUMMON);
        array.put(R.id.tag_affix_nuker, AFFIX_NUKER);
        array.put(R.id.tag_affix_control, AFFIX_CONTROL);
        return array;
    }

    static String getTagName(String tagName, int index) {
        if (index != INDEX_EN) {
            for (String[] array : TAG_ARRAY) {
                if (tagName.equals(array[INDEX_EN]))
                    return array[index];
            }
        }
        return tagName;
    }
}
