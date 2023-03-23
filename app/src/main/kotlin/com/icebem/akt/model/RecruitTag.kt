package com.icebem.akt.model

import android.util.SparseArray
import com.icebem.akt.R
import com.icebem.akt.util.ArkData

internal object RecruitTag {
    private val STAR_1 = arrayOf("1★", "1★", "1★", "★1", "1★")
    private val STAR_2 = arrayOf("2★", "2★", "2★", "★2", "2★")
    private val STAR_3 = arrayOf("3★", "3★", "3★", "★3", "3★")
    private val STAR_4 = arrayOf("4★", "4★", "4★", "★4", "4★")
    private val STAR_5 = arrayOf("5★", "5★", "5★", "★5", "5★")
    private val STAR_6 = arrayOf("6★", "6★", "6★", "★6", "6★")
    val QUALIFICATION_1 = arrayOf("Robot", "支援机械", "支援機械", "ロボット", "로봇")
    private val QUALIFICATION_2 = arrayOf("Starter", "新手", "新手", "初期", "신입")
    val QUALIFICATION_5 = arrayOf("Senior Operator", "资深干员", "資深幹員", "エリート", "특별 채용")
    val QUALIFICATION_6 = arrayOf("Top Operator", "高级资深干员", "高級資深幹員", "上級エリート", "고급 특별 채용")
    private val POSITION_MELEE = arrayOf("Melee", "近战位", "近戰位", "近距離", "근거리")
    private val POSITION_RANGED = arrayOf("Ranged", "远程位", "遠程位", "遠距離", "원거리")
    private val TYPE_VANGUARD = arrayOf("Vanguard", "先锋干员", "先鋒幹員", "先鋒タイプ", "뱅가드")
    private val TYPE_SNIPER = arrayOf("Sniper", "狙击干员", "狙擊幹員", "狙撃タイプ", "스나이퍼")
    private val TYPE_GUARD = arrayOf("Guard", "近卫干员", "近衛幹員", "前衛タイプ", "가드")
    private val TYPE_CASTER = arrayOf("Caster", "术师干员", "術師幹員", "術師タイプ", "캐스터")
    private val TYPE_DEFENDER = arrayOf("Defender", "重装干员", "重裝幹員", "重装タイプ", "디펜더")
    private val TYPE_MEDIC = arrayOf("Medic", "医疗干员", "醫療幹員", "医療タイプ", "메딕")
    private val TYPE_SPECIALIST = arrayOf("Specialist", "特种干员", "特種幹員", "特殊タイプ", "스페셜리스트")
    private val TYPE_SUPPORTER = arrayOf("Supporter", "辅助干员", "輔助幹員", "補助タイプ", "서포터")
    private val AFFIX_SURVIVAL = arrayOf("Survival", "生存", "生存", "生存", "생존형")
    private val AFFIX_AOE = arrayOf("AoE", "群攻", "群攻", "範囲攻撃", "범위공격")
    private val AFFIX_SLOW = arrayOf("Slow", "减速", "減速", "減速", "감속")
    private val AFFIX_HEALING = arrayOf("Healing", "治疗", "治療", "治療", "힐링")
    private val AFFIX_DPS = arrayOf("DPS", "输出", "輸出", "火力", "딜러")
    private val AFFIX_DEFENSE = arrayOf("Defense", "防护", "防護", "防御", "방어형")
    private val AFFIX_RECOVERY = arrayOf("DP-Recovery", "费用回复", "費用回復", "COST回復", "코스트+")
    private val AFFIX_REDEPLOY = arrayOf("Fast-Redeploy", "快速复活", "快速復活", "高速再配置", "쾌속부활")
    private val AFFIX_DEBUFF = arrayOf("Debuff", "削弱", "削弱", "弱化", "디버프")
    private val AFFIX_SUPPORT = arrayOf("Support", "支援", "支援", "支援", "지원")
    private val AFFIX_SHIFT = arrayOf("Shift", "位移", "位移", "強制移動", "강제이동")
    private val AFFIX_SUMMON = arrayOf("Summon", "召唤", "召喚", "召喚", "소환")
    private val AFFIX_NUKER = arrayOf("Nuker", "爆发", "爆發", "爆発力", "누커")
    private val AFFIX_CONTROL = arrayOf("Crowd-Control", "控场", "控場", "牽制", "제어형")
    private val TAG_ARRAY = arrayOf(QUALIFICATION_2, POSITION_MELEE, POSITION_RANGED, TYPE_VANGUARD, TYPE_SNIPER, TYPE_GUARD, TYPE_CASTER, TYPE_DEFENDER, TYPE_MEDIC, TYPE_SPECIALIST, TYPE_SUPPORTER, AFFIX_SURVIVAL, AFFIX_AOE, AFFIX_SLOW, AFFIX_HEALING, AFFIX_DPS, AFFIX_DEFENSE, AFFIX_RECOVERY, AFFIX_REDEPLOY, AFFIX_DEBUFF, AFFIX_SUPPORT, AFFIX_SHIFT, AFFIX_SUMMON, AFFIX_NUKER, AFFIX_CONTROL)

