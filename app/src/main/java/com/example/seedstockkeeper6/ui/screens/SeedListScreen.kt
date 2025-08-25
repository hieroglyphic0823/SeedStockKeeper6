package com.example.seedstockkeeper6.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.ui.components.FamilyIcon
import com.example.seedstockkeeper6.util.familyRotationMinYearsLabel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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

    DisposableEffect(Unit) {
        val registration = db.collection("seeds")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    seeds = it.documents.mapNotNull { doc ->
                        val seed = doc.toObject(SeedPacket::class.java)
                        seed?.let { s -> doc.id to s }
                    }
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
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(16.dp), // 角丸
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp,
                    focusedElevation = 6.dp,
                    hoveredElevation = 6.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp) // 外側余白
                    .clickable {
                        navController.navigate("input/$encodedSeed")
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val rotation  = familyRotationMinYearsLabel(seed.family) ?: ""
                    FamilyIcon(
                        family = seed.family,
                        size = 60.dp,
                        cornerRadius = 10.dp,
                        rotationLabel = rotation,
                        badgeProtrusion = 6.dp  // はみ出し量を調整（6–10dpくらいが目安）
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${seed.productName} (${seed.variety})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("有効期限: ${seed.expirationYear}年 ${seed.expirationMonth}月", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light))
                    }
                    Checkbox(
                        checked = checked,
                        onCheckedChange = {
                            if (it) selectedIds.add(id) else selectedIds.remove(id)
                        },
                        colors = androidx.compose.material3.CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            checkmarkColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}
