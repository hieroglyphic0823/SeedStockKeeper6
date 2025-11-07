package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
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
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import java.time.temporal.ChronoUnit
import java.time.LocalDate
import java.time.YearMonth
import com.example.seedstockkeeper6.util.normalizeFamilyName
import com.example.seedstockkeeper6.ui.theme.backgroundLightMediumContrast
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import com.google.gson.Gson
import java.net.URLEncoder

/**
 * Colorをグレースケールに変換する拡張関数
 */
private fun Color.toGrayscale(): Color {
    // RGB値を取得（0.0-1.0の範囲）
    val red = this.red
    val green = this.green
    val blue = this.blue
    
    // 輝度を計算（0.299*R + 0.587*G + 0.114*B）
    val gray = 0.299f * red + 0.587f * green + 0.114f * blue
    
    // グレースケールのColorを作成（透明度は維持）
    return Color(red = gray, green = gray, blue = gray, alpha = this.alpha)
}

/**
 * 種暦画面用フィルターカードコンポーネント
 */
@Composable
fun CalendarFilterCard(
    showUrgentSeeds: Boolean,
    onUrgentSeedsChange: (Boolean) -> Unit,
    showThisMonthSeeds: Boolean,
    onThisMonthSeedsChange: (Boolean) -> Unit,
    showNormalSeeds: Boolean,
    onNormalSeedsChange: (Boolean) -> Unit,
    showFinishedSeeds: Boolean,
    onFinishedSeedsChange: (Boolean) -> Unit,
    showExpiredSeeds: Boolean,
    onExpiredSeedsChange: (Boolean) -> Unit,
    showFilters: Boolean,
    onFiltersToggle: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sortType: SortType,
    onSortTypeChange: (SortType) -> Unit
) {
    // 並べ替えダイアログの表示状態
    var showSortDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 1行目：並べ替え、吟味アイコン（右揃え）
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 並べ替えアイコンボタン（左）
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { showSortDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapVert,
                        contentDescription = "並べ替え",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "並べ替え",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 吟味アイコンボタン（右端）
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { onFiltersToggle() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterAlt,
                        contentDescription = "吟味",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "吟味",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // 並べ替えダイアログ
            if (showSortDialog) {
                AlertDialog(
                    onDismissRequest = { showSortDialog = false },
                    title = { Text("並べ替え") },
                    text = {
                        Column {
                            SortType.values().forEach { type ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            onSortTypeChange(type)
                                            showSortDialog = false
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = sortType == type,
                                        onClick = { 
                                            onSortTypeChange(type)
                                            showSortDialog = false
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary,
                                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = type.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSortDialog = false }) {
                            Text("閉じる")
                        }
                    }
                )
            }
            
            // フィルター用ボタンと検索ボックス（条件付き表示）
            if (showFilters) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1行目：「終了間近」「まきどき」「通常」（重要度順）
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 終了間近アイコン+状態名ボタン（重要度1）
                        val urgentContainerColor = if (showUrgentSeeds) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer.toGrayscale()
                        }
                        val urgentContentColor = if (showUrgentSeeds) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer.toGrayscale()
                        }
                        Row(
                            modifier = Modifier
                                .background(
                                    color = urgentContainerColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onUrgentSeedsChange(!showUrgentSeeds) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.warning),
                                contentDescription = "終了間近",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = "終了間近",
                                style = MaterialTheme.typography.bodyMedium,
                                color = urgentContentColor
                            )
                        }
                        
                        // まきどきアイコン+状態名ボタン（重要度2）
                        val thisMonthContainerColor = if (showThisMonthSeeds) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.toGrayscale()
                        }
                        val thisMonthContentColor = if (showThisMonthSeeds) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer.toGrayscale()
                        }
                        Row(
                            modifier = Modifier
                                .background(
                                    color = thisMonthContainerColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onThisMonthSeedsChange(!showThisMonthSeeds) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.seed_bag_enp),
                                contentDescription = "まきどき",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = "まきどき",
                                style = MaterialTheme.typography.bodyMedium,
                                color = thisMonthContentColor
                            )
                        }
                        
                        // 通常アイコン+状態名ボタン（重要度3）
                        val normalContainerColor = if (showNormalSeeds) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer.toGrayscale()
                        }
                        val normalContentColor = if (showNormalSeeds) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onTertiaryContainer.toGrayscale()
                        }
                        Row(
                            modifier = Modifier
                                .background(
                                    color = normalContainerColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onNormalSeedsChange(!showNormalSeeds) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.seed_bag_full),
                                contentDescription = "通常",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = "通常",
                                style = MaterialTheme.typography.bodyMedium,
                                color = normalContentColor
                            )
                        }
                    }
                    
                    // 2行目：「まき終わり」「期限切れ」（重要度順）
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // まき終わりアイコン+状態名ボタン（重要度4）
                        val finishedContainerColor = if (showFinishedSeeds) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer.toGrayscale()
                        }
                        val finishedContentColor = if (showFinishedSeeds) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer.toGrayscale()
                        }
                        Row(
                            modifier = Modifier
                                .background(
                                    color = finishedContainerColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onFinishedSeedsChange(!showFinishedSeeds) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.seed),
                                contentDescription = "まき終わり",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = "まき終わり",
                                style = MaterialTheme.typography.bodyMedium,
                                color = finishedContentColor
                            )
                        }
                        
                        // 期限切れアイコン+状態名ボタン（重要度5）
                        val expiredContainerColor = if (showExpiredSeeds) {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest.toGrayscale()
                        }
                        val expiredContentColor = if (showExpiredSeeds) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.toGrayscale()
                        }
                        Row(
                            modifier = Modifier
                                .background(
                                    color = expiredContainerColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onExpiredSeedsChange(!showExpiredSeeds) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.close),
                                contentDescription = "期限切れ",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Text(
                                text = "期限切れ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = expiredContentColor
                            )
                        }
                    }
                    
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 検索ボックス
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("種を検索") },
                    placeholder = { Text("商品名、品種、科名で検索") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: SeedListViewModel,
    isPreview: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    // まきどき・収穫は常に表示（チェックボックス削除のため）
    val showSowing = true
    val showHarvest = true
    
    // 種の状態によるフィルタリング（種目録画面と同じ）
    var showUrgentSeeds by remember { mutableStateOf(true) }         // 終了間近
    var showThisMonthSeeds by remember { mutableStateOf(true) }      // まきどき
    var showNormalSeeds by remember { mutableStateOf(true) }          // 通常
    var showFinishedSeeds by remember { mutableStateOf(true) }        // まき終わり
    var showExpiredSeeds by remember { mutableStateOf(true) }         // 期限切れ
    
    // 抽出条件の表示状態
    var showFilters by remember { mutableStateOf(false) }  // 吟味をクリックしたときのみ表示
    
    // 並べ替えの状態
    var sortType by remember { mutableStateOf(SortType.IMPORTANCE) }
    
    // データの取得（プレビュー時はViewModelから、実装時はFirebaseリスナーで常に最新データを取得）
    val seeds = if (isPreview) {
        // プレビュー時：ViewModelからデータを取得
        val previewSeeds = viewModel.seeds.value
        previewSeeds.forEach { seed ->
        }
        previewSeeds
    } else {
        // 実装時：Firebaseリスナーで常に最新データを取得（種覚書画面での変更も反映される）
        val db = Firebase.firestore
        val auth = FirebaseAuth.getInstance()
        val currentUid = auth.currentUser?.uid ?: ""
        // 初期値としてViewModelのデータを使用（ある場合）
        var firebaseSeeds by remember { mutableStateOf(viewModel.seeds.value) }
        
        DisposableEffect(currentUid) {
            var registration: com.google.firebase.firestore.ListenerRegistration? = null
            
            if (currentUid.isNotEmpty()) {
                try {
                    registration = db.collection("seeds")
                        .whereEqualTo("ownerUid", currentUid)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                // エラーハンドリングを改善
                                when (error.code) {
                                    com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE -> {
                                        android.util.Log.w("CalendarScreen", "Firestore unavailable")
                                    }
                                    com.google.firebase.firestore.FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                                        android.util.Log.w("CalendarScreen", "Firestore deadline exceeded")
                                    }
                                    else -> {
                                        android.util.Log.e("CalendarScreen", "Firestore error: ${error.message}")
                                    }
                                }
                                return@addSnapshotListener
                            }
                            
                            if (snapshot != null) {
                                val newSeeds = snapshot.documents.mapNotNull { doc ->
                                    try {
                                        doc.toObject(SeedPacket::class.java)?.copy(id = doc.id)
                                    } catch (e: Exception) {
                                        android.util.Log.e("CalendarScreen", "Failed to parse seed: ${e.message}", e)
                                        null
                                    }
                                }
                                android.util.Log.d("CalendarScreen", "Firebaseリスナー: データ更新 - ${newSeeds.size}件")
                                firebaseSeeds = newSeeds
                                // ViewModelも更新（他の画面でも最新データを使用できるように）
                                viewModel.loadSeeds()
                            }
                        }
                } catch (e: Exception) {
                    android.util.Log.e("CalendarScreen", "Firebaseリスナー設定エラー: ${e.message}", e)
                }
            }
            
            onDispose {
                try {
                    registration?.remove()
                    android.util.Log.d("CalendarScreen", "Firebaseリスナー解除")
                } catch (e: Exception) {
                    android.util.Log.e("CalendarScreen", "Firebaseリスナー解除エラー: ${e.message}", e)
                }
            }
        }
        
        firebaseSeeds
    }
    
    // 重要度の順位（数値が小さいほど優先度が高い）
    fun getImportanceOrder(status: String): Int = when (status) {
        "urgent" -> 1      // 期限間近
        "thisMonth" -> 2   // まきどき
        "normal" -> 3      // 通常
        "finished" -> 4    // まき終わり
        "expired" -> 5     // 期限切れ
        else -> 6
    }
    
    // 検索フィルタリングと種の状態によるフィルタリング
    val filteredSeeds = remember(seeds, searchQuery, showUrgentSeeds, showThisMonthSeeds, showNormalSeeds, showFinishedSeeds, showExpiredSeeds, sortType, isPreview) {
        val filtered = seeds.filter { seed: SeedPacket ->
            val matchesSearch = searchQuery.isEmpty() || 
                seed.productName.contains(searchQuery, ignoreCase = true) ||
                seed.variety.contains(searchQuery, ignoreCase = true) ||
                seed.family.contains(searchQuery, ignoreCase = true)
            
            val seedStatus = getSeedStatus(seed)
            val matchesStatus = when (seedStatus) {
                "thisMonth" -> showThisMonthSeeds
                "urgent" -> showUrgentSeeds
                "expired" -> showExpiredSeeds
                "finished" -> showFinishedSeeds
                "normal" -> showNormalSeeds
                else -> true
            }
            
            matchesSearch && matchesStatus
        }
        
        // 並べ替え処理
        when (sortType) {
            SortType.IMPORTANCE -> filtered.sortedBy { getImportanceOrder(getSeedStatus(it)) }
            SortType.REGISTRATION -> filtered.reversed() // 登録順（新しい順）
            SortType.NAME -> filtered.sortedBy { it.productName }
            SortType.STATUS -> filtered.sortedBy { getSeedStatus(it) }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // 抽出条件カード（種目録画面と同じスタイル）
        CalendarFilterCard(
            showUrgentSeeds = showUrgentSeeds,
            onUrgentSeedsChange = { showUrgentSeeds = it },
            showThisMonthSeeds = showThisMonthSeeds,
            onThisMonthSeedsChange = { showThisMonthSeeds = it },
            showNormalSeeds = showNormalSeeds,
            onNormalSeedsChange = { showNormalSeeds = it },
            showFinishedSeeds = showFinishedSeeds,
            onFinishedSeedsChange = { showFinishedSeeds = it },
            showExpiredSeeds = showExpiredSeeds,
            onExpiredSeedsChange = { showExpiredSeeds = it },
            showFilters = showFilters,
            onFiltersToggle = { showFilters = !showFilters },
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            sortType = sortType,
            onSortTypeChange = { sortType = it }
        )
        
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
                isPreview = isPreview,
                navController = navController
            )
        }
    }
}