    val array: SparseArray<Array<String>>
        get() = SparseArray<Array<String>>().apply {
            put(R.id.tag_star_1, STAR_1)
            put(R.id.tag_star_2, STAR_2)
            put(R.id.tag_star_3, STAR_3)
            put(R.id.tag_star_4, STAR_4)
            put(R.id.tag_star_5, STAR_5)
            put(R.id.tag_star_6, STAR_6)
            put(R.id.tag_qualification_1, QUALIFICATION_1)
            put(R.id.tag_qualification_2, QUALIFICATION_2)
            put(R.id.tag_qualification_5, QUALIFICATION_5)
            put(R.id.tag_qualification_6, QUALIFICATION_6)
            put(R.id.tag_position_melee, POSITION_MELEE)
            put(R.id.tag_position_ranged, POSITION_RANGED)
            put(R.id.tag_type_vanguard, TYPE_VANGUARD)
            put(R.id.tag_type_sniper, TYPE_SNIPER)
            put(R.id.tag_type_guard, TYPE_GUARD)
            put(R.id.tag_type_caster, TYPE_CASTER)
            put(R.id.tag_type_defender, TYPE_DEFENDER)
            put(R.id.tag_type_medic, TYPE_MEDIC)
            put(R.id.tag_type_specialist, TYPE_SPECIALIST)
            put(R.id.tag_type_supporter, TYPE_SUPPORTER)
            put(R.id.tag_affix_survival, AFFIX_SURVIVAL)
            put(R.id.tag_affix_aoe, AFFIX_AOE)
            put(R.id.tag_affix_slow, AFFIX_SLOW)
            put(R.id.tag_affix_healing, AFFIX_HEALING)
            put(R.id.tag_affix_dps, AFFIX_DPS)
            put(R.id.tag_affix_defense, AFFIX_DEFENSE)
            put(R.id.tag_affix_recovery, AFFIX_RECOVERY)
            put(R.id.tag_affix_redeploy, AFFIX_REDEPLOY)
            put(R.id.tag_affix_debuff, AFFIX_DEBUFF)
            put(R.id.tag_affix_support, AFFIX_SUPPORT)
            put(R.id.tag_affix_shift, AFFIX_SHIFT)
            put(R.id.tag_affix_summon, AFFIX_SUMMON)
            put(R.id.tag_affix_nuker, AFFIX_NUKER)
            put(R.id.tag_affix_control, AFFIX_CONTROL)
        }

    fun getTagName(tagName: String, index: Int): String {
        if (index != ArkData.INDEX_EN) {
            for (array in TAG_ARRAY) {
                if (tagName == array[ArkData.INDEX_EN]) return array[index]
            }
        }
        return tagName
    }
}