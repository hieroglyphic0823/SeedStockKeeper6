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
    val seeds = if (isPreview || viewModel.seeds.value.isNotEmpty()) {
        // プレビュー時：ViewModelからデータを取得
        viewModel.seeds.value
    } else {
        // 実装時：Firebaseからデータを取得
        val db = Firebase.firestore
        val auth = FirebaseAuth.getInstance()
        val currentUid = auth.currentUser?.uid ?: ""
        var firebaseSeeds by remember { mutableStateOf(listOf<SeedPacket>()) }
        
        DisposableEffect(currentUid) {
            val registration = if (currentUid.isNotEmpty()) {
                db.collection("seeds")
                    .whereEqualTo("ownerUid", currentUid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.w("CalendarScreen", "Listen failed.", error)
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
            } else {
                null
            }
            
            onDispose {
                registration?.remove()
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
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = "カレンダー"
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
                showHarvest = showHarvest
            )
        }
    }
}

@Composable
fun GanttChartCalendar(
    seeds: List<SeedPacket>,
    showSowing: Boolean,
    showHarvest: Boolean
) {
    val today = java.time.LocalDate.now()
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
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column {
            // ヘッダー行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .height(24.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左側：商品名ヘッダー
                Text(
                    text = "商品名",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(80.dp)
                )
                
                    // 右側：月ヘッダー（横スクロール可能）
                    Canvas(
                        modifier = Modifier
                            .height(24.dp)
                            .horizontalScroll(scrollState)
                    ) {
                    val outlineColor = Color(0xFF79747E) // Material3 outline color
                    val thinLineColor = outlineColor.copy(alpha = 0.3f)
                    
                    // 月ヘッダーの背景
                    drawRect(
                        color = Color(0xFFF3E5F5), // Material3 secondaryContainer color
                        size = size
                    )
                    
                    // 月の境界線とラベル
                    months.forEachIndexed { index, (month, year) ->
                        val x = index * 60.dp.toPx()
                        
                        // 月の境界線
                        drawLine(
                            color = outlineColor,
                            start = androidx.compose.ui.geometry.Offset(x, 0f),
                            end = androidx.compose.ui.geometry.Offset(x, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                        
                        // 月の三分割ライン
                        val firstThirdX = x + 20.dp.toPx()
                        val secondThirdX = x + 40.dp.toPx()
                        
                        drawLine(
                            color = thinLineColor,
                            start = androidx.compose.ui.geometry.Offset(firstThirdX, 0f),
                            end = androidx.compose.ui.geometry.Offset(firstThirdX, size.height),
                            strokeWidth = 0.5.dp.toPx()
                        )
                        
                        drawLine(
                            color = thinLineColor,
                            start = androidx.compose.ui.geometry.Offset(secondThirdX, 0f),
                            end = androidx.compose.ui.geometry.Offset(secondThirdX, size.height),
                            strokeWidth = 0.5.dp.toPx()
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
                            .horizontalScroll(scrollState)
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
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            // データ行
            LazyColumn {
                items(seeds) { seed: SeedPacket ->
                    GanttChartRow(
                        seed = seed,
                        months = months,
                        showSowing = showSowing,
                        showHarvest = showHarvest,
                        scrollState = scrollState
                    )
                }
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
    scrollState: ScrollState
) {
    val today = java.time.LocalDate.now()
    val currentMonth = today.monthValue
    val currentYear = today.year
    val cellWidth = 20.dp  // 上旬・中旬・下旬 1つのセル幅
    val cellWidthPx = with(LocalDensity.current) { cellWidth.toPx() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左側：商品名と品種名
        Column(modifier = Modifier.width(80.dp)) {
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

        // 背景グリッド（Canvas）
        Box(
            modifier = Modifier
                .height(40.dp)
                .horizontalScroll(scrollState)
        ) {
            Canvas(
                modifier = Modifier
                    .width((months.size * 60).dp) // 明示的に幅を指定
                    .height(40.dp)
            ) {
                val outlineColor = Color(0xFF79747E)
                val thinLineColor = outlineColor.copy(alpha = 0.3f)

                // 全体の背景
                drawRect(color = Color(0xFFFFFBFE), size = size)

                // 月ごとに3分割（上中下）
                months.forEachIndexed { index, (month, year) ->
                    val monthStartX = index * 60.dp.toPx()

                    // 月の枠線
                    drawLine(
                        color = outlineColor,
                        start = androidx.compose.ui.geometry.Offset(monthStartX, 0f),
                        end = androidx.compose.ui.geometry.Offset(monthStartX, size.height),
                        strokeWidth = 1.dp.toPx()
                    )

                    // 上中下の分割線
                    for (i in 1..2) {
                        val x = monthStartX + i * cellWidthPx
                        drawLine(
                            color = thinLineColor,
                            start = androidx.compose.ui.geometry.Offset(x, 0f),
                            end = androidx.compose.ui.geometry.Offset(x, size.height),
                            strokeWidth = 0.5.dp.toPx()
                        )
                    }
                }

                // 右端の境界
                val right = months.size * 60.dp.toPx()
                drawLine(
                    color = outlineColor,
                    start = androidx.compose.ui.geometry.Offset(right, 0f),
                    end = androidx.compose.ui.geometry.Offset(right, size.height),
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
        }

        // 前景レイヤー（バーを重ねる）
        Box(
            modifier = Modifier
                .height(40.dp)
                .horizontalScroll(scrollState)
                .offset(y = (-40).dp)
        ) {
            Row(
                modifier = Modifier.width((months.size * 60).dp) // 明示的に幅を指定
            ) {
                months.forEach { (month, year) ->
                    repeat(3) { periodIndex -> // 0=上旬, 1=中旬, 2=下旬
                        Box(
                            modifier = Modifier
                                .width(cellWidth)
                                .height(40.dp)
                                .background(
                                    Color.Transparent,
                                    RoundedCornerShape(0.dp) // 角丸なし
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // 播種期間のバー（デバッグ用ログ付き）
                            val isSowingInPeriod = seed.isSowingIn(month, year, periodIndex)
                            val shouldShowSowing = showSowing && isSowingInPeriod
                            
                            // デバッグログ出力（恋むすめの商品または現在の月の最初の期間）
                            if ((seed.productName.contains("恋むすめ") || seed.productName.contains("恋")) || 
                                (month == currentMonth && year == currentYear && periodIndex == 0)) {
                                android.util.Log.d("CalendarScreen", "=== 播種期間デバッグ ===")
                                android.util.Log.d("CalendarScreen", "現在の月: $currentMonth, 現在の年: $currentYear")
                                android.util.Log.d("CalendarScreen", "商品名: ${seed.productName}")
                                android.util.Log.d("CalendarScreen", "月: $month, 年: $year, 期間: $periodIndex")
                                android.util.Log.d("CalendarScreen", "showSowing: $showSowing")
                                android.util.Log.d("CalendarScreen", "isSowingInPeriod: $isSowingInPeriod")
                                android.util.Log.d("CalendarScreen", "shouldShowSowing: $shouldShowSowing")
                                android.util.Log.d("CalendarScreen", "calendar entries: ${seed.calendar.size}")
                                seed.calendar.forEachIndexed { index, entry ->
                                    android.util.Log.d("CalendarScreen", "  entry[$index]: region=${entry.region}, sowing_start=${entry.sowing_start_date}, sowing_end=${entry.sowing_end_date}")
                                }
                                
                                // 恋むすめの場合、期間の詳細も出力
                                if (seed.productName.contains("恋むすめ") || seed.productName.contains("恋")) {
                                    val periodName = when (periodIndex) {
                                        0 -> "上旬(1-10日)"
                                        1 -> "中旬(11-20日)"
                                        2 -> "下旬(21-31日)"
                                        else -> "不明"
                                    }
                                    android.util.Log.d("CalendarScreen", "恋むすめ詳細: ${month}月${periodName}でチェック中")
                                }
                            }
                            
                            if (shouldShowSowing) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Color(0xFFE8DEF8),
                                            RoundedCornerShape(0.dp) // 角丸なし
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.grain),
                                        contentDescription = "播種",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // 収穫期間のバー
                            if (showHarvest && seed.isHarvestIn(month, year, periodIndex)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Color(0xFFFCE7F3),
                                            RoundedCornerShape(0.dp) // 角丸なし
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.harvest),
                                        contentDescription = "収穫",
                                        modifier = Modifier.size(16.dp)
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



