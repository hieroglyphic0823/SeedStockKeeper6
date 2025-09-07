package com.example.seedstockkeeper6.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
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
    viewModel: SeedListViewModel
) {
    Log.d("BootTrace", ">>> SeedListScreen Composable表示")
    val db = Firebase.firestore
    var seeds by remember { mutableStateOf(listOf<Pair<String, SeedPacket>>()) }
    val listState = rememberLazyListState()
    
    // 現在のユーザーのUIDを取得
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    val currentUid = currentUser?.uid

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

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(seeds) { (id, seed) ->
            val checked = selectedIds.contains(id)
            val encodedSeed = URLEncoder.encode(Gson().toJson(seed), StandardCharsets.UTF_8.toString())
            
            // スワイプ状態管理
            var swipeOffset by remember { mutableStateOf(0f) }
            var isSwipeActive by remember { mutableStateOf(false) }
            
            // カスタムスワイプ削除アイテム
            SwipeableListItem(
                id = id,
                seed = seed,
                checked = checked,
                encodedSeed = encodedSeed,
                selectedIds = selectedIds,
                navController = navController,
                viewModel = viewModel,
                swipeOffset = swipeOffset,
                isSwipeActive = isSwipeActive,
                onSwipeOffsetChange = { offset ->
                    swipeOffset = offset
                },
                onSwipeActiveChange = { active ->
                    isSwipeActive = active
                }
            )
            
            // 区切り線（最後のアイテム以外）
            if (seeds.indexOf(id to seed) < seeds.size - 1) {
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun SwipeableListItem(
    id: String,
    seed: SeedPacket,
    checked: Boolean,
    encodedSeed: String,
    selectedIds: MutableList<String>,
    navController: NavController,
    viewModel: SeedListViewModel,
    swipeOffset: Float,
    isSwipeActive: Boolean,
    onSwipeOffsetChange: (Float) -> Unit,
    onSwipeActiveChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 削除ボタン（右端に配置、スワイプ幅に応じて表示）
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(if (swipeOffset > 0) swipeOffset.dp else 0.dp)
                .height(60.dp)
                .background(
                    MaterialTheme.colorScheme.error,
                    RoundedCornerShape(8.dp)
                )
                .clickable(enabled = swipeOffset > 0) {
                    viewModel.deleteSeed(id)
                },
            contentAlignment = Alignment.Center
        ) {
            if (swipeOffset > 0) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "削除",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // メインコンテンツ（スワイプに応じて移動）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = (-swipeOffset).dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { _ ->
                            onSwipeActiveChange(true)
                        },
                        onDragEnd = {
                            onSwipeActiveChange(false)
                            // 指を離してもサイズを保つ（リセットしない）
                        },
                        onDrag = { _, dragAmount ->
                            val newOffset = (swipeOffset + dragAmount.x).coerceIn(0f, 120f)
                            onSwipeOffsetChange(newOffset)
                        }
                    )
                }
                .clickable {
                    navController.navigate("input/$encodedSeed")
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            val rotation = familyRotationMinYearsLabel(seed.family) ?: ""
            FamilyIcon(
                family = seed.family,
                size = 50.dp,
                cornerRadius = 8.dp,
                rotationLabel = rotation,
                badgeProtrusion = 4.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${seed.productName} (${seed.variety})", 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "有効期限: ${seed.expirationYear}年 ${seed.expirationMonth}月", 
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light)
                )
                
                // コンパニオンプランツの表示
                if (seed.companionPlants.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val companionPlantNames = seed.companionPlants
                        .filter { it.plant.isNotBlank() }
                        .map { it.plant }
                        .take(3) // 最大3つまで表示
                    
                    if (companionPlantNames.isNotEmpty()) {
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Cを丸で囲ったアイコン
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    ),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Text(
                                    text = "C",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // コンパニオンプランツ名
                            Text(
                                "${companionPlantNames.joinToString(", ")}${if (seed.companionPlants.size > 3) "..." else ""}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light)
                            )
                        }
                    }
                }
            }
            Checkbox(
                checked = checked,
                onCheckedChange = {
                    if (it) selectedIds.add(id) else selectedIds.remove(id)
                },
                colors = androidx.compose.material3.CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