@Composable
fun GanttChartCalendar(
    seeds: List<SeedPacket>,
    showSowing: Boolean,
    showHarvest: Boolean,
    isPreview: Boolean = false,
    navController: NavController? = null
) {
    val today = if (isPreview) {
        LocalDate.of(2025, 5, 1) // プレビュー時は2025年5月1日を使用
    } else {
        LocalDate.now()
    }
    
    // 📅 現在の月から3カ月前を起点に、2年分先までのカレンダー期間を計算
    val calendarStartDate = LocalDate.of(today.year, today.monthValue, 1).minusMonths(3) // 過去3カ月分も表示
    val calendarEndDate = calendarStartDate.plusYears(2).minusMonths(1) // 開始から2年分先まで
    
    // カレンダーの総月数を計算（過去3カ月 + 2年分 = 27ヶ月）
    val totalMonths = ChronoUnit.MONTHS.between(calendarStartDate, calendarEndDate).toInt() + 1
    
    // カレンダー全体の月のリストを作成
    val months = remember {
        List(totalMonths) { i ->
            val date = calendarStartDate.plusMonths(i.toLong())
            date.monthValue to date.year
        }
    }
    
    // 📅 当月のインデックスを計算
    val todayMonthStart = LocalDate.of(today.year, today.monthValue, 1)
    val currentMonthIndex = ChronoUnit.MONTHS.between(calendarStartDate, todayMonthStart).toInt()
    
    // 横スクロールの状態を共有
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    
    // 今月の位置にスクロール（初期表示）
    LaunchedEffect(Unit) {
        val cellWidth = 20.dp
        val scrollPosition = currentMonthIndex * 3 * with(density) { cellWidth.toPx() }
        scrollState.scrollTo(scrollPosition.toInt())
    }
    
    // MaterialTheme.colorSchemeの値を抽出
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer
    val outlineColor = MaterialTheme.colorScheme.surfaceContainerLowest // 背景色と同じ色に変更
    
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
                val outlineColor = MaterialTheme.colorScheme.surfaceContainerLowest // 背景色と同じ色に変更
                val thinLineColor = MaterialTheme.colorScheme.surfaceContainerLowest // 背景色と同じ色に変更
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
                        isPreview = isPreview,
                        navController = navController,
                        calendarStartDate = calendarStartDate,
                        calendarEndDate = calendarEndDate
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
    isPreview: Boolean = false,
    navController: NavController? = null,
    calendarStartDate: LocalDate,
    calendarEndDate: LocalDate
) {
    val today = if (isPreview) {
        LocalDate.of(2025, 5, 1) // プレビュー時は2025年5月1日を使用
    } else {
        LocalDate.now()
    }
    val currentMonth = today.monthValue
    val currentYear = today.year
    val cellWidth = 20.dp  // 上旬・中旬・下旬 1つのセル幅
    val cellWidthPx = with(LocalDensity.current) { cellWidth.toPx() }
    val density = LocalDensity.current
    val context = LocalContext.current
    
    // 🌾 まきどきアイコンと🥕 収穫アイコンのぷるぷる揺れアニメーション（共通）
    val harvestShakeTransition = rememberInfiniteTransition(label = "harvestShake")
    val shakeRotation by harvestShakeTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                -6f at 0
                6f at 100
                -3f at 200
                3f at 300
                -6f at 400
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeRotation"
    )
    
    // 高さの計算：まきどきと収穫の両方が表示される場合は80dp、片方のみの場合は40dp
    val rowHeight = if (showSowing && showHarvest) 80.dp else 40.dp
    val halfHeight = 40.dp // 常に40dp（両方表示時は上下に分割）
    
    // MaterialTheme.colorSchemeの値を抽出
    val surfaceContainerLowColor = MaterialTheme.colorScheme.surfaceContainerLow
    val outlineColor = MaterialTheme.colorScheme.surfaceContainerLowest // 背景色と同じ色に変更
    val surfaceContainerLowestColor = MaterialTheme.colorScheme.surfaceContainerLowest // 背景色を事前に取得
    val errorContainerColor = MaterialTheme.colorScheme.errorContainer // 期限切れの月の色
    val surfaceContainerHighestColor = MaterialTheme.colorScheme.surfaceContainerHighest // 有効期限の月以降のグレーアウト色
    // 播種期間の背景色定義（種目録のカレンダーと同じ）
    val sowingExpiredBackgroundColor = backgroundLightMediumContrast // 有効期限の月の色
    val sowingExpiredGrayColor = surfaceContainerHighestColor // 有効期限の月以降（お城画面の期限切れカードと同じ色）

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(surfaceContainerLowColor)
            .drawWithContent {
                drawContent()
                // 下の境界線を描画（白に変更）
                drawLine(
                    color = Color.White,
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
        // 左側：商品名と品種名（種の状態の背景色と状態アイコン付き）
        val seedStatus = getSeedStatus(seed)
        val backgroundColor = when (seedStatus) {
            "finished" -> MaterialTheme.colorScheme.secondaryContainer  // まき終わり
            "expired" -> MaterialTheme.colorScheme.surfaceContainerHighest      // 期限切れ：淡グレ
            "urgent" -> MaterialTheme.colorScheme.errorContainer  // 強い赤系：終了間近を強調
            "thisMonth" -> MaterialTheme.colorScheme.primaryContainer       // 黄色系：まきどき
            else -> MaterialTheme.colorScheme.tertiaryContainer             // 緑系：通常
        }
        val statusIconResId = when (seedStatus) {
            "finished" -> R.drawable.seed  // まき終わり：seed
            "urgent" -> R.drawable.warning  // 期限間近：warning
            "thisMonth" -> R.drawable.seed_bag_enp  // まきどき：seed_bag_enp
            "expired" -> R.drawable.close  // 期限切れ：close
            else -> R.drawable.seed_bag_full  // 通常：seed_bag_full
        }
        
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(rowHeight)
                .background(backgroundColor)
                .clickable(enabled = navController != null) {
                    navController?.let {
                        val encodedSeed = URLEncoder.encode(Gson().toJson(seed), "UTF-8")
                        it.navigate("input/$encodedSeed")
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp)
                    .wrapContentHeight(Alignment.CenterVertically),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = seed.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (seedStatus) {
                        "finished" -> MaterialTheme.colorScheme.onSecondaryContainer
                        "expired" -> MaterialTheme.colorScheme.onSurface
                        "urgent" -> MaterialTheme.colorScheme.onErrorContainer
                        "thisMonth" -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onTertiaryContainer
                    },
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
            
            // 状態アイコンを商品名の右下に表示
            Icon(
                painter = painterResource(id = statusIconResId),
                contentDescription = when (seedStatus) {
                    "finished" -> "まき終わり"
                    "urgent" -> "期限間近"
                    "thisMonth" -> "まきどき"
                    "expired" -> "期限切れ"
                    else -> "通常"
                },
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp, bottom = 2.dp),
                tint = Color.Unspecified
            )
        }

        // 右側：カレンダー部分（横スクロール可能）
        val gridOutlineColor = MaterialTheme.colorScheme.surfaceContainerLowest // 背景色と同じ色に変更
        val gridThinLineColor = MaterialTheme.colorScheme.surfaceContainerLowest // 背景色と同じ色に変更
        val gridBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLow // カレンダー部の色をsurfaceContainerLowに変更
        val sowingBarColor = MaterialTheme.colorScheme.primaryContainer // 播種期間: PrimaryContainer
        val harvestBarColor = MaterialTheme.colorScheme.primary // 収穫期間: Primary（種目録の種暦と同じ）
        
        Box(
            modifier = Modifier
                .height(rowHeight)
                .horizontalScroll(scrollState)
        ) {
            // 背景グリッド（Canvas）
            Canvas(
                modifier = Modifier
                    .width((months.size * 3 * cellWidth.value).dp) // 明示的に幅を指定
                    .height(rowHeight)
            ) {
                // 有効期限の判定用
                val expirationDate = try {
                    if (seed.expirationMonth > 0) {
                        YearMonth.of(seed.expirationYear, seed.expirationMonth)
                    } else {
                        YearMonth.of(9999, 12) // 有効期限なしの場合は非常に遠い未来の日付を設定
                    }
                } catch (e: Exception) {
                    YearMonth.of(9999, 12)
                }
                
                // 月ごとに上半分と下半分を分けて背景色を描画（種目録のカレンダーと同じロジック）
                months.forEachIndexed { index, (month, year) ->
                    val monthStartX = index * 3 * cellWidthPx
                    val monthWidth = 3 * cellWidthPx
                    val currentMonthDate = YearMonth.of(year, month)
                    
                    // 上半分の背景色（播種期間表示部分）を有効期限に応じて変更
                    val halfHeightPx = size.height / 2f
                    val topHalfBackgroundColor = when {
                        currentMonthDate < expirationDate -> {
                            // 有効期限の月より前：通常色
                            surfaceContainerLowColor
                        }
                        currentMonthDate == expirationDate -> {
                            // 有効期限の月：backgroundLightMediumContrast
                            sowingExpiredBackgroundColor
                        }
                        else -> {
                            // 有効期限の月より後：グレーアウト
                            sowingExpiredGrayColor
                        }
                    }
                    
                    // 上半分の背景を描画（播種期間表示部分）
                    drawRect(
                        color = topHalfBackgroundColor,
                        topLeft = androidx.compose.ui.geometry.Offset(monthStartX, 0f),
                        size = androidx.compose.ui.geometry.Size(monthWidth, halfHeightPx)
                    )
                    
                    // 下半分の背景を描画（収穫期間表示部分、常に通常色）
                    drawRect(
                        color = surfaceContainerLowColor,
                        topLeft = androidx.compose.ui.geometry.Offset(monthStartX, halfHeightPx),
                        size = androidx.compose.ui.geometry.Size(monthWidth, halfHeightPx)
                    )
                }
                
                // 収穫が表示される場合は中央に横線を引く
                if (showHarvest) {
                    val halfHeightPx = size.height / 2f
                    drawLine(
                        color = gridOutlineColor,
                        start = androidx.compose.ui.geometry.Offset(0f, halfHeightPx),
                        end = androidx.compose.ui.geometry.Offset(size.width, halfHeightPx),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 月ごとに3分割（上中下）
                months.forEachIndexed { index, (month, year) ->
                    val monthStartX = index * 3 * cellWidthPx
                    
                    // 期限切れの月かどうかを判定
                    val isExpired = seed.isExpired(month, year, isPreview)
                    val monthLineColor = if (isExpired) gridBackgroundColor else gridOutlineColor
                    val monthThinLineColor = if (isExpired) gridBackgroundColor else gridThinLineColor

                    // 月の枠線
                    drawLine(
                        color = monthLineColor,
                        start = androidx.compose.ui.geometry.Offset(monthStartX, 0f),
                        end = androidx.compose.ui.geometry.Offset(monthStartX, size.height),
                        strokeWidth = 1.dp.toPx()
                    )

                    // 上中下の分割線
                    for (i in 1..2) {
                        val x = monthStartX + i * cellWidthPx
                        drawLine(
                            color = monthThinLineColor,
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
                
                // 上下の境界線（背景色と同じ色に変更）
                drawLine(
                    color = surfaceContainerLowestColor,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = surfaceContainerLowestColor,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 前景レイヤー（バーとアイコンをCanvasで描画）
            // 事前に必要な値を取得
            val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
            val resources = context.resources
            val halfHeightPx = with(density) { halfHeight.toPx() }
            val barHeightPx = with(density) { 22.dp.toPx() }
            val barHalfHeightPx = with(density) { 11.dp.toPx() }
            val sowingIconSizePx = with(density) { 10.dp.toPx() }
            val sowingIconOffsetPx = with(density) { 12.dp.toPx() }
            val harvestIconSizePx = with(density) { 20.dp.toPx() }
            val harvestIconOffsetPx = with(density) { 20.dp.toPx() }
            val plantingIconSizePx = with(density) { 24.dp.toPx() }
            val plantingIconOffsetPx = with(density) { 30.dp.toPx() } // まきどきバーの中心から30dp上
            
            Canvas(
                modifier = Modifier
                    .width((months.size * 3 * cellWidth.value).dp)
                    .height(rowHeight)
            ) {
                val expirationDate = try {
                    if (seed.expirationMonth > 0) {
                        YearMonth.of(seed.expirationYear, seed.expirationMonth)
                    } else {
                        YearMonth.of(9999, 12)
                    }
                } catch (e: Exception) {
                    YearMonth.of(9999, 12)
                }
                
                // まきどき期間の描画
                if (showSowing) {
                    seed.calendar.forEach { entry ->
                        val sowingStart = com.example.seedstockkeeper6.utils.DateConversionUtils.toLocalDate(entry.sowing_start_date)
                        val sowingEnd = com.example.seedstockkeeper6.utils.DateConversionUtils.toLocalDate(entry.sowing_end_date)
                        
                        if (sowingStart != null && sowingEnd != null) {
                            val startYear = sowingStart.year
                            val startMonth = sowingStart.monthValue
                            val endYear = sowingEnd.year
                            val endMonth = sowingEnd.monthValue
                            
                            // 期間の開始月と終了月のインデックスを計算
                            val startMonthDate = LocalDate.of(startYear, startMonth, 1)
                            val endMonthDate = LocalDate.of(endYear, endMonth, 1)
                            
                            // カレンダーの範囲内にあるかチェック
                            val startMonthIndex = if (startMonthDate.isBefore(calendarStartDate)) {
                                // カレンダー開始より前の場合は0から開始
                                0
                            } else {
                                ChronoUnit.MONTHS.between(calendarStartDate, startMonthDate).toInt().coerceIn(0, months.size - 1)
                            }
                            
                            val endMonthIndex = if (endMonthDate.isAfter(calendarEndDate)) {
                                // カレンダー終了より後の場合は最後まで
                                months.size - 1
                            } else {
                                ChronoUnit.MONTHS.between(calendarStartDate, endMonthDate).toInt().coerceIn(0, months.size - 1)
                            }
                            
                            if (startMonthIndex >= 0 && endMonthIndex >= 0 && startMonthIndex <= endMonthIndex) {
                                val startX = startMonthIndex * 3 * cellWidthPx
                                val endX = (endMonthIndex + 1) * 3 * cellWidthPx
                                val centerY = if (showSowing && showHarvest) {
                                    halfHeightPx / 2f
                                } else {
                                    size.height / 2f
                                }
                                
                                // まきどきバーの背景を描画
                                drawRect(
                                    color = sowingBarColor,
                                    topLeft = androidx.compose.ui.geometry.Offset(startX, centerY - barHalfHeightPx),
                                    size = androidx.compose.ui.geometry.Size(endX - startX, barHeightPx)
                                )
                                
                                // まきどきアイコン（sesame）をパラパラ点滅アニメーションで描画
                                val iconSize = sowingIconSizePx
                                val iconResource = R.drawable.sesame
                                
                                val iconBitmap = runCatching {
                                    BitmapFactory.decodeResource(resources, iconResource)
                                        ?: throw Exception("decode failed")
                                }.getOrElse {
                                    val drawable = resources.getDrawable(iconResource, null)
                                    val bmp = Bitmap.createBitmap(iconSize.toInt(), iconSize.toInt(), Bitmap.Config.ARGB_8888)
                                    val c = AndroidCanvas(bmp)
                                    drawable.setBounds(0, 0, iconSize.toInt(), iconSize.toInt())
                                    drawable.draw(c)
                                    bmp
                                }
                                val iconImage = iconBitmap.asImageBitmap()
                                val iconDisplaySizeInt = iconSize.toInt()
                                
                                for (m in startMonthIndex..endMonthIndex) {
                                    if (m < 0 || m >= months.size) continue
                                    val monthX = m * 3 * cellWidthPx
                                    // 月を3分割してそれぞれの中心にアイコンを配置（種目録の種暦と同じ計算方法）
                                    val positions = listOf(
                                        monthX + cellWidthPx / 2f,      // 上旬の中心
                                        monthX + cellWidthPx * 1.5f,   // 中旬の中心
                                        monthX + cellWidthPx * 2.5f   // 下旬の中心
                                    )
                                    
                                    positions.forEach { iconX ->
                                        if (iconX >= startX && iconX <= endX) {
                                            val currentMonthDate = LocalDate.of(months[m].second, months[m].first, 1)
                                            val currentYearMonth = YearMonth.of(currentMonthDate.year, currentMonthDate.monthValue)
                                            val isExpired = currentYearMonth > expirationDate
                                            
                                            if (!isExpired) {
                                                val iconY = centerY - sowingIconOffsetPx
                                                val iconCenterX = iconX
                                                val iconCenterY = iconY + iconDisplaySizeInt / 2f
                                                
                                                // まきどきアイコンをぷるぷる揺れアニメーションで描画（収穫アイコンと同じ）
                                                val nativeCanvas = drawContext.canvas.nativeCanvas
                                                nativeCanvas.save()
                                                val pivotX = iconCenterX
                                                val pivotY = iconCenterY
                                                nativeCanvas.translate(pivotX, pivotY)
                                                nativeCanvas.rotate(shakeRotation)
                                                nativeCanvas.translate(-pivotX, -pivotY)
                                                
                                                val srcRect = android.graphics.Rect(0, 0, iconBitmap.width, iconBitmap.height)
                                                val dstRect = android.graphics.RectF(
                                                    iconCenterX - iconDisplaySizeInt / 2f,
                                                    iconY,
                                                    iconCenterX + iconDisplaySizeInt / 2f,
                                                    iconY + iconDisplaySizeInt
                                                )
                                                
                                                nativeCanvas.drawBitmap(iconBitmap, srcRect, dstRect, android.graphics.Paint())
                                                nativeCanvas.restore()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 日付から月内での位置を計算する関数
                fun getDateOffsetInMonth(date: LocalDate): Float {
                    val day = date.dayOfMonth
                    val lastDay = YearMonth.of(date.year, date.monthValue).lengthOfMonth()
                    return (day - 1).toFloat() / lastDay.toFloat()
                }
                
                // 収穫期間の描画
                if (showHarvest) {
                    seed.calendar.forEach { entry ->
                        val harvestStart = com.example.seedstockkeeper6.utils.DateConversionUtils.toLocalDate(entry.harvest_start_date)
                        val harvestEnd = com.example.seedstockkeeper6.utils.DateConversionUtils.toLocalDate(entry.harvest_end_date)
                        
                        if (harvestStart != null && harvestEnd != null) {
                            val startYear = harvestStart.year
                            val startMonth = harvestStart.monthValue
                            val endYear = harvestEnd.year
                            val endMonth = harvestEnd.monthValue
                            
                            // 期間の開始月と終了月のインデックスを計算
                            val startMonthDate = LocalDate.of(startYear, startMonth, 1)
                            val endMonthDate = LocalDate.of(endYear, endMonth, 1)
                            
                            // カレンダーの範囲内にあるかチェック
                            val startMonthIndex = if (startMonthDate.isBefore(calendarStartDate)) {
                                // カレンダー開始より前の場合は0から開始
                                0
                            } else {
                                ChronoUnit.MONTHS.between(calendarStartDate, startMonthDate).toInt().coerceIn(0, months.size - 1)
                            }
                            
                            val endMonthIndex = if (endMonthDate.isAfter(calendarEndDate)) {
                                // カレンダー終了より後の場合は最後まで
                                months.size - 1
                            } else {
                                ChronoUnit.MONTHS.between(calendarStartDate, endMonthDate).toInt().coerceIn(0, months.size - 1)
                            }
                            
                            if (startMonthIndex >= 0 && endMonthIndex >= 0 && startMonthIndex <= endMonthIndex) {
                                // 日付から月内での位置を計算して、正確な開始位置と終了位置を計算
                                val startOffset = getDateOffsetInMonth(harvestStart)
                                val endOffset = getDateOffsetInMonth(harvestEnd)
                                val startX = (startMonthIndex + startOffset) * 3 * cellWidthPx
                                val endX = (endMonthIndex + endOffset) * 3 * cellWidthPx
                                val centerY = if (showSowing && showHarvest) {
                                    halfHeightPx + halfHeightPx / 2f
                                } else {
                                    size.height / 2f
                                }
                                
                                // 収穫バーの背景を描画
                                drawRect(
                                    color = harvestBarColor,
                                    topLeft = androidx.compose.ui.geometry.Offset(startX, centerY - barHalfHeightPx),
                                    size = androidx.compose.ui.geometry.Size(endX - startX, barHeightPx)
                                )
                                
                                // 収穫アイコン（familyアイコン）をぷるぷる揺れアニメーションで描画
                                val iconSize = harvestIconSizePx
                                val normalizedFamily = normalizeFamilyName(seed.family)
                                val iconResource = when (normalizedFamily) {
                                    "イネ科" -> R.drawable.corn
                                    "ナス科" -> R.drawable.eggplant
                                    "ヒルガオ科" -> R.drawable.sweet_potato
                                    "アブラナ科" -> R.drawable.broccoli
                                    "ウリ科" -> R.drawable.cucumber
                                    "マメ科" -> R.drawable.bean
                                    "キク科" -> R.drawable.lettuce
                                    "セリ科" -> R.drawable.carrot
                                    "ヒガンバナ科" -> R.drawable.onion2
                                    "アマランサス科" -> R.drawable.spinach
                                    "バラ科" -> R.drawable.strawberry
                                    "ミカン科" -> R.drawable.orange
                                    "アカザ科" -> R.drawable.spinach
                                    "シソ科" -> R.drawable.perilla
                                    "ユリ科（ネギ類）" -> R.drawable.onion2
                                    "ショウガ科" -> R.drawable.ginger
                                    "アオイ科" -> R.drawable.okra
                                    else -> R.drawable.vegetables
                                }
                                
                                val iconBitmap = runCatching {
                                    BitmapFactory.decodeResource(resources, iconResource)
                                        ?: throw Exception("decode failed")
                                }.getOrElse {
                                    val drawable = resources.getDrawable(iconResource, null)
                                    val bmp = Bitmap.createBitmap(iconSize.toInt(), iconSize.toInt(), Bitmap.Config.ARGB_8888)
                                    val c = AndroidCanvas(bmp)
                                    drawable.setBounds(0, 0, iconSize.toInt(), iconSize.toInt())
                                    drawable.draw(c)
                                    bmp
                                }
                                val iconDisplaySizeInt = iconSize.toInt()
                                
                                for (m in startMonthIndex..endMonthIndex) {
                                    if (m < 0 || m >= months.size) continue
                                    val monthX = m * 3 * cellWidthPx
                                    // 各旬の中心位置を計算（種目録の種暦と同じ計算方法）
                                    val positions = listOf(
                                        monthX + cellWidthPx / 2f,      // 上旬の中心
                                        monthX + cellWidthPx * 1.5f,   // 中旬の中心
                                        monthX + cellWidthPx * 2.5f   // 下旬の中心
                                    )
                                    
                                    positions.forEach { iconX ->
                                        if (iconX >= startX && iconX <= endX) {
                                            val iconY = centerY - harvestIconOffsetPx
                                            val iconCenterX = iconX
                                            val iconCenterY = iconY + iconDisplaySizeInt / 2f
                                            
                                            val nativeCanvas = drawContext.canvas.nativeCanvas
                                            nativeCanvas.save()
                                            val pivotX = iconCenterX
                                            val pivotY = iconCenterY
                                            nativeCanvas.translate(pivotX, pivotY)
                                            nativeCanvas.rotate(shakeRotation)
                                            nativeCanvas.translate(-pivotX, -pivotY)
                                            
                                            val srcRect = android.graphics.Rect(0, 0, iconBitmap.width, iconBitmap.height)
                                            val dstRect = android.graphics.RectF(
                                                iconCenterX - iconDisplaySizeInt / 2f,
                                                iconY,
                                                iconCenterX + iconDisplaySizeInt / 2f,
                                                iconY + iconDisplaySizeInt
                                            )
                                            nativeCanvas.drawBitmap(iconBitmap, srcRect, dstRect, android.graphics.Paint())
                                            nativeCanvas.restore()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // まいた日のアイコンを描画
                if (showHarvest && seed.sowingDate.isNotEmpty()) {
                    val sowingDateResult = runCatching {
                        LocalDate.parse(seed.sowingDate)
                    }.getOrNull()
                    
                    sowingDateResult?.let { sowingDate ->
                        val sowingYear = sowingDate.year
                        val sowingMonth = sowingDate.monthValue
                        val sowingDay = sowingDate.dayOfMonth
                        
                        val sowingMonthDate = LocalDate.of(sowingYear, sowingMonth, 1)
                        val sowingMonthIndex = if (sowingMonthDate.isBefore(calendarStartDate)) {
                            -1 // カレンダー範囲外
                        } else if (sowingMonthDate.isAfter(calendarEndDate)) {
                            -1 // カレンダー範囲外
                        } else {
                            ChronoUnit.MONTHS.between(calendarStartDate, sowingMonthDate).toInt().coerceIn(0, months.size - 1)
                        }
                        
                        if (sowingMonthIndex >= 0) {
                            val lastDay = YearMonth.of(sowingYear, sowingMonth).lengthOfMonth()
                            val dayRatio = sowingDay.toFloat() / lastDay.toFloat()
                            
                            // どの旬に属するかを判断してアイコン位置を補正（種目録の種暦と同じ計算方法）
                            val monthX = sowingMonthIndex * 3 * cellWidthPx
                            val periodX = when {
                                dayRatio < 1f / 3f -> monthX + cellWidthPx / 2f      // 上旬の中心
                                dayRatio < 2f / 3f -> monthX + cellWidthPx * 1.5f     // 中旬の中心
                                else -> monthX + cellWidthPx * 2.5f                   // 下旬の中心
                            }
                            
                            // まきどきバーの中心位置を計算
                            val sowingCenterY = if (showSowing && showHarvest) {
                                halfHeightPx / 2f
                            } else {
                                size.height / 2f
                            }
                            
                            // まいた日のアイコン位置（まきどきバーの中心から30dp上）
                            val iconX = periodX
                            val iconY = sowingCenterY - plantingIconOffsetPx
                            val iconSize = plantingIconSizePx
                            
                            val plantingBitmap = runCatching {
                                BitmapFactory.decodeResource(resources, R.drawable.planting)
                                    ?: throw Exception("decode failed")
                            }.getOrElse {
                                val drawable = resources.getDrawable(R.drawable.planting, null)
                                val bmp = Bitmap.createBitmap(iconSize.toInt(), iconSize.toInt(), Bitmap.Config.ARGB_8888)
                                val c = AndroidCanvas(bmp)
                                drawable.setBounds(0, 0, iconSize.toInt(), iconSize.toInt())
                                drawable.draw(c)
                                bmp
                            }
                            
                            drawImage(
                                image = plantingBitmap.asImageBitmap(),
                                dstOffset = IntOffset(
                                    (iconX - iconSize / 2).toInt(),
                                    iconY.toInt()
                                ),
                                dstSize = IntSize(iconSize.toInt(), iconSize.toInt())
                            )
                        }
                    }
                }
            }
        }
    }
}



