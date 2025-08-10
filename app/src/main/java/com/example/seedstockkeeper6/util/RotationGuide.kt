package com.example.seedstockkeeper6.util

/** 科ごとの輪作年限（目安）の “小さい方の数字” を返す。該当なしは null。 */
fun familyRotationMinYears(rawFamily: String?): Int? {
    val f = normalizeFamilyName(rawFamily)
    return when (f) {
        "アマランサス科" -> 2      // 2〜3年
        "ネギ科"         -> 3      // 3〜4年（Allium/Amaryllidaceae）
        "セリ科"         -> 3      // 3〜4年
        "キク科"         -> 2      // 2〜3年
        "アブラナ科"     -> 4      // 4〜5年
        "ウリ科"         -> 3      // 3〜4年
        "マメ科"         -> 3      // 3〜4年
        "イネ科"         -> 2      // 2〜3年
        "ナス科"         -> 4      // 4〜5年
        "バラ科"         -> 3      // 3〜4年（果実系の想定）
        "ヒルガオ科"       -> 0   // 永年作物→数値表示なし（バッジ非表示）
        "ミカン科"       -> 0   // 永年作物→数値表示なし（バッジ非表示）
        else              -> null
    }
}
fun familyRotationMinYearsLabel(
    rawFamily: String?,
    withYearSuffix: Boolean = false,   // true なら "2年" のように単位を付ける
    perennialLabel: String? = null     // 永年作物(ミカン科)に表示したい文字（nullで非表示）
): String? {
    val min = familyRotationMinYears(rawFamily) ?: return perennialLabel
    return if (withYearSuffix) "${min}年" else min.toString()
}