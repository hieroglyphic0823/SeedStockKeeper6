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
    
    // フィルタ状態（複数選択可、初期状態はすべてオン）
    var showThisMonthSeeds by remember { mutableStateOf(true) }      // 今月まきどき
    var showUrgentSeeds by remember { mutableStateOf(true) }         // 終了間近
    var showExpiredSeeds by remember { mutableStateOf(true) }        // 有効期限切れ
    var showFinishedSeeds by remember { mutableStateOf(true) }        // まき終わり
    var showNormalSeeds by remember { mutableStateOf(true) }          // 通常
    
    var showFilters by remember { mutableStateOf(true) }  // チェックボックスを常に表示
    var showSearchBox by remember { mutableStateOf(false) }  // 検索ボックスを隠す
    
    // URLパラメータの処理
    LaunchedEffect(backStackEntry) {
        val filter = backStackEntry?.arguments?.getString("filter")
        when (filter) {
            "thisMonth" -> {
                showThisMonthSeeds = true
                showUrgentSeeds = false
                showExpiredSeeds = false
                showFinishedSeeds = false
                showNormalSeeds = false
                showFilters = true
                showSearchBox = true
            }
            "urgent" -> {
                showThisMonthSeeds = false
                showUrgentSeeds = true
                showExpiredSeeds = false
                showFinishedSeeds = false
                showNormalSeeds = false
                showFilters = true
                showSearchBox = true
            }
            "expired" -> {
                showThisMonthSeeds = false
                showUrgentSeeds = false
                showExpiredSeeds = true
                showFinishedSeeds = false
                showNormalSeeds = false
                showFilters = true
                showSearchBox = true
            }
        }
    }
    
    // 現在のユーザーのUIDを取得
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    val currentUid = currentUser?.uid
    
    // フィルタリングされた種リスト
    val filteredSeeds = remember(seeds, searchQuery, showThisMonthSeeds, showUrgentSeeds, showExpiredSeeds, showFinishedSeeds, showNormalSeeds) {
        seeds.filter { (_, seed) ->
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
                                    val seedWithId = seed.copy(
                                        id = doc.id, 
                                        documentId = doc.id
                                    )
                                    
                                    // デバッグログを追加
                                    android.util.Log.d("SeedListScreen", "種データ取得: ${seedWithId.productName}(${seedWithId.variety}) - isFinished: ${seedWithId.isFinished}, isExpired: ${seedWithId.isExpired}")
                                    doc.id to seedWithId
                                } else {
                                    null
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("SeedListScreen", "種データ取得エラー: ${e.message}")
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
                // フィルター用チェックボックス（常に表示）
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1行目：「まきどき」「終了間近」「通常」
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // まきどきチェックボックス
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = showThisMonthSeeds,
                                onCheckedChange = { showThisMonthSeeds = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    uncheckedColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "まきどき",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        // 終了間近チェックボックス
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = showUrgentSeeds,
                                onCheckedChange = { showUrgentSeeds = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.onErrorContainer,
                                    uncheckedColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "終了間近",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        
                        // 通常チェックボックス
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = showNormalSeeds,
                                onCheckedChange = { showNormalSeeds = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.onSurface,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "通常",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    // 2行目：「期限切れ」「まき終わり」と検索ボックス表示切り替えボタン
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 期限切れチェックボックス
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = showExpiredSeeds,
                                onCheckedChange = { showExpiredSeeds = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.onSurface,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "期限切れ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // まき終わりチェックボックス
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = showFinishedSeeds,
                                onCheckedChange = { showFinishedSeeds = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    uncheckedColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "まき終わり",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
                        // 検索ボックス表示切り替えボタン（2行目の右端）
                        IconButton(
                            onClick = { showSearchBox = !showSearchBox }
                        ) {
                            Icon(
                                imageVector = if (showSearchBox) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = if (showSearchBox) "検索ボックスを隠す" else "検索ボックスを表示",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // 検索ボックス（条件付き表示）
                if (showSearchBox) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
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
                    "expired" -> MaterialTheme.colorScheme.surfaceContainerHighest  // 有効期限切れ
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
