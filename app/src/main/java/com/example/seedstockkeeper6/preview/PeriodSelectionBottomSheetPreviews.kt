package com.example.seedstockkeeper6.preview

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.seedstockkeeper6.ui.components.PeriodSelectionBottomSheet
import com.example.seedstockkeeper6.ui.theme.SeedStockKeeper6Theme

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "期間選択ボトムシート - 播種開始")
@Composable
fun PeriodSelectionBottomSheetPreview_SowingStart() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        var selectedYear by remember { mutableStateOf("2025") }
        var selectedMonth by remember { mutableStateOf("8") }
        var selectedStage by remember { mutableStateOf("上旬") }
        
        Surface {
            PeriodSelectionBottomSheet(
                title = "播種開始",
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                selectedStage = selectedStage,
                onYearChange = { selectedYear = it },
                onMonthChange = { selectedMonth = it },
                onStageChange = { selectedStage = it },
                onConfirm = { },
                onCancel = { }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "期間選択ボトムシート - 収穫開始")
@Composable
fun PeriodSelectionBottomSheetPreview_HarvestStart() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        var selectedYear by remember { mutableStateOf("2025") }
        var selectedMonth by remember { mutableStateOf("10") }
        var selectedStage by remember { mutableStateOf("上旬") }
        
        Surface {
            PeriodSelectionBottomSheet(
                title = "収穫開始",
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                selectedStage = selectedStage,
                onYearChange = { selectedYear = it },
                onMonthChange = { selectedMonth = it },
                onStageChange = { selectedStage = it },
                onConfirm = { },
                onCancel = { }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "期間選択ボトムシート - 有効期限")
@Composable
fun PeriodSelectionBottomSheetPreview_Expiration() {
    SeedStockKeeper6Theme(darkTheme = false, dynamicColor = false) {
        var selectedYear by remember { mutableStateOf("2026") }
        var selectedMonth by remember { mutableStateOf("10") }
        var selectedStage by remember { mutableStateOf("") }
        
        Surface {
            PeriodSelectionBottomSheet(
                title = "有効期限",
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                selectedStage = selectedStage,
                onYearChange = { selectedYear = it },
                onMonthChange = { selectedMonth = it },
                onStageChange = { selectedStage = it },
                onConfirm = { },
                onCancel = { },
                confirmButtonColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        }
    }
}
