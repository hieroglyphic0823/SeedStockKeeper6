package com.example.seedstockkeeper6.util

fun normalizeEffectName(rawEffect: String?): String {
    if (rawEffect.isNullOrBlank()) return "不明"
    val normalized = rawEffect.trim()

    return when {
        // 害虫予防
        normalized.contains("害虫") || normalized.contains("虫") || normalized.contains("アブラムシ") ||
                normalized.contains("ハダニ") || normalized.contains("pest", ignoreCase = true) ||
                normalized.contains("bug", ignoreCase = true) || normalized.contains("repellent", ignoreCase = true)
            -> "害虫予防"

        // 病気予防
        normalized.contains("病気") || normalized.contains("病害") || normalized.contains("うどんこ") ||
                normalized.contains("黒星") || normalized.contains("disease", true) || normalized.contains("health", true)
            -> "病気予防"

        // 生育促進
        normalized.contains("生育促進") || normalized.contains("成長") || normalized.contains("育ち") ||
                normalized.contains("vigour", true) || normalized.contains("growth", true) || normalized.contains("boost", true)
            -> "生育促進"

        // 空間活用
        normalized.contains("空間") || normalized.contains("レイアウト") || normalized.contains("つる") ||
                normalized.contains("這う") || normalized.contains("支柱") ||
                normalized.contains("space", true) || normalized.contains("layout", true) || normalized.contains("optimiz", true)
            -> "空間活用"

        // 風味向上
        normalized.contains("風味") || normalized.contains("香り") || normalized.contains("味") ||
                normalized.contains("flavor", true) || normalized.contains("taste", true)
            -> "風味向上"

        // 土壌改善
        normalized.contains("土壌") || normalized.contains("地力") || normalized.contains("団粒") || normalized.contains("根粒") ||
                normalized.contains("soil", true) || normalized.contains("fertility", true) || normalized.contains("improve", true)
            -> "土壌改善"

        // 受粉促進
        normalized.contains("受粉") || normalized.contains("ミツバチ") || normalized.contains("花粉") ||
                normalized.contains("bee", true) || normalized.contains("pollination", true)
            -> "受粉促進"

        // 雑草抑制
        normalized.contains("雑草") || normalized.contains("グランドカバー") ||
                normalized.contains("mulch", true) || normalized.contains("weed", true)
            -> "雑草抑制"

        // 景観美化
        normalized.contains("景観") || normalized.contains("観賞") || normalized.contains("花壇") || normalized.contains("美化") ||
                normalized.contains("landscape", true) || normalized.contains("beauty", true) || normalized.contains("beautify", true)
            -> "景観美化"

        // 水分保持
        normalized.contains("水分") || normalized.contains("乾燥") || normalized.contains("保水") ||
                normalized.contains("moisture", true) || normalized.contains("retention", true)
            -> "水分保持"

        // 土壌pH調整
        normalized.contains("pH") || normalized.contains("酸度") || normalized.contains("アルカリ") || normalized.contains("中和") ||
                normalized.contains("lime", true) || normalized.contains("balance", true)
            -> "土壌pH調整"

        // 作業性向上
        normalized.contains("作業") || normalized.contains("管理") || normalized.contains("効率") ||
                normalized.contains("efficiency", true) || normalized.contains("easy", true)
            -> "作業性向上"

        // 収量安定化
        normalized.contains("収量") || normalized.contains("安定") || normalized.contains("収穫") ||
                normalized.contains("yield", true) || normalized.contains("harvest", true) || normalized.contains("stability", true)
            -> "収量安定化"

        else -> "不明"
    }
}
