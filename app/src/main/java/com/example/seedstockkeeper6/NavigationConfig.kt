package com.example.seedstockkeeper6

// ナビゲーション項目の定義
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val iconRes: Int
) {
    object Home : BottomNavItem(
        route = "list",
        title = "ホーム",
        iconRes = 0
    )
    object Search : BottomNavItem(
        route = "search",
        title = "検索",
        iconRes = 1
    )
    object Add : BottomNavItem(
        route = "add",
        title = "追加",
        iconRes = 2
    )
    object Calendar : BottomNavItem(
        route = "calendar",
        title = "カレンダー",
        iconRes = 3
    )
    object Settings : BottomNavItem(
        route = "settings",
        title = "設定",
        iconRes = 4
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Search,
    BottomNavItem.Add,
    BottomNavItem.Calendar,
    BottomNavItem.Settings
)
