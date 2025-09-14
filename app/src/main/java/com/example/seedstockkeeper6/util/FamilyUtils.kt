package com.example.seedstockkeeper6.util
fun normalizeFamilyName(rawFamily: String?): String {
    if (rawFamily.isNullOrBlank()) return "不明"
    val normalized = rawFamily.trim().replace("属科", "科")
    return when {
        normalized.contains("ナス") -> "ナス科"
        normalized.contains("アブラナ") || normalized.contains("十字") -> "アブラナ科"
        normalized.contains("ヒルガオ") -> "ヒルガオ科"
        normalized.contains("ウリ") -> "ウリ科"
        normalized.contains("マメ") -> "マメ科"
        normalized.contains("セリ") || normalized.contains("せり") -> "セリ科"
        normalized.contains("キク") -> "キク科"
        normalized.contains("ネギ") || normalized.contains("ヒガンバナ") || normalized.contains("アリウム") -> "ネギ科"
        normalized.contains("アマランサス") || normalized.contains("ヒユ") -> "アマランサス科"
        normalized.contains("イネ") -> "イネ科"
        normalized.contains("バラ") -> "バラ科"
        normalized.contains("ミカン") || normalized.contains("柑橘") -> "ミカン科"
        else -> "不明"
    }
}