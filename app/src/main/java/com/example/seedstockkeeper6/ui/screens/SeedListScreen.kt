package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavBackStackEntry
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import java.net.URLEncoder

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
    
    // 表示モードの状態（リスト/ギャラリー）
    var displayMode by remember { mutableStateOf("gallery") } // "list" or "gallery"（初期値はギャラリー）
    
    // 並べ替えの状態
    var sortType by remember { mutableStateOf(SortType.IMPORTANCE) }
    
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
    
    // 重要度の順位（数値が小さいほど優先度が高い）
    fun getImportanceOrder(status: String): Int = when (status) {
        "urgent" -> 1      // 期限間近
        "thisMonth" -> 2   // まきどき
        "normal" -> 3      // 通常
        "finished" -> 4    // まき終わり
        "expired" -> 5     // 期限切れ
        else -> 6
    }
    
    // フィルタリングとソートされた種リスト
    val filteredSeeds = remember(seeds, searchQuery, showThisMonthSeeds, showUrgentSeeds, showExpiredSeeds, showFinishedSeeds, showNormalSeeds, sortType) {
        val filtered = seeds.filter { (_, seed) ->
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
            SortType.IMPORTANCE -> {
                // 重要度順：期限間近 > まきどき > 通常 > まき終わり > 期限切れ
                filtered.sortedWith(compareBy<Pair<String, SeedPacket>> { (_, seed) ->
                    getImportanceOrder(getSeedStatus(seed))
                }.thenBy { (_, seed) -> seed.productName })
            }
            SortType.REGISTRATION -> {
                // 登録順（元の順序を保持）
                filtered
            }
            SortType.NAME -> {
                // あいうえお順（商品名でソート）
                filtered.sortedBy { (_, seed) -> seed.productName }
            }
            SortType.STATUS -> {
                // 状態順：状態でグループ化し、各グループ内で商品名でソート
                filtered.sortedWith(compareBy<Pair<String, SeedPacket>> { (_, seed) ->
                    getSeedStatus(seed)
                }.thenBy { (_, seed) -> seed.productName })
            }
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
        // 抽出条件カード
        SeedListFilterCard(
            showThisMonthSeeds = showThisMonthSeeds,
            onThisMonthSeedsChange = { showThisMonthSeeds = it },
            showUrgentSeeds = showUrgentSeeds,
            onUrgentSeedsChange = { showUrgentSeeds = it },
            showExpiredSeeds = showExpiredSeeds,
            onExpiredSeedsChange = { showExpiredSeeds = it },
            showFinishedSeeds = showFinishedSeeds,
            onFinishedSeedsChange = { showFinishedSeeds = it },
            showNormalSeeds = showNormalSeeds,
            onNormalSeedsChange = { showNormalSeeds = it },
            showSearchBox = showSearchBox,
            onSearchBoxToggle = { showSearchBox = !showSearchBox },
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            displayMode = displayMode,
            onDisplayModeChange = { displayMode = it },
            sortType = sortType,
            onSortTypeChange = { sortType = it }
        )
        
        // 種リスト/ギャラリー
        Column(modifier = Modifier.weight(1f)) {
            if (displayMode == "list") {
                // リスト表示
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                    userScrollEnabled = true
                ) {
                    itemsIndexed(
                        items = filteredSeeds,
                        key = { _, (id, _) -> id }
                    ) { index, (id, seed) ->
                        val encodedSeed = URLEncoder.encode(Gson().toJson(seed), "UTF-8")
                        SeedListItem(
                            seed = seed,
                            encodedSeed = encodedSeed,
                            navController = navController,
                            viewModel = viewModel,
                            snackbarHostState = snackbarHostState,
                            scope = scope,
                            onDelete = {
                                onDeleteSelected(listOf(id))
                            },
                            isLastItem = index == filteredSeeds.size - 1
                        )
                    }
                }
            } else {
                // ギャラリー表示
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredSeeds,
                        key = { (id, _) -> id }
                    ) { (id, seed) ->
                        val encodedSeed = URLEncoder.encode(Gson().toJson(seed), "UTF-8")
                        SeedGalleryItem(
                            seed = seed,
                            encodedSeed = encodedSeed,
                            navController = navController
                        )
                    }
                }
            }
        }
        }
    }
}
