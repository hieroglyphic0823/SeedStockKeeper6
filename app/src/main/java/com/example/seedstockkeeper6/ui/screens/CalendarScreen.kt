package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: SeedListViewModel,
    isPreview: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSowing by remember { mutableStateOf(true) }
    var showHarvest by remember { mutableStateOf(false) }
    
    // データの取得（プレビュー時はViewModelから、実装時はFirebaseから）
    val seeds = if (isPreview) {
        // プレビュー時：ViewModelからデータを取得
        val previewSeeds = viewModel.seeds.value
        android.util.Log.d("CalendarScreen", "プレビュー時データ取得: ${previewSeeds.size}件")
        previewSeeds.forEach { seed ->
            android.util.Log.d("CalendarScreen", "プレビュー商品: ${seed.productName}")
        }
        previewSeeds
    } else if (viewModel.seeds.value.isNotEmpty()) {
        // 実装時：ViewModelにデータがある場合はそれを使用
        viewModel.seeds.value
    } else {
        // 実装時：Firebaseからデータを取得
        val db = Firebase.firestore
        val auth = FirebaseAuth.getInstance()
        val currentUid = auth.currentUser?.uid ?: ""
        var firebaseSeeds by remember { mutableStateOf(listOf<SeedPacket>()) }
        
        DisposableEffect(currentUid) {
            var registration: com.google.firebase.firestore.ListenerRegistration? = null
            
            if (currentUid.isNotEmpty()) {
                try {
                    registration = db.collection("seeds")
                        .whereEqualTo("ownerUid", currentUid)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.w("CalendarScreen", "Listen failed: ${error.message}")
                                // エラーハンドリングを改善
                                when (error.code) {
                                    com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE -> {
                                        Log.w("CalendarScreen", "Network unavailable, using cached data")
                                    }
                                    com.google.firebase.firestore.FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                                        Log.w("CalendarScreen", "Request timeout, using cached data")
                                    }
                                    else -> {
                                        Log.e("CalendarScreen", "Firestore error: ${error.code} - ${error.message}")
                                    }
                                }
                                return@addSnapshotListener
                            }
                            
                            if (snapshot != null) {
                                val newSeeds = snapshot.documents.mapNotNull { doc ->
                                    try {
                                        doc.toObject(SeedPacket::class.java)?.copy(id = doc.id)
                                    } catch (e: Exception) {
                                        Log.w("CalendarScreen", "Failed to convert document ${doc.id} to SeedPacket", e)
                                        null
                                    }
                                }
                                firebaseSeeds = newSeeds
                            }
                        }
                } catch (e: Exception) {
                    Log.e("CalendarScreen", "Error setting up Firestore listener: ${e.message}")
                }
            }
            
            onDispose {
                try {
                    registration?.remove()
                } catch (e: Exception) {
                    Log.w("CalendarScreen", "Error removing listener: ${e.message}")
                }
            }
        }
        
        firebaseSeeds
    }
    
    // 検索フィルタリング
    val filteredSeeds = remember(seeds, searchQuery) {
        seeds.filter { seed: SeedPacket ->
            val matchesSearch = searchQuery.isEmpty() || 
                seed.productName.contains(searchQuery, ignoreCase = true) ||
                seed.variety.contains(searchQuery, ignoreCase = true) ||
                seed.family.contains(searchQuery, ignoreCase = true)
            
            matchesSearch
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // 検索バー
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("商品名・品種・科で検索") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "検索"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        // カレンダー表示フィルター
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = showSowing,
                    onCheckedChange = { showSowing = it }
                )
                Text(
                    text = "播種",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = showHarvest,
                    onCheckedChange = { showHarvest = it }
                )
                Text(
                    text = "収穫",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // ガントチャート風カレンダー
        if (filteredSeeds.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "該当する種子が見つかりません",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            GanttChartCalendar(
                seeds = filteredSeeds,
                showSowing = showSowing,
                showHarvest = showHarvest,
                isPreview = isPreview
            )
        }
    }
}

