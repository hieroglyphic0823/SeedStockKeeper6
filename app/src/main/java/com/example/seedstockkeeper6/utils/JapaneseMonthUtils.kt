package com.example.seedstockkeeper6.utils

object JapaneseMonthUtils {
    
    /**
     * 和風月名を取得
     */
    fun getJapaneseMonthName(month: Int): String {
        return when (month) {
            1 -> "睦月（むつき）"
            2 -> "如月（きさらぎ）"
            3 -> "弥生（やよい）"
            4 -> "卯月（うづき）"
            5 -> "皐月（さつき）"
            6 -> "水無月（みなづき）"
            7 -> "文月（ふみづき）"
            8 -> "葉月（はづき）"
            9 -> "長月（ながつき）"
            10 -> "神無月（かんなづき）"
            11 -> "霜月（しもつき）"
            12 -> "師走（しわす）"
            else -> "${month}月"
        }
    }
    
    /**
     * 和風月名（短縮版）を取得
     */
    fun getJapaneseMonthNameShort(month: Int): String {
        return when (month) {
            1 -> "睦月"
            2 -> "如月"
            3 -> "弥生"
            4 -> "卯月"
            5 -> "皐月"
            6 -> "水無月"
            7 -> "文月"
            8 -> "葉月"
            9 -> "長月"
            10 -> "神無月"
            11 -> "霜月"
            12 -> "師走"
            else -> "${month}月"
        }
    }
    
    /**
     * 季節の候を取得
     */
    fun getSeasonalGreeting(month: Int): String {
        return when (month) {
            1 -> "新春の候"
            2 -> "立春の候"
            3 -> "春分の候"
            4 -> "桜花の候"
            5 -> "新緑の候"
            6 -> "梅雨の候"
            7 -> "盛夏の候"
            8 -> "残暑の候"
            9 -> "秋分の候"
            10 -> "秋冷の候"
            11 -> "晩秋の候"
            12 -> "歳末の候"
            else -> "季節の候"
        }
    }
    
    /**
     * 種まきキーワードを取得
     */
    fun getSowingKeyword(month: Int): String {
        return when (month) {
            1 -> "冬野菜の種まき"
            2 -> "早春の種まき"
            3 -> "春の種まき"
            4 -> "春まきの最盛期"
            5 -> "夏野菜の種まき"
            6 -> "梅雨時の種まき"
            7 -> "盛夏の種まき"
            8 -> "秋野菜の種まき"
            9 -> "秋の種まき"
            10 -> "晩秋の種まき"
            11 -> "冬支度の種まき"
            12 -> "年末の種まき"
            else -> "種まき"
        }
    }
}
