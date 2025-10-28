package com.example.seedstockkeeper6.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import com.example.seedstockkeeper6.R
import com.example.seedstockkeeper6.model.NotificationData
import com.example.seedstockkeeper6.model.SeedPacket
import com.example.seedstockkeeper6.model.extractSeedInfoFromNotificationData
import com.example.seedstockkeeper6.notification.NotificationContentGenerator
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SukesanMessageCard(
    seeds: List<SeedPacket>,
    currentMonth: Int,
    currentYear: Int,
    isPreview: Boolean = false,
    farmOwner: String = "水戸黄門",
    farmName: String = "菜園",
    farmLatitude: Double = 35.6762,
    farmLongitude: Double = 139.6503,
    latestNotification: NotificationData?,
    isLoading: Boolean,
    onNotificationClick: () -> Unit
) {
    // NotificationContentGeneratorをrememberでインスタンス化
    val contentGenerator = remember { NotificationContentGenerator() }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // メッセージ部分の高さを取得するためのBox
            var messageHeight by remember { mutableStateOf(0.dp) }
            val density = LocalDensity.current

            // 吹き出し部分
            Card(
                modifier = Modifier
                    .weight(1f)
                    .onSizeChanged { size ->
                        messageHeight = with(density) { size.height.toDp() }
                    }
                    .clickable { 
                        if (latestNotification != null) {
                            onNotificationClick()
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp) // 吹き出しの形（右下の角を小さく）
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // 通知内容
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "通知を読み込み中...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else if (latestNotification != null) {
                        val notification = latestNotification!!
                        
                        Column {
                            // 通知タイトル（1行）
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // 回転アニメーション付きのyabumi_shinshyuアイコン
                                val infiniteTransition = rememberInfiniteTransition(label = "rotation")
                                val rotation by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 360f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(2000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                    ),
                                    label = "rotation"
                                )
                                
                                Image(
                                    painter = painterResource(id = R.drawable.yabumi_shinshyu),
                                    contentDescription = "風車",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .graphicsLayer {
                                            rotationZ = rotation
                                        }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = notification.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // NotificationContentGeneratorを使って1行表示の内容を生成
                            val messageText = contentGenerator.generateSingleLineContent(notification)
                            val lines = messageText.trim().split("\n").filter { it.isNotEmpty() }
                            
                            Column {
                                lines.forEach { line ->
                                    Text(
                                        text = line,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Black,
                                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "通知がありません",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
                        )
                    }
                }
            }
            
            // すけさんアイコン（右側に移動）
            // CoilのImageLoaderを設定（GIFサポート付き）
            val context = LocalContext.current
            val imageLoader = remember {
                ImageLoader.Builder(context)
                    .components {
                        add(ImageDecoderDecoder.Factory()) // GIFをサポートするために必要
                    }
                    .build()
            }
            
            AsyncImage(
                model = R.drawable.suke_up_c,
                contentDescription = "すけさん",
                imageLoader = imageLoader,
                modifier = Modifier.size(
                    width = 60.dp,
                    height = messageHeight
                )
            )
        }
    }
}

@Composable
fun SowingSummaryCards(
    thisMonthSowingCount: Int,
    urgentSeedsCount: Int,
    navController: NavController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.grain),
                contentDescription = "種",
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "今月の種",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 播種予定種子数
            val onThisMonthClick = {
                // 種リスト画面に遷移し、「今月まける」チェックボックスをオンにする
                navController.navigate("list?filter=thisMonth")
            }
            
            SummaryCardWithEmojiIcon(
                emojiIcon = "🌱",
                title = "まきどき",
                value = "$thisMonthSowingCount",
                subtitle = "",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
                onClick = onThisMonthClick
            )
            
            // まき時終了間近の種子数
            val onUrgentClick = {
                // 種リスト画面に遷移し、「終了間近」チェックボックスをオンにする
                navController.navigate("list?filter=urgent")
            }
            
            SummaryCardWithEmojiIcon(
                emojiIcon = "⏳",
                title = "期限間近",
                value = "$urgentSeedsCount",
                subtitle = "",
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                onClick = onUrgentClick
            )
        }
    }
}

