package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.ui.components.SwipeToDeleteItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavBackStackEntry
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.ui.components.FamilyIcon
import com.example.seedstockkeeper6.util.familyRotationMinYearsLabel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.launch

// 種の状態を判定する関数
fun getSeedStatus(seed: SeedPacket): String {
    val currentDate = java.time.LocalDate.now()
    val currentMonth = currentDate.monthValue
    val currentYear = currentDate.year
    
    // 1. まき終わりの判定（最優先）
    if (seed.isFinished) return "finished"
    
    // 2. 期限切れの判定
    if (seed.isExpired) return "expired"
    
    // 3. 終了間近の判定
    val isUrgent = seed.calendar.any { entry ->
        val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
        val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
        sowingEndMonth == currentMonth && sowingEndYear == currentYear
    }
    if (isUrgent) return "urgent"
    
    // 4. 今月まける種の判定
    val isThisMonth = seed.calendar.any { entry ->
        if (entry.sowing_start_date.isNotEmpty() && entry.sowing_end_date.isNotEmpty()) {
            try {
                val startMonth = entry.sowing_start_date.split("-")[1].toInt()
                val endMonth = entry.sowing_end_date.split("-")[1].toInt()
                startMonth <= currentMonth && endMonth >= currentMonth
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }
    if (isThisMonth) return "thisMonth"
    
    return "normal"
}

@Composable
fun SeedListScreen(
    navController: NavController,
    selectedIds: MutableList<String>,
    viewModel: SeedListViewModel,
    onDeleteSelected: (List<String>) -> Unit,
    backStackEntry: NavBackStackEntry? = null
) {
    val db = Firebase.firestore
    var seeds by remember { mutableStateOf(listOf<Pair<String, SeedPacket>>()) }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 検索状態
    var searchQuery by remember { mutableStateOf("") }
    var showThisMonthOnly by remember { mutableStateOf(false) }
    var showExpiredOnly by remember { mutableStateOf(false) }
    var showUrgentOnly by remember { mutableStateOf(false) }
    var showFinishedSeeds by remember { mutableStateOf(true) }
    var showFilters by remember { mutableStateOf(false) }
    
    // URLパラメータの処理
    LaunchedEffect(backStackEntry) {
        val filter = backStackEntry?.arguments?.getString("filter")
        when (filter) {
            "thisMonth" -> {
                showThisMonthOnly = true
                showFilters = true
            }
            "urgent" -> {
                showUrgentOnly = true
                showFilters = true
            }
            "expired" -> {
                showExpiredOnly = true
                showFilters = true
            }
        }
    }
    
    // 現在のユーザーのUIDを取得
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    val currentUid = currentUser?.uid
    
    // フィルタリングされた種リスト
    val filteredSeeds = remember(seeds, searchQuery, showThisMonthOnly, showExpiredOnly, showUrgentOnly, showFinishedSeeds) {
        seeds.filter { (_, seed) ->
            val matchesSearch = searchQuery.isEmpty() || 
                seed.productName.contains(searchQuery, ignoreCase = true) ||
                seed.variety.contains(searchQuery, ignoreCase = true) ||
                seed.family.contains(searchQuery, ignoreCase = true)
            
            val matchesThisMonth = if (showThisMonthOnly) {
                val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
                // 播種期間のCalendarEntryを探す
                seed.calendar.any { entry ->
                    if (entry.sowing_start_date.isNotEmpty() && entry.sowing_end_date.isNotEmpty()) {
                        try {
                            val startMonth = entry.sowing_start_date.split("-")[1].toInt()
                            val endMonth = entry.sowing_end_date.split("-")[1].toInt()
                            startMonth <= currentMonth && endMonth >= currentMonth
                        } catch (e: Exception) {
                            false
                        }
                    } else {
                        false
                    }
                }
            } else {
                true
            }
            
            val matchesExpired = if (showExpiredOnly) {
                // 期限切れの判定（種集計と同じロジック）
                val currentDate = java.time.LocalDate.now()
                val expirationDate = java.time.LocalDate.of(seed.expirationYear, seed.expirationMonth, 1)
                currentDate.isAfter(expirationDate.plusMonths(1).minusDays(1))
            } else {
                true
            }
            
            val matchesUrgent = if (showUrgentOnly) {
                // 終了間近の判定（今月内で播種期間が終了する種）
                val currentDate = java.time.LocalDate.now()
                val currentMonth = currentDate.monthValue
                val currentYear = currentDate.year
                
                seed.calendar.any { entry ->
                    val sowingEndMonth = com.example.seedstockkeeper6.utils.DateConversionUtils.getMonthFromDate(entry.sowing_end_date)
                    val sowingEndYear = com.example.seedstockkeeper6.utils.DateConversionUtils.getYearFromDate(entry.sowing_end_date)
                    sowingEndMonth == currentMonth && sowingEndYear == currentYear
                }
            } else {
                true
            }
            
            val matchesFinished = if (showFinishedSeeds) {
                true // まき終わりの種も表示
            } else {
                !seed.isFinished // まき終わりの種を非表示
            }
            
            matchesSearch && matchesThisMonth && matchesExpired && matchesUrgent && matchesFinished
        }
    }

    DisposableEffect(Unit) {
        if (currentUid == null) {
            return@DisposableEffect onDispose { }
        }
        
        var registration: ListenerRegistration? = null
        
        try {
            registration = db.collection("seeds")
                .whereEqualTo("ownerUid", currentUid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // エラーハンドリングを改善
                        when (error.code) {
                            com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE -> {
                            }
                            com.google.firebase.firestore.FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                            }
                            else -> {
                            }
                        }
                        return@addSnapshotListener
                    }
                    
                    snapshot?.let {
                        val newSeeds = it.documents.mapNotNull { doc ->
                            try {
                                val seed = doc.toObject(SeedPacket::class.java)
                                if (seed != null) {
                                    // FirestoreのドキュメントIDをSeedPacketのidフィールドに設定
                                    // 新しいフィールド（isFinished, isExpired）をFirestoreから直接取得
                                    val isFinished = doc.getBoolean("isFinished") ?: false
                                    val isExpired = doc.getBoolean("isExpired") ?: false
                                    
                                    val seedWithId = seed.copy(
                                        id = doc.id, 
                                        documentId = doc.id,
                                        isFinished = isFinished,
                                        isExpired = isExpired
                                    )
                                    doc.id to seedWithId
                                } else {
                                    null
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }
                        // データが実際に変更された場合のみ更新
                        if (newSeeds != seeds) {
                            seeds = newSeeds
                        }
                    }
                }
        } catch (e: Exception) {
        }
        
        onDispose {
            try {
                registration?.remove()
            } catch (e: Exception) {
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues)
        ) {
        // 検索バー
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // フリーワード検索とフィルターボタン
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("種を検索") },
                        placeholder = { Text("商品名、品種、科名で検索") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // フィルター表示切り替えボタン
                    IconButton(
                        onClick = { showFilters = !showFilters }
                    ) {
                        Icon(
                            imageVector = if (showFilters) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (showFilters) "フィルターを隠す" else "フィルターを表示",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // フィルター用チェックボックス（条件付き表示）
                if (showFilters) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // レスポンシブレイアウト：画面幅に応じて1行または2行表示
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1行目：「今月まける」「期限切れ」
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 今月まける種チェックボックス
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = showThisMonthOnly,
                                    onCheckedChange = { showThisMonthOnly = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "今月まける",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            // 期限切れチェックボックス
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = showExpiredOnly,
                                    onCheckedChange = { showExpiredOnly = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.error,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "期限切れ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        
                        // 2行目：「終了間近」「まき終わり」
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 終了間近チェックボックス
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = showUrgentOnly,
                                    onCheckedChange = { showUrgentOnly = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.tertiary,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "終了間近",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            
                            // まき終わりチェックボックス
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = showFinishedSeeds,
                                    onCheckedChange = { showFinishedSeeds = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.secondary,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "まき終わり",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                }
                }
            }
        }
        
        // 種リスト
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp), // アイテム間の間隔を6dpに設定
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp), // 上下に2dpのパディングを追加
            // スクロール設定
            userScrollEnabled = true
        ) {
            items(
                items = filteredSeeds,
                key = { (id, _) -> id } // キーを指定してリコンポジションを最適化
            ) { (id, seed) ->
            val checked = selectedIds.contains(id)
            val encodedSeed = URLEncoder.encode(Gson().toJson(seed), "UTF-8")
            
            // スワイプ可能なリストアイテム
            SwipeToDeleteItem(
                modifier = Modifier
                    .heightIn(min = 80.dp), // 最小高さを設定してスクロール性能を向上
                onDelete = {
                    // 実際の削除処理を呼び出す
                    onDeleteSelected(listOf(id))
                }
            ) {
                // 種の状態を判定
                val seedStatus = getSeedStatus(seed)
                val backgroundColor = when (seedStatus) {
                    "finished" -> MaterialTheme.colorScheme.secondaryContainer  // まき終わり
                    "expired" -> MaterialTheme.colorScheme.surfaceContainerHigh  // 有効期限切れ
                    "urgent" -> MaterialTheme.colorScheme.errorContainer         // 終了間近
                    "thisMonth" -> MaterialTheme.colorScheme.primaryContainer    // 今月まきどき
                    else -> MaterialTheme.colorScheme.surfaceContainerLowest                   // 通常
                }
                
                // 期限切れの種の色をLogで確認
                if (seedStatus == "expired") {
                }
                
                // リストアイテム
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(12.dp)
                        .clickable {
                            navController.navigate("input/$encodedSeed")
                        },
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    // Familyアイコン
                    val rotation = familyRotationMinYearsLabel(seed.family) ?: ""
                    FamilyIcon(
                        family = seed.family,
                        size = 48.dp,
                        cornerRadius = 8.dp,
                        rotationLabel = rotation,
                        badgeProtrusion = 4.dp,
                        showCircleBorder = true
                    )
                    
                    // 中央: 縦並びの情報
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // 商品名
                        Text(
                            text = seed.productName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // 品種名
                        Text(
                            text = seed.variety,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // 有効期限
                        Text(
                            text = "有効期限: ${seed.expirationYear}/${seed.expirationMonth}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // コンパニオンプランツ
                        if (seed.companionPlants.isNotEmpty()) {
                            val companionPlantNames = seed.companionPlants
                                .filter { it.plant.isNotBlank() }
                                .map { it.plant }
                                .take(3)
                            
                            if (companionPlantNames.isNotEmpty()) {
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Cを丸で囲ったアイコン
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = androidx.compose.ui.Alignment.Center
                                    ) {
                                        Text(
                                            text = "C",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    // コンパニオンプランツ名
                                    Text(
                                        text = "${companionPlantNames.joinToString(", ")}${if (seed.companionPlants.size > 3) "..." else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    
                    // 右側: まき終わりアイコン
                    IconButton(
                        onClick = {
                            val isChecked = !seed.isFinished
                            // まき終わりフラグの更新処理
                            val documentId = seed.documentId ?: seed.id
                            if (documentId != null) {
                                viewModel.updateFinishedFlag(documentId, isChecked) { result ->
                                    scope.launch {
                                        if (result.isSuccess) {
                                            val message = if (isChecked) "まき終わりに設定しました" else "まき終わりを解除しました"
                                            snackbarHostState.showSnackbar(
                                                message = message,
                                                duration = SnackbarDuration.Short
                                            )
                                        } else {
                                            snackbarHostState.showSnackbar(
                                                message = "更新に失敗しました",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (seed.isFinished) R.drawable.checkmark else R.drawable.packet
                            ),
                            contentDescription = if (seed.isFinished) "まき終わり済み" else "まき終わり未完了",
                            modifier = Modifier.size(36.dp),
                            tint = Color.Unspecified // tintにColor.Unspecifiedを指定して元の色を使用
                        )
                    }
                }
            }
            
            // 区切り線（最後のアイテム以外）
            if (filteredSeeds.indexOf(id to seed) < filteredSeeds.size - 1) {
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
        }
    }
    }
}
