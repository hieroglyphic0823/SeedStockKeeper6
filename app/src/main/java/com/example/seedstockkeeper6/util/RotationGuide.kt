package com.example.seedstockkeeper6.util

/** 科ごとの連作障害年数の範囲を返す。該当なしは null。 */
fun familyRotationYearsRange(rawFamily: String?): String? {
    val f = normalizeFamilyName(rawFamily)
    return when (f) {
        "イネ科" -> "1～2年（比較的弱い）"
        "ナス科" -> "3～4年（強い連作障害）"
        "ウリ科" -> "2～3年"
        "アブラナ科" -> "2～3年"
        "マメ科" -> "2～3年（土壌病害が出やすい）"
        "キク科" -> "1～2年（比較的弱い）"
        "セリ科" -> "2～3年"
        "ヒガンバナ科" -> "3～4年（強め）"
        "バラ科" -> "3～4年"
        "ミカン科" -> "数年以上（果樹なので輪作よりも病害虫リスク管理が重要）"
        "ヒルガオ科" -> null // 永年作物→表示なし
        "アマランサス科" -> "2～3年"
        "アカザ科" -> "2～3年"
        "シソ科" -> "1～2年（比較的弱い）"
        "ユリ科（ネギ類）" -> "3～4年（強め）"
        "ショウガ科" -> "2～3年"
        "アオイ科" -> "2～3年"
        else -> null
    }
}

/** 科ごとの輪作年限（目安）の "小さい方の数字" を返す。該当なしは null。 */
fun familyRotationMinYears(rawFamily: String?): Int? {
    val f = normalizeFamilyName(rawFamily)
    return when (f) {
        "アマランサス科" -> 2      // 2〜3年
        "ヒガンバナ科"         -> 3      // 3〜4年
        "セリ科"         -> 2      // 2〜3年
        "キク科"         -> 1      // 1〜2年
        "アブラナ科"     -> 2      // 2〜3年
        "ウリ科"         -> 2      // 2〜3年
        "マメ科"         -> 2      // 2〜3年
        "イネ科"         -> 1      // 1〜2年
        "ナス科"         -> 3      // 3〜4年
        "バラ科"         -> 3      // 3〜4年
        "ヒルガオ科"       -> 0   // 永年作物→数値表示なし（バッジ非表示）
        "ミカン科"       -> 0   // 永年作物→数値表示なし（バッジ非表示）
        "アカザ科"        -> 2      // 2〜3年
        "シソ科"         -> 1      // 1〜2年
        "ユリ科（ネギ類）" -> 3      // 3〜4年
        "ショウガ科"      -> 2      // 2〜3年
        "アオイ科"        -> 2      // 2〜3年
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