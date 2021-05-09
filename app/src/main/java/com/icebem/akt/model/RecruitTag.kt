package com.icebem.akt.model;

import android.util.SparseArray;

import com.icebem.akt.R;
import com.icebem.akt.util.DataUtil;

class RecruitTag {
    private static final String[] STAR_1 = {"1★", "1★", "1★", "★1", "1★"};
    private static final String[] STAR_2 = {"2★", "2★", "2★", "★2", "2★"};
    private static final String[] STAR_3 = {"3★", "3★", "3★", "★3", "3★"};
    private static final String[] STAR_4 = {"4★", "4★", "4★", "★4", "4★"};
    private static final String[] STAR_5 = {"5★", "5★", "5★", "★5", "5★"};
    private static final String[] STAR_6 = {"6★", "6★", "6★", "★6", "6★"};
    static final String[] QUALIFICATION_1 = {"Robot", "支援机械", "支援機械", "ロボット", "로봇"};
    private static final String[] QUALIFICATION_2 = {"Starter", "新手", "新手", "初期", "신입"};
    static final String[] QUALIFICATION_5 = {"Senior Operator", "资深干员", "資深幹員", "エリート", "특별 채용"};
    static final String[] QUALIFICATION_6 = {"Top Operator", "高级资深干员", "高級資深幹員", "上級エリート", "고급 특별 채용"};
    private static final String[] POSITION_MELEE = {"Melee", "近战位", "近戰位", "近距離", "근거리"};
    private static final String[] POSITION_RANGED = {"Ranged", "远程位", "遠程位", "遠距離", "원거리"};
    private static final String[] TYPE_VANGUARD = {"Vanguard", "先锋干员", "先鋒幹員", "先鋒タイプ", "뱅가드"};
    private static final String[] TYPE_SNIPER = {"Sniper", "狙击干员", "狙擊幹員", "狙撃タイプ", "스나이퍼"};
    private static final String[] TYPE_GUARD = {"Guard", "近卫干员", "近衛幹員", "前衛タイプ", "가드"};
    private static final String[] TYPE_CASTER = {"Caster", "术师干员", "術師幹員", "術師タイプ", "캐스터"};
    private static final String[] TYPE_DEFENDER = {"Defender", "重装干员", "重裝幹員", "重装タイプ", "디펜더"};
    private static final String[] TYPE_MEDIC = {"Medic", "医疗干员", "醫療幹員", "医療タイプ", "메딕"};
    private static final String[] TYPE_SPECIALIST = {"Specialist", "特种干员", "特種幹員", "特殊タイプ", "스페셜리스트"};
    private static final String[] TYPE_SUPPORTER = {"Supporter", "辅助干员", "輔助幹員", "補助タイプ", "서포터"};
    private static final String[] AFFIX_SURVIVAL = {"Survival", "生存", "生存", "生存", "생존형"};
    private static final String[] AFFIX_AOE = {"AoE", "群攻", "群攻", "範囲攻撃", "범위공격"};
    private static final String[] AFFIX_SLOW = {"Slow", "减速", "減速", "減速", "감속"};
    private static final String[] AFFIX_HEALING = {"Healing", "治疗", "治療", "治療", "힐링"};
    private static final String[] AFFIX_DPS = {"DPS", "输出", "輸出", "火力", "딜러"};
    private static final String[] AFFIX_DEFENSE = {"Defense", "防护", "防護", "防御", "방어형"};
    private static final String[] AFFIX_RECOVERY = {"DP-Recovery", "费用回复", "費用回復", "COST回復", "코스트+"};
    private static final String[] AFFIX_REDEPLOY = {"Fast-Redeploy", "快速复活", "快速復活", "高速再配置", "쾌속부활"};
    private static final String[] AFFIX_DEBUFF = {"Debuff", "削弱", "削弱", "弱化", "디버프"};
    private static final String[] AFFIX_SUPPORT = {"Support", "支援", "支援", "支援", "지원"};
    private static final String[] AFFIX_SHIFT = {"Shift", "位移", "位移", "強制移動", "강제이동"};
    private static final String[] AFFIX_SUMMON = {"Summon", "召唤", "召喚", "召喚", "소환"};
    private static final String[] AFFIX_NUKER = {"Nuker", "爆发", "爆發", "爆発力", "누커"};
    private static final String[] AFFIX_CONTROL = {"Crowd-Control", "控场", "控場", "牽制", "제어형"};
    private static final String[][] TAG_ARRAY = {
            QUALIFICATION_2,
            POSITION_MELEE,
            POSITION_RANGED,
            TYPE_VANGUARD,
            TYPE_SNIPER,
            TYPE_GUARD,
            TYPE_CASTER,
            TYPE_DEFENDER,
            TYPE_MEDIC,
            TYPE_SPECIALIST,
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
        array.put(R.id.tag_type_specialist, TYPE_SPECIALIST);
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
        if (index != DataUtil.INDEX_EN) {
            for (String[] array : TAG_ARRAY) {
                if (tagName.equals(array[DataUtil.INDEX_EN]))
                    return array[index];
            }
        }
        return tagName;
    }
}
