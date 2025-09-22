package com.example.seedstockkeeper6.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme
import com.example.seedstockkeeper6.ui.screens.CalendarScreen
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel

@Preview(showBackground = true, name = "カレンダー画面 - ライトテーマ", heightDp = 800)
@Composable
fun CalendarScreenPreview() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        val navController = rememberNavController()
        val viewModel = createPreviewSeedListViewModel()
        
        CalendarScreen(
            navController = navController,
            viewModel = viewModel,
            isPreview = true
        )
    }
}

@Preview(showBackground = true, name = "カレンダー画面 - ダークテーマ", heightDp = 800)
@Composable
fun CalendarScreenDarkPreview() {
    SeedStockKeeper6Theme(darkTheme = true, dynamicColor = false) {
        val navController = rememberNavController()
        val viewModel = createPreviewSeedListViewModel()
        
        CalendarScreen(
            navController = navController,
            viewModel = viewModel,
            isPreview = true
        )
    }
}

@Preview(showBackground = true, name = "カレンダー画面 - 動的カラー", heightDp = 800)
@Composable
fun CalendarScreenDynamicPreview() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = true) {
        val navController = rememberNavController()
        val viewModel = createPreviewSeedListViewModel()
        
        CalendarScreen(
            navController = navController,
            viewModel = viewModel,
            isPreview = true
        )
    }
}
