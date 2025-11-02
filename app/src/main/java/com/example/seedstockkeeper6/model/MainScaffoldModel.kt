package com.example.seedstockkeeper6.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * MainScaffoldのデータモデルとヘルパー関数
 */

/**
 * ナビゲーションアイテムの定数
 */
object NavigationConstants {
    const val CASTLE_ROUTE = "castle"
    const val LIST_ROUTE = "list"
    const val CALENDAR_ROUTE = "calendar"
    const val NOTIFICATION_HISTORY_ROUTE = "notification_history"
    const val SETTINGS_ROUTE = "settings"
    const val INPUT_ROUTE_PREFIX = "input"
    const val NOTIFICATION_PREVIEW_ROUTE = "notification_preview"
    const val MAP_SELECTION_ROUTE = "map_selection"
}

/**
 * アイコンサイズの定数
 */
object IconSizeConstants {
    val DEFAULT_SIZE = 24.dp
    val SELECTED_SIZE = 28.dp
    val BADGE_SIZE = 18.dp
    val TOP_APP_BAR_SIZE = 24.dp
}

/**
 * アニメーション定数
 */
object AnimationConstants {
    const val ROTATION_DURATION_MS = 500
    const val SAVE_ANIMATION_DURATION_MS = 2000
    const val APP_INITIALIZATION_DELAY_MS = 3000
    const val SETTINGS_LOAD_DELAY_MS = 1000
}

/**
 * 通知バッジの定数
 */
object NotificationConstants {
    const val MAX_BADGE_COUNT = 99
    const val BADGE_OVERFLOW_TEXT = "99+"
}

/**
 * 画面タイトルの定数
 */
object ScreenTitleConstants {
    const val CASTLE_TITLE = "お城"
    const val LIST_TITLE = "お棚場"
    const val CALENDAR_TITLE = "種暦"
    const val NOTIFICATION_TITLE = "通知履歴"
    const val SETTINGS_TITLE = "農園設定"
    const val NOTIFICATION_PREVIEW_TITLE = "通知テスト・プレビュー"
    const val MAP_SELECTION_TITLE = "Googleマップで農園位置を選択"
    const val INPUT_TITLE = "種札"
    const val NEW_INPUT_TITLE = "新規作成"
}

/**
 * アイコンの説明文定数
 */
object IconDescriptionConstants {
    const val BACK_BUTTON = "戻る"
    const val EDIT_BUTTON = "編集"
    const val CANCEL_BUTTON = "キャンセル"
    const val SAVE_BUTTON = "保存"
    const val ADD_BUTTON = "追加"
    const val DELETE_BUTTON = "削除"
    const val SETTINGS_BUTTON = "設定"
    const val NOTIFICATION_PREVIEW_BUTTON = "通知テスト・プレビュー"
    const val CASTLE_ICON = "お城"
    const val LIST_ICON = "目録"
    const val CALENDAR_ICON = "カレンダー"
    const val NOTIFICATION_ICON = "通知履歴"
    const val FARM_SETTINGS_ICON = "農園設定"
    const val GOOGLE_MAPS_ICON = "Googleマップ"
    const val SEED_INFO_ICON = "種情報"
    const val PACKET_ICON = "種情報"
    const val HOME_ICON = "お城"
    const val GARDEN_CART_ICON = "農園設定"
    const val CALENDAR_DARK_ICON = "カレンダー"
    const val CALENDAR_LIGHT_ICON = "カレンダー"
    const val YABUMI_ICON = "通知履歴"
}

/**
 * Snackbarメッセージの定数
 */
object SnackbarMessageConstants {
    const val DELETE_SUCCESS_SINGLE = "件の種情報を削除しました"
    const val DELETE_SUCCESS_PARTIAL = "件削除しました（件失敗）"
    const val DELETE_FAILURE = "削除に失敗しました"
}

/**
 * 色の定数
 */
object ColorConstants {
    val TRANSPARENT = Color.Transparent
    val UNSPECIFIED = Color.Unspecified
}

/**
 * レイアウト定数
 */
object LayoutConstants {
    val HORIZONTAL_PADDING = 12.dp
    val ACTION_SPACING = 8.dp
    val ICON_SPACING = 12.dp
    val BADGE_OFFSET_X = 16.dp
    val BADGE_OFFSET_Y = (-8).dp
}

/**
 * 通知バッジの表示テキストを生成
 */
fun getBadgeText(count: Int): String {
    return if (count > NotificationConstants.MAX_BADGE_COUNT) {
        NotificationConstants.BADGE_OVERFLOW_TEXT
    } else {
        count.toString()
    }
}

/**
 * お城画面のタイトルを生成
 */
fun getCastleTitle(farmName: String): String {
    return if (farmName.isNotEmpty()) {
        "${ScreenTitleConstants.CASTLE_TITLE}（$farmName）"
    } else {
        ScreenTitleConstants.CASTLE_TITLE
    }
}

/**
 * 種情報画面のタイトルを生成
 */
fun getSeedInfoTitle(
    productName: String,
    variety: String,
    family: String,
    isEditMode: Boolean,
    hasExistingData: Boolean
): String {
    return when {
        // DisplayMode: Familyアイコンと商品名
        !isEditMode && hasExistingData -> {
            if (productName.isNotEmpty() && variety.isNotEmpty()) {
                "$productName（$variety）"
            } else if (productName.isNotEmpty()) {
                productName
            } else {
                ScreenTitleConstants.INPUT_TITLE
            }
        }
        // EditMode新規作成: 現行アイコン+「新規作成」
        isEditMode && !hasExistingData -> {
            ScreenTitleConstants.NEW_INPUT_TITLE
        }
        // EditMode編集: Familyアイコンと商品名+（科名）
        isEditMode && hasExistingData -> {
            if (productName.isNotEmpty() && family.isNotEmpty()) {
                "$productName（$family）"
            } else {
                productName.ifEmpty { ScreenTitleConstants.INPUT_TITLE }
            }
        }
        // デフォルト: 商品名がある場合は商品名、ない場合は「種札」
        else -> {
            if (productName.isNotEmpty()) {
                if (variety.isNotEmpty()) {
                    "$productName（$variety）"
                } else {
                    productName
                }
            } else {
                ScreenTitleConstants.INPUT_TITLE
            }
        }
    }
}

/**
 * 削除結果のSnackbarメッセージを生成
 */
fun getDeleteSuccessMessage(count: Int): String {
    return "$count${SnackbarMessageConstants.DELETE_SUCCESS_SINGLE}"
}

/**
 * 部分削除成功のSnackbarメッセージを生成
 */
fun getDeletePartialSuccessMessage(successCount: Int, failureCount: Int): String {
    return "${successCount}件削除しました（${failureCount}件失敗）"
}