@Composable
fun StatisticsWidgets(
    totalSeeds: Int,
    finishedSeedsCount: Int,
    expiredSeedsCount: Int,
    familyDistribution: List<Pair<String, Int>>,
    navController: NavController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = com.example.seedstockkeeper6.R.drawable.chart),
                contentDescription = "統計",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "統計",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 左側：登録総数と期限切れを縦に並べる
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // 登録種子総数
                SummaryCardWithoutIcon(
                    title = "登録総数",
                    value = "$totalSeeds",
                    subtitle = "",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // まきおわり種子数
                val onFinishedClick = {
                    // 種リスト画面に遷移し、「まきおわり」チェックボックスをオンにする
                    navController.navigate("list?filter=finished")
                }
                
                SummaryCardWithoutIcon(
                    title = "まきおわり",
                    value = "$finishedSeedsCount",
                    subtitle = "",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onFinishedClick
                )
                
                // 期限切れ種子数
                val onExpiredClick = {
                    // 種リスト画面に遷移し、「期限切れ」チェックボックスをオンにする
                    navController.navigate("list?filter=expired")
                }
                
                SummaryCardWithoutIcon(
                    title = "期限切れ",
                    value = "$expiredSeedsCount",
                    subtitle = "",
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onExpiredClick
                )
            }
            
            // 右側：科別分布（縦長表示）
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "科別分布",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 円グラフ表示
                    if (familyDistribution.isNotEmpty()) {
                        PieChart(
                            data = familyDistribution,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    } else {
                        Text(
                            text = "有効期限内の種がありません",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCardWithEmojiIcon(
    emojiIcon: String,
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.clickable { onClick?.invoke() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 上段: タイトルのみ
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = contentColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 下段: 絵文字アイコンと値
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = emojiIcon,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(48.dp)
                        .onSizeChanged { size ->
                        }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.onSizeChanged { size ->
                    }
                )
                
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCardWithoutIcon(
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.clickable { onClick?.invoke() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // タイトル
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = contentColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 値
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )

                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PieChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.second }
    if (total == 0) return
    
    val colors = listOf(
        Color(0xFF2196F3),  // 鮮やかな青
        Color(0xFF4CAF50),  // 鮮やかな緑
        Color(0xFFFF9800),  // 鮮やかなオレンジ
        Color(0xFF9C27B0),  // 鮮やかな紫
        Color(0xFFE91E63)   // 鮮やかなピンク
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 円グラフ
        Canvas(
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = minOf(canvasWidth, canvasHeight) / 2f
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f
            
            var startAngle = -90f // 12時の位置から開始
            
            data.forEachIndexed { index, (_, count) ->
                val sweepAngle = (count.toFloat() / total) * 360f
                val color = colors[index % colors.size]
                
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        centerX - radius,
                        centerY - radius
                    ),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                )
                
                startAngle += sweepAngle
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 凡例（円グラフの下に表示）
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            data.take(3).forEachIndexed { index, (family, count) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                colors[index % colors.size],
                                CircleShape
                            )
                    )
                    Text(
                        text = "$family ($count)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * リッチセクション（通知詳細ダイアログ用）
 */
@Composable
private fun RichSection(
    title: String, 
    items: List<Pair<String, String>>, 
    iconResource: Int? = null, 
    textColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconResource != null) {
            Image(
                painter = painterResource(id = iconResource),
                contentDescription = title,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = textColor
        )
    }
    if (items.isEmpty()) {
        Text(
            text = "該当なし",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.8f)
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (name, desc) ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                if (desc.isNotEmpty()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

/**
 * 通知詳細ダイアログ
 */
@Composable
fun NotificationDetailDialog(
    notification: NotificationData,
    onDismiss: () -> Unit
) {
    val contentGenerator = remember { NotificationContentGenerator() }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 通知内容（NotificationContentGeneratorを使用）
                val content = remember(notification) { contentGenerator.generateContent(notification) }
                
                // ヘッダー
                if (notification.summary.isNotEmpty()) {
                    Text(
                        text = notification.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // 今月まきどきの種
                if (notification.thisMonthSeeds.isNotEmpty()) {
                    RichSection(
                        title = "🌱まきどき",
                        items = notification.thisMonthSeeds.map { it.name to it.description },
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // 終了間近の種
                if (notification.endingSoonSeeds.isNotEmpty()) {
                    val endingSoonItems = notification.endingSoonSeeds.map { seed ->
                        val expirationInfo = if (seed.expirationYear > 0 && seed.expirationMonth > 0) {
                            " (${seed.expirationYear}/${seed.expirationMonth})"
                        } else {
                            ""
                        }
                        "${seed.name}${expirationInfo}" to seed.description
                    }
                    RichSection(
                        title = "⏳期限間近",
                        items = endingSoonItems,
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // おすすめの種
                if (notification.recommendedSeeds.isNotEmpty()) {
                    RichSection(
                        title = "🎯今月のおすすめ",
                        items = notification.recommendedSeeds.map { it.name to it.description },
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // 送信日時
                Text(
                    text = "送信日時: ${notification.sentAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}
