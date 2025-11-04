package com.example.seedstockkeeper6.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.ui.components.FamilyIcon
import com.example.seedstockkeeper6.ui.components.SwipeToDeleteItem
import com.example.seedstockkeeper6.util.familyRotationMinYearsLabel
import com.example.seedstockkeeper6.viewmodel.SeedListViewModel
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import android.util.Log

/**
 * リストアイテムコンポーネント
 */
@Composable
fun SeedListItem(
    seed: SeedPacket,
    encodedSeed: String,
    navController: NavController,
    viewModel: SeedListViewModel,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    onDelete: () -> Unit,
    isLastItem: Boolean
) {
    // スワイプ可能なリストアイテム
    SwipeToDeleteItem(
        modifier = Modifier
            .heightIn(min = 80.dp),
        onDelete = onDelete
    ) {
        // 種の状態を判定
        val seedStatus = getSeedStatus(seed)
        val backgroundColor = when (seedStatus) {
            "finished" -> MaterialTheme.colorScheme.secondaryContainer  // まき終わり
            "expired" -> MaterialTheme.colorScheme.surfaceContainerHighest      // 期限切れ：淡グレ
            "urgent" -> MaterialTheme.colorScheme.errorContainer  // 強い赤系：終了間近を強調
            "thisMonth" -> MaterialTheme.colorScheme.primaryContainer       // 黄色系：まきどき
            else -> MaterialTheme.colorScheme.tertiaryContainer             // 緑系：通常
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
            verticalAlignment = Alignment.CenterVertically
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
                            verticalAlignment = Alignment.CenterVertically,
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
                                contentAlignment = Alignment.Center
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
            
            // 右側: 状態アイコン
            // 種の状態に応じたアイコンを決定
            val statusIconResId = when (seedStatus) {
                "finished" -> R.drawable.seed  // まき終わり：seed
                "urgent" -> R.drawable.warning  // 期限間近：warning
                "thisMonth" -> R.drawable.seed_bag_enp  // まきどき：seed_bag_enp
                "expired" -> R.drawable.close  // 期限切れ：close
                else -> null  // 通常：何も表示しない（またはデフォルトアイコン）
            }
            val statusIconDescription = when (seedStatus) {
                "finished" -> "まき終わり"
                "urgent" -> "期限間近"
                "thisMonth" -> "まきどき"
                "expired" -> "期限切れ"
                else -> "通常"
            }
            
            // すべての状態でクリック可能
            val isClickable = seedStatus in listOf("finished", "urgent", "thisMonth", "expired")
            
            if (isClickable) {
                // クリックでまき終わりに設定（または解除）
            IconButton(
                onClick = {
                        val isChecked = if (seedStatus == "finished") {
                            // まき終わりの場合は解除
                            !seed.isFinished
                        } else {
                            // その他の状態の場合はまき終わりに設定
                            true
                        }
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
                    if (statusIconResId != null) {
                        // 状態アイコンをdrawableリソースから表示
                        Icon(
                            painter = painterResource(id = statusIconResId),
                            contentDescription = statusIconDescription,
                            modifier = Modifier.size(36.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
            } else {
                // 通常状態：アイコンのみ表示（クリック不可）
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 通常状態ではアイコンを表示しない
                }
            }
        }
    }
    
    // 区切り線（最後のアイテム以外）
    if (!isLastItem) {
        HorizontalDivider(
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

/**
 * ギャラリーアイテムコンポーネント
 */
@Composable
fun SeedGalleryItem(
    seed: SeedPacket,
    encodedSeed: String,
    navController: NavController
) {
    val seedStatus = getSeedStatus(seed)
    
    // 画像URLの変換とログ出力
    var downloadUrl by remember { mutableStateOf<String?>(null) }
    val firstImageUrl = if (seed.imageUrls.isNotEmpty()) seed.imageUrls.first() else null
    
    LaunchedEffect(firstImageUrl) {
        if (firstImageUrl != null) {
            Log.d("SeedGalleryItem", "商品名: ${seed.productName}, 品種: ${seed.variety}")
            Log.d("SeedGalleryItem", "元のimageUrl: $firstImageUrl")
            
            // Firebase Storageのパスの場合はdownloadUrlを取得
            if (firstImageUrl.startsWith("seed_images/")) {
                Log.d("SeedGalleryItem", "Firebase Storageパスを検出: $firstImageUrl")
                try {
                    val storageRef = Firebase.storage.reference.child(firstImageUrl)
                    val url = storageRef.downloadUrl.await().toString()
                    downloadUrl = url
                    Log.d("SeedGalleryItem", "downloadUrl取得成功: $url")
                } catch (e: Exception) {
                    Log.e("SeedGalleryItem", "downloadUrl取得失敗: ${e.message}", e)
                    downloadUrl = null
                }
            } else if (firstImageUrl.startsWith("http://") || firstImageUrl.startsWith("https://")) {
                // すでにHTTP/HTTPS URLの場合はそのまま使用
                downloadUrl = firstImageUrl
                Log.d("SeedGalleryItem", "HTTP/HTTPS URL: $firstImageUrl")
            } else {
                // その他の場合はそのまま使用（file://など）
                downloadUrl = firstImageUrl
                Log.d("SeedGalleryItem", "その他のURL形式: $firstImageUrl")
            }
        } else {
            Log.d("SeedGalleryItem", "商品名: ${seed.productName}, 品種: ${seed.variety} - 画像URLが空です")
            downloadUrl = null
        }
    }
    
    // 状態に応じたフレーム画像を決定
    val frameImageResId = when (seedStatus) {
        "finished" -> R.drawable.goshiki_lb_sq  // まき終わり
        "expired" -> R.drawable.goshiki_lg_sq   // 期限切れ
        "urgent" -> R.drawable.goshiki_r_sq    // 期限間近
        "thisMonth" -> R.drawable.goshiki_y_sq // まきどき
        else -> R.drawable.goshiki_g_sq        // 通常
    }
    
    // 状態アイコンを決定
    val statusIconResId = when (seedStatus) {
        "finished" -> R.drawable.seed  // まき終わり：seed
        "urgent" -> R.drawable.warning  // 期限間近：warning
        "thisMonth" -> R.drawable.seed_bag_enp  // まきどき：seed_bag_enp
        "expired" -> R.drawable.close  // 期限切れ：close
        else -> null  // 通常：表示しない
    }
    
    // ギャラリーアイテム
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                navController.navigate("input/$encodedSeed")
            }
    ) {
        // 種の写真（中央に表示）- 写真がある場合は最前面に表示
        if (downloadUrl != null) {
            // 背景画像（k_goshiki_jin.png）- 写真がある場合は半透明で表示
            Image(
                painter = painterResource(id = frameImageResId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(1f),
                contentScale = ContentScale.FillBounds
            )
            
            // 種の写真（中央に表示、前面に配置）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = downloadUrl,
                    contentDescription = seed.productName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
            }
            
            // フレーム画像（最前面に配置）
            Image(
                painter = painterResource(id = frameImageResId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f),
                contentScale = ContentScale.FillBounds
            )
            
            // 状態アイコンを右上にバッジ表示（写真がある場合）
            if (seedStatus in listOf("finished", "urgent", "thisMonth", "expired")) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(3f),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = CircleShape
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (statusIconResId != null) {
                            // 状態アイコンをdrawableリソースから表示
                            Icon(
                                painter = painterResource(id = statusIconResId),
                                contentDescription = when (seedStatus) {
                                    "finished" -> "まき終わり"
                                    "urgent" -> "期限間近"
                                    "thisMonth" -> "まきどき"
                                    "expired" -> "期限切れ"
                                    else -> "状態"
                                },
                                modifier = Modifier.size(16.dp),
                                tint = Color.Unspecified
                            )
                        }
                    }
                }
            }
        } else {
            // 背景画像（k_goshiki_jin.png）- 写真がない場合は通常表示
            Image(
                painter = painterResource(id = frameImageResId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            // 写真がない場合のフォールバック表示
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Familyアイコン
                    val rotation = familyRotationMinYearsLabel(seed.family) ?: ""
                    FamilyIcon(
                        family = seed.family,
                        size = 32.dp,
                        cornerRadius = 6.dp,
                        rotationLabel = rotation,
                        badgeProtrusion = 3.dp,
                        showCircleBorder = true
                    )
                    
                    // 商品名
                    Text(
                        text = seed.productName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            
            // フレーム画像（最前面に配置）- 写真がない場合も表示
            Image(
                painter = painterResource(id = frameImageResId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f),
                contentScale = ContentScale.FillBounds
            )
            
                            // 状態アイコンを右上にバッジ表示（写真がない場合）
            if (seedStatus in listOf("finished", "urgent", "thisMonth", "expired")) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(3f),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = CircleShape
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (statusIconResId != null) {
                            // 状態アイコンをdrawableリソースから表示
                            Icon(
                                painter = painterResource(id = statusIconResId),
                                contentDescription = when (seedStatus) {
                                    "finished" -> "まき終わり"
                                    "urgent" -> "期限間近"
                                    "thisMonth" -> "まきどき"
                                    "expired" -> "期限切れ"
                                    else -> "状態"
                                },
                                modifier = Modifier.size(16.dp),
                                tint = Color.Unspecified
                            )
                        }
                    }
                }
            }
        }
    }
}