@Composable
fun GanttChartCalendar(
    seeds: List<SeedPacket>,
    showSowing: Boolean,
    showHarvest: Boolean,
    isPreview: Boolean = false
) {
    val today = if (isPreview) {
        java.time.LocalDate.of(2025, 5, 1) // プレビュー時は2025年5月1日を使用
    } else {
        java.time.LocalDate.now()
    }
    val currentMonth = today.monthValue
    val currentYear = today.year
    
    // 今月から2年分の月リストを生成
    val months = remember {
        (0 until 24).map { offset ->
            val date = java.time.LocalDate.of(currentYear, currentMonth, 1).plusMonths(offset.toLong())
            date.monthValue to date.year
        }
    }
    
    // 横スクロールの状態を共有
    val scrollState = rememberScrollState()
    
    // MaterialTheme.colorSchemeの値を抽出
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer
    val outlineColor = MaterialTheme.colorScheme.outline
    
    Column {
            // ヘッダー行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(secondaryContainerColor)
                    .height(24.dp)
                    .drawWithContent {
                        drawContent()
                        // 上の境界線を描画
                        drawLine(
                            color = outlineColor,
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                        // 下の境界線を描画
                        drawLine(
                            color = outlineColor,
                            start = androidx.compose.ui.geometry.Offset(0f, size.height),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                        // 左の境界線を描画（商品名部分のみ）
                        drawLine(
                            color = outlineColor,
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(0f, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左側：商品名ヘッダー
                Text(
                    text = "商品名",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .width(80.dp)
                        .padding(horizontal = 16.dp)
                )
                
                // 右側：月ヘッダー（横スクロール可能）
                val outlineColor = MaterialTheme.colorScheme.outline
                val thinLineColor = outlineColor.copy(alpha = 0.3f)
                val headerBackgroundColor = MaterialTheme.colorScheme.secondaryContainer
                
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .horizontalScroll(scrollState)
                ) {
                    Canvas(
                        modifier = Modifier
                            .height(24.dp)
                            .width((months.size * 3 * 20).dp) // cellWidth = 20dp
                    ) {
                        // 月ヘッダーの背景
                        drawRect(
                            color = headerBackgroundColor,
                            size = size
                        )
                        
                        // 月の境界線とラベル
                        months.forEachIndexed { index, (month, year) ->
                            val x = index * 3 * 20.dp.toPx() // cellWidth = 20dp
                            
                            // 月の境界線
                            drawLine(
                                color = outlineColor,
                                start = androidx.compose.ui.geometry.Offset(x, 0f),
                                end = androidx.compose.ui.geometry.Offset(x, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                            
                        }
                        
                        // 右端の線
                        drawLine(
                            color = outlineColor,
                            start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                        
                        // 上下の境界線
                        drawLine(
                            color = outlineColor,
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = outlineColor,
                            start = androidx.compose.ui.geometry.Offset(0f, size.height),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    
                    // 月ラベルを重ねて表示
                    Row(
                        modifier = Modifier
                            .height(24.dp)
                            .width((months.size * 3 * 20).dp) // cellWidth = 20dp
                            .offset(y = 0.dp), // Canvasと同じ位置に重ねる
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        months.forEach { (month, year) ->
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(24.dp)
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${month}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            // データ行
            LazyColumn(
                verticalArrangement = Arrangement.Top
            ) {
                items(seeds) { seed: SeedPacket ->
                    GanttChartRow(
                        seed = seed,
                        months = months,
                        showSowing = showSowing,
                        showHarvest = showHarvest,
                        scrollState = scrollState,
                        isPreview = isPreview
                    )
                }
            }
        }
}

@Composable
fun GanttChartRow(
    seed: SeedPacket,
    months: List<Pair<Int, Int>>,
    showSowing: Boolean,
    showHarvest: Boolean,
    scrollState: ScrollState,
    isPreview: Boolean = false
) {
    val today = if (isPreview) {
        java.time.LocalDate.of(2025, 5, 1) // プレビュー時は2025年5月1日を使用
    } else {
        java.time.LocalDate.now()
    }
    val currentMonth = today.monthValue
    val currentYear = today.year
    val cellWidth = 20.dp  // 上旬・中旬・下旬 1つのセル幅
    val cellWidthPx = with(LocalDensity.current) { cellWidth.toPx() }
    
    // MaterialTheme.colorSchemeの値を抽出
    val surfaceContainerLowColor = MaterialTheme.colorScheme.surfaceContainerLow
    val outlineColor = MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(surfaceContainerLowColor)
            .drawWithContent {
                drawContent()
                // 下の境界線を描画
                drawLine(
                    color = outlineColor,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                // 左の境界線を描画（商品名部分のみ）
                drawLine(
                    color = outlineColor,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(0f, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左側：商品名と品種名
        Column(
            modifier = Modifier
                .width(80.dp)
                .padding(horizontal = 0.dp, vertical = 0.dp)
        ) {
            Text(
                text = seed.productName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = seed.variety,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 右側：カレンダー部分（横スクロール可能）
        val gridOutlineColor = MaterialTheme.colorScheme.outline
        val gridThinLineColor = gridOutlineColor.copy(alpha = 0.3f)
        val gridBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest // カレンダー部の色
        val sowingBarColor = MaterialTheme.colorScheme.primaryContainer // 播種期間: PrimaryContainer
        val harvestBarColor = MaterialTheme.colorScheme.secondary // 収穫期間: Secondary
        
        Box(
            modifier = Modifier
                .height(40.dp)
                .horizontalScroll(scrollState)
        ) {
            // 背景グリッド（Canvas）
            Canvas(
                modifier = Modifier
                    .width((months.size * 3 * cellWidth.value).dp) // 明示的に幅を指定
                    .height(40.dp)
            ) {
                // 全体の背景
                drawRect(color = gridBackgroundColor, size = size)

                // 月ごとに3分割（上中下）
                months.forEachIndexed { index, (month, year) ->
                    val monthStartX = index * 3 * cellWidthPx

                    // 月の枠線
                    drawLine(
                        color = gridOutlineColor,
                        start = androidx.compose.ui.geometry.Offset(monthStartX, 0f),
                        end = androidx.compose.ui.geometry.Offset(monthStartX, size.height),
                        strokeWidth = 1.dp.toPx()
                    )

                    // 上中下の分割線
                    for (i in 1..2) {
                        val x = monthStartX + i * cellWidthPx
                        drawLine(
                            color = gridThinLineColor,
                            start = androidx.compose.ui.geometry.Offset(x, 0f),
                            end = androidx.compose.ui.geometry.Offset(x, size.height),
                            strokeWidth = 0.5.dp.toPx()
                        )
                    }
                }

                // 左端の境界
                drawLine(
                    color = gridOutlineColor,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(0f, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                
                // 右端の境界
                val right = months.size * 3 * cellWidthPx
                drawLine(
                    color = gridOutlineColor,
                    start = androidx.compose.ui.geometry.Offset(right, 0f),
                    end = androidx.compose.ui.geometry.Offset(right, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                
                // 上下の境界線
                drawLine(
                    color = gridOutlineColor,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = gridOutlineColor,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 前景レイヤー（バーを重ねる）
            Row(
                modifier = Modifier.width((months.size * 3 * cellWidth.value).dp) // 明示的に幅を指定
            ) {
                months.forEach { (month, year) ->
                    repeat(3) { periodIndex -> // 0=上旬, 1=中旬, 2=下旬
                        Box(
                            modifier = Modifier
                                .width(cellWidth)
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // 播種期間のバー（デバッグ用ログ付き）
                            val isSowingInPeriod = seed.isSowingIn(month, year, periodIndex, isPreview)
                            val shouldShowSowing = showSowing && isSowingInPeriod
                            
                            // デバッグログ出力（今月まけるにんじんの商品のみ）
                            if (seed.productName.contains("今月まけるにんじん")) {
                                val periodName = when (periodIndex) {
                                    0 -> "上旬(1-10日)"
                                    1 -> "中旬(11-20日)"
                                    2 -> "下旬(21-31日)"
                                    else -> "不明"
                                }
                                android.util.Log.d("CalendarScreen", "播種期間チェック: ${seed.productName}, ${year}年${month}月${periodName}, isSowingInPeriod=$isSowingInPeriod, shouldShowSowing=$shouldShowSowing")
                            }
                            
                            if (shouldShowSowing) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 8.dp) // 上下に8dpの余白
                                        .background(
                                            sowingBarColor,
                                            RoundedCornerShape(0.dp) // 角丸なし
                                        )
                                        .height(20.dp) // 高さをアイコンと同じ20dpに変更
                                        .width(20.dp), // 幅を少し大きく
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.germination),
                                        contentDescription = "播種",
                                        modifier = Modifier.size(20.dp), // アイコンサイズを大きく
                                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                                    )
                                }
                            }

                            // 収穫期間のバー
                            if (showHarvest && seed.isHarvestIn(month, year, periodIndex, isPreview)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 8.dp) // 上下に8dpの余白
                                        .background(
                                            harvestBarColor,
                                            RoundedCornerShape(0.dp) // 角丸なし
                                        )
                                        .height(20.dp) // 高さをアイコンと同じ20dpに変更
                                        .width(20.dp), // 幅を少し大きく
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.harvest),
                                        contentDescription = "収穫",
                                        modifier = Modifier.size(20.dp) // アイコンサイズを大きく
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



