package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.example.seedstockkeeper6.viewmodel.SeedInputViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StageSelector(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val stageOptions = listOf("上旬", "中旬", "下旬")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            modifier = modifier
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            stageOptions.forEach { stage ->
                DropdownMenuItem(
                    text = { Text(stage) },
                    onClick = {
                        onValueChange(stage)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LoadingAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("AI_network.json"))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )
    
    // Lottieアニメーション開始のLog出力
    LaunchedEffect(Unit) {
        android.util.Log.d("LoadingAnimation", "=== Lottieアニメーション開始 ===")
        android.util.Log.d("LoadingAnimation", "LoadingAnimation（AI_network.json）が表示されました")
        android.util.Log.d("LoadingAnimation", "アニメーション開始時刻: ${System.currentTimeMillis()}")
    }
    
    LottieAnimation(
        composition = composition,
        progress = { progress }
    )
}

@Composable
fun SukesanGifAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Loading screen.json"))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )
    
    // Lottieアニメーション開始のLog出力
    LaunchedEffect(Unit) {
        android.util.Log.d("SukesanGifAnimation", "=== Lottieアニメーション開始 ===")
        android.util.Log.d("SukesanGifAnimation", "AIで解析ボタンで処理中に表示されるLottieアニメーション")
        android.util.Log.d("SukesanGifAnimation", "アニメーション開始時刻: ${System.currentTimeMillis()}")
    }
    
    // ウィンドウサイズとアニメーションサイズをLog出力（ダイアログと同じ幅に設定）
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val windowWidthDp = configuration.screenWidthDp
    val windowHeightDp = configuration.screenHeightDp
    val screenDensity = density.density
    
    // ダイアログと同じ幅を計算（Card padding 16dp + Column padding 20dp = 36dp）
    val dialogPadding = 16.dp + 20.dp // ダイアログの合計padding
    val dialogActualWidthDp = windowWidthDp - dialogPadding.value
    val animationWidthRatio = dialogActualWidthDp / windowWidthDp
    
    LaunchedEffect(Unit) {
        android.util.Log.d("SukesanGifAnimation", "=== アニメーションサイズ（ダイアログと同じ幅） ===")
        android.util.Log.d("SukesanGifAnimation", "ウィンドウ幅: ${windowWidthDp}dp")
        android.util.Log.d("SukesanGifAnimation", "ウィンドウ高: ${windowHeightDp}dp")
        android.util.Log.d("SukesanGifAnimation", "画面密度: ${screenDensity}")
        android.util.Log.d("SukesanGifAnimation", "ダイアログpadding: ${dialogPadding.value}dp")
        android.util.Log.d("SukesanGifAnimation", "ダイアログ実際の幅: ${dialogActualWidthDp}dp")
        android.util.Log.d("SukesanGifAnimation", "アニメーション幅比率: ${(animationWidthRatio * 100).toInt()}%")
        android.util.Log.d("SukesanGifAnimation", "アニメーション幅(px): ${density.run { dialogActualWidthDp.dp.toPx() }}px")
        android.util.Log.d("SukesanGifAnimation", "Pixel 7解像度: 1080x2100px")
    }
    
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth(animationWidthRatio) // ダイアログと同じ幅
            .aspectRatio(1f) // 正方形を維持
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilySelector(
    label: String = "科名",
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val familyOptions = listOf(
        "アブラナ科",
        "アマランサス科",
        "アカザ科", // ホウレンソウなど
        "イネ科",
        "ウリ科",
        "キク科",
        "シソ科", // ハーブ
        "セリ科",
        "ヒガンバナ科", // ネギ・ニンニク
        "ユリ科（ネギ類）", // UI用に明記
        "ナス科",
        "バラ科",
        "ヒルガオ科",
        "マメ科",
        "ミカン科",
        "ショウガ科", // 補助
        "アオイ科", // オクラ
        "その他"
    )
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            familyOptions.forEach { family ->
                DropdownMenuItem(
                    text = { Text(family) },
                    onClick = {
                        onValueChange(family)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CropConfirmDialog(viewModel: SeedInputViewModel) {
    val ctx = LocalContext.current
    if (!viewModel.showCropConfirmDialog) return
    val preview = viewModel.pendingCropBitmap

    AlertDialog(
        onDismissRequest = { viewModel.cancelCropReplace() },
        title = { Text("画像を差し替えますか？") },
        text = {
            if (preview != null) {
                Image(
                    bitmap = preview.asImageBitmap(),
                    contentDescription = "切り抜きプレビュー",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 360.dp)
                )
            } else {
                Text("プレビューを表示できませんでした")
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.confirmCropReplace(ctx) }) {
                Text("はい")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.cancelCropReplace() }) {
                Text("いいえ")
            }
        }
    )
}
