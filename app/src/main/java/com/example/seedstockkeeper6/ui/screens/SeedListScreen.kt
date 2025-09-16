package com.example.seedstockkeeper6.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.example.seedstockkeeper6.ui.components.SwipeToDeleteItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.ui.components.FamilyIcon
import com.example.seedstockkeeper6.util.familyRotationMinYearsLabel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun SeedListScreen(
    navController: NavController,
    selectedIds: MutableList<String>,
    viewModel: SeedListViewModel,
    onDeleteSelected: (List<String>) -> Unit
) {
    Log.d("BootTrace", ">>> SeedListScreen Composable表示")
    val db = Firebase.firestore
    var seeds by remember { mutableStateOf(listOf<Pair<String, SeedPacket>>()) }
    val listState = rememberLazyListState()
    
    // 検索状態
    var searchQuery by remember { mutableStateOf("") }
    var showThisMonthOnly by remember { mutableStateOf(false) }
    
    // 現在のユーザーのUIDを取得
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    val currentUid = currentUser?.uid
    
    // フィルタリングされた種リスト
    val filteredSeeds = remember(seeds, searchQuery, showThisMonthOnly) {
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
            
            matchesSearch && matchesThisMonth
        }
    }

    DisposableEffect(Unit) {
        if (currentUid == null) {
            Log.w("SeedListScreen", "No authenticated user")
            return@DisposableEffect onDispose { }
        }
        
        val registration = db.collection("seeds")
            .whereEqualTo("ownerUid", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SeedListScreen", "Firebase error: ${error.message}")
                    return@addSnapshotListener
                }
                
                snapshot?.let {
                    val newSeeds = it.documents.mapNotNull { doc ->
                        val seed = doc.toObject(SeedPacket::class.java)
                        if (seed != null) {
                            // FirestoreのドキュメントIDをSeedPacketのidフィールドに設定
                            val seedWithId = seed.copy(id = doc.id, documentId = doc.id)
                            doc.id to seedWithId
                        } else {
                            Log.w("SeedListScreen", "Failed to convert document ${doc.id} to SeedPacket")
                            null
                        }
                    }
                    seeds = newSeeds
                }
            }
        onDispose {
            registration.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
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
                // フリーワード検索
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
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
                        text = "今月まける種",
                        style = MaterialTheme.typography.bodyMedium
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
            verticalArrangement = Arrangement.spacedBy(0.dp), // アイテム間の間隔を0dpに設定
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp) // 上下に8dpのパディングを追加
        ) {
            items(filteredSeeds) { (id, seed) ->
            val checked = selectedIds.contains(id)
            val encodedSeed = URLEncoder.encode(Gson().toJson(seed), StandardCharsets.UTF_8.toString())
            
            // スワイプ可能なリストアイテム
            SwipeToDeleteItem(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                onDelete = {
                    // 実際の削除処理を呼び出す
                    onDeleteSelected(listOf(id))
                }
            ) {
                // リストアイテム
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("input/$encodedSeed")
                        },
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                val rotation = familyRotationMinYearsLabel(seed.family) ?: ""
                FamilyIcon(
                    family = seed.family,
                    size = 48.dp,
                    cornerRadius = 8.dp,
                    rotationLabel = rotation,
                    badgeProtrusion = 4.dp,
                    showCircleBorder = true
                )
                
                // 商品情報Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // 品種名の上の余白（Material3ルール: 8dp）
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 商品名と品種名を横並び（横スクロール対応）
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            seed.productName, 
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            "（${seed.variety}）", 
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Light),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(2.dp)) // 4dp → 2dpに縮小
                    
                    // 有効期限のみ表示（Checkbox一時削除でテスト）
                    Text(
                        "有効期限: ${seed.expirationYear}年 ${seed.expirationMonth}月", 
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Light,
                            lineHeight = MaterialTheme.typography.bodyLarge.fontSize // フォントサイズと同じlineHeight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp) // 上下の余白を4dpに制限（Checkbox削除テスト）
                    )
                    
                    // コンパニオンプランツの表示
                    if (seed.companionPlants.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp)) // 4dp → 2dpに縮小
                        val companionPlantNames = seed.companionPlants
                            .filter { it.plant.isNotBlank() }
                            .map { it.plant }
                            .take(3) // 最大3つまで表示
                        
                        if (companionPlantNames.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Cを丸で囲ったアイコン（Material3準拠）
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.tertiaryContainer,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    Text(
                                        text = "C",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // コンパニオンプランツ名（1行表示、横スクロール対応）
                                Text(
                                    "${companionPlantNames.joinToString(", ")}${if (seed.companionPlants.size > 3) "..." else ""}",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Light),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    
                    // コンパニオンプランツの下の余白（Material3ルール: 8dp）
                    Spacer(modifier = Modifier.height(8.dp))
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
