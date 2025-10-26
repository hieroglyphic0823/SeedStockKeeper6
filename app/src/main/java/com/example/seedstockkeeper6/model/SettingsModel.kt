package com.example.seedstockkeeper6.model

import androidx.compose.ui.graphics.Color

/**
 * 設定画面のデータモデルとヘルパー関数
 */

/**
 * 地域ごとの色定義
 */
fun getRegionColor(region: String): Color {
    return when (region) {
        "寒地" -> Color(0xFF1A237E) // 紺
        "寒冷地" -> Color(0xFF1976D2) // 青
        "温暖地" -> Color(0xFFFF9800) // オレンジ
        "暖地" -> Color(0xFFE91E63) // ピンク
        else -> Color(0xFF9E9E9E) // グレー（未設定時）
    }
}

/**
 * 農園主の選択肢
 */
val FARM_OWNER_OPTIONS = listOf("水戸黄門", "お銀", "八兵衛", "その他")

/**
 * 地域の選択肢
 */
val REGION_OPTIONS = listOf("寒地", "寒冷地", "温暖地", "暖地")

/**
 * 通知頻度の選択肢
 */
val NOTIFICATION_FREQUENCY_OPTIONS = listOf("月一回", "週１回", "なし")

/**
 * 曜日の選択肢
 */
val WEEKDAY_OPTIONS = listOf("月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "日曜日")

/**
 * 種情報URLプロバイダーの選択肢
 */
val SEED_INFO_URL_PROVIDER_OPTIONS = listOf("サカタのたね", "たねのタキイ", "その他")

/**
 * 設定項目の定数
 */
object SettingsConstants {
    const val FARM_NAME_LABEL = "農園名を入力"
    const val FARM_NAME_HELP = "あなたの農園の名前を設定してください"
    const val FARM_OWNER_LABEL = "農園主名を入力"
    const val FARM_OWNER_HELP = "あなたの農園の主を選択してください"
    const val REGION_HELP = "種子登録時の地域初期値として使用されます"
    const val NOTIFICATION_HELP = "種まきのタイミングをお知らせします"
    const val SEED_INFO_URL_HELP = "種情報の参照先URLを設定します"
    const val CUSTOM_URL_PLACEHOLDER = "https://example.com"
    const val GOOGLE_MAPS_BUTTON_TEXT = "Googleマップで選択"
    const val GOOGLE_MAPS_HELP = "Googleマップから農園の位置を選択してください"
    const val NOTIFICATION_TEST_BUTTON_TEXT = "通知テスト・プレビュー"
    const val REGION_SELECTION_TITLE = "地域"
    const val PREFECTURE_SELECTION_TITLE = "県を選択"
    const val REGION_DISPLAY_TITLE = "地域"
    const val PREFECTURE_DISPLAY_TITLE = "県"
    const val NOTIFICATION_FREQUENCY_DISPLAY_TITLE = "通知頻度"
    const val NOTIFICATION_WEEKDAY_DISPLAY_TITLE = "通知曜日"
    const val SEED_INFO_URL_DISPLAY_TITLE = "種情報URL"
    const val CUSTOM_URL_DISPLAY_TITLE = "カスタムURL"
    const val FARM_POSITION_DISPLAY_TITLE = "農園位置"
    const val CURRENT_POSITION_DISPLAY_TITLE = "現在の位置"
    const val LATITUDE_DISPLAY_PREFIX = "緯度: "
    const val LONGITUDE_DISPLAY_PREFIX = "経度: "
    const val ADDRESS_DISPLAY_PREFIX = "住所: "
    const val NOT_SET_DISPLAY_TEXT = "未設定"
    const val WEEKDAY_SELECTION_LABEL = "曜日を選択:"
    const val URL_INPUT_LABEL = "URLを入力"
    const val SAVE_BUTTON_DESCRIPTION = "保存"
    const val REGION_SELECTION_DESCRIPTION = "地域選択"
    const val FARM_POSITION_DESCRIPTION = "農園位置"
    const val FARM_OWNER_DESCRIPTION = "農園主"
    const val REGION_SETTING_DESCRIPTION = "地域設定"
    const val NOTIFICATION_DESCRIPTION = "通知設定"
    const val SEED_INFO_URL_DESCRIPTION = "種情報URL設定"
    const val GOOGLE_MAPS_DESCRIPTION = "Googleマップで選択"
    const val NOTIFICATION_TEST_DESCRIPTION = "通知テスト・プレビュー"
    const val FARM_NAME_DESCRIPTION = "農園名"
    const val NOTIFICATION_ACTIVE_DESCRIPTION = "通知設定"
    const val LINK_DESCRIPTION = "種情報URL設定"
    const val PERSON_DESCRIPTION = "農園主"
    const val PUBLIC_DESCRIPTION = "地域設定"
    const val LOCATION_DESCRIPTION = "農園位置"
    const val NOTIFICATIONS_DESCRIPTION = "通知テスト・プレビュー"
}
